package com.example.hhllnw.download.core;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.hhllnw.download.common.Constants;
import com.example.hhllnw.download.common.TickTack;
import com.example.hhllnw.download.entities.DownloadEntity;
import com.example.hhllnw.download.listener.ConnectThreadCallBack;
import com.example.hhllnw.download.listener.DownloadCallBack;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by hhl on 2016/12/20.
 */

public class DownloadTask implements ConnectThreadCallBack, DownloadCallBack {
    private final DownloadEntity entity;
    private final Handler handler;
    private final Context context;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    private ExecutorService executors;
    private ConnectThread connectThread;
    private SubsectionDownloadThread[] subthreads;

    public DownloadTask(Context context, DownloadEntity entity, Handler handler, ExecutorService executors) {
        this.context = context;
        this.entity = entity;
        this.handler = handler;
        this.executors = executors;
    }

    public void start() {
        entity.setTotalLength(-1);
        if (entity.getTotalLength() > 0) {
            startDownload();
        } else {
            entity.setStatus(DownloadEntity.Status.connecting);
            postStatus();
            connectThread = new ConnectThread(entity.getUrl(), this);
            executors.execute(connectThread);
        }
    }

    /**
     * 开始下载
     */
    private void startDownload() {
        String path = Environment.getExternalStorageDirectory() + "/1test/"
                + entity.getUrl().substring(entity.getUrl().lastIndexOf("/") + 1);
        File file = new File(path);
        if (!file.exists()) {
            entity.setCurLength(0);
        } else {
            if (entity.getPercent() == 100) {
                Toast.makeText(context, "文件已经下载完成,查看路径 " + path, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (entity.isSupportRange()) {//multi threads
            multilDownload(entity.getTotalLength());
        } else {//single threads
            singleDownload();
        }
    }

    /**
     * 发送消息
     */
    private void postStatus() {
        Message msg = Message.obtain();
        msg.obj = entity;
        handler.sendMessage(msg);
    }

    /**
     * 暂停
     */
    public void pause() {
        isPause = true;
        Log.e("pause", "pause");
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (subthreads != null) {
            for (int i = 0; i < subthreads.length; i++) {
                if (subthreads[i] != null && subthreads[i].isRunning()) {
                    if (entity.isSupportRange()) {
                        subthreads[i].pause();
                    } else {
                        subthreads[i].cancel();
                    }
                }
            }
        }
    }

    /**
     * 取消下载
     */
    public void cancel() {
        isCancel = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (subthreads != null) {
            for (int i = 0; i < subthreads.length; i++) {
                if (subthreads[i] != null && subthreads[i].isRunning()) {
                    subthreads[i].cancel();
                }
            }
        }
    }

    /**
     * 多线程分段下载
     *
     * @param totalLength
     */
    private void multilDownload(int totalLength) {
        //entity.setTotalLength(totalLength);
        entity.setStatus(DownloadEntity.Status.downloading);
        postStatus();
        subthreads = new SubsectionDownloadThread[Constants.KEY_MAX_RANGE_THREADS];
        int block = totalLength / Constants.KEY_MAX_RANGE_THREADS;
        int startPosition = 0;
        int endPosition = 0;
        if (entity.getRanges() == null) {
            entity.setRanges(new HashMap<Integer, Integer>());
            for (int i = 0; i < Constants.KEY_MAX_RANGE_THREADS; i++) {
                entity.getRanges().put(i, 0);
            }
        }
        for (int i = 0; i < Constants.KEY_MAX_RANGE_THREADS; i++) {
            startPosition = i * block + entity.getRanges().get(i);
            if (i == Constants.KEY_MAX_RANGE_THREADS - 1) {
                endPosition = totalLength;
            } else {
                endPosition = (i + 1) * block - 1;
            }
            if (startPosition < endPosition) {
                subthreads[i] = new SubsectionDownloadThread(entity.getUrl(), i, startPosition, endPosition, this);
                executors.execute(subthreads[i]);
            } else {
                entity.setStatus(DownloadEntity.Status.completed);
            }
        }
    }

    /**
     * 单线程下载
     */
    private void singleDownload() {
        entity.setStatus(DownloadEntity.Status.downloading);
        postStatus();
        subthreads = new SubsectionDownloadThread[1];
        subthreads[0] = new SubsectionDownloadThread(entity.getUrl(), 0, 0, 0, this);
        executors.execute(subthreads[0]);
    }

    @Override
    public void connectResult(boolean isSupportRange, int totalLength) {
        entity.setSupportRange(isSupportRange);
        entity.setTotalLength(totalLength);
        startDownload();
    }

    @Override
    public void connectError(String message) {
        if (isPause || isCancel) {
            entity.setStatus(isPause ? DownloadEntity.Status.pause : DownloadEntity.Status.cancel);
        } else {
            entity.setStatus(DownloadEntity.Status.err);
        }
        postStatus();
    }

    @Override
    public synchronized void onChangeUI(int index, int progress) {
        if (entity.isSupportRange()) {
            int range = entity.getRanges().get(index) + progress;
            entity.getRanges().put(index, range);
        }
        int curLength = entity.getCurLength() + progress;
        entity.setCurLength(curLength);

        if (TickTack.getInstance().isNotify()) {
            postStatus();
        }

    }

    @Override
    public synchronized void downloadComplete(int index) {

        if (subthreads != null && subthreads.length > 0) {
            for (int i = 0; i < subthreads.length; i++) {
                if (subthreads[i] != null && !subthreads[i].isComplete()) {
                    return;
                }
            }
        }
        if (entity.getTotalLength() > 0 && entity.getCurLength() != entity.getTotalLength()) {
            entity.setStatus(DownloadEntity.Status.err);
            //下载出现异常，删除文件
            entity.reSet();
            String path = Environment.getExternalStorageDirectory() + "/1test/"
                    + entity.getUrl().substring(entity.getUrl().lastIndexOf("/") + 1);
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } else {
            entity.setStatus(DownloadEntity.Status.completed);
        }
        postStatus();

    }

    @Override
    public synchronized void onDownloadError(int index, String message) {

        if (subthreads != null && subthreads.length > 0) {
            for (int i = 0; i < subthreads.length; i++) {
                if (!subthreads[i].isError()) {//把未报错的线程强制停止
                    subthreads[i].cancelByError();
                }
            }
            entity.setStatus(DownloadEntity.Status.err);
            postStatus();
        }
    }

    @Override
    public synchronized void downloadPause() {
        if (subthreads != null && subthreads.length > 0) {
            for (int i = 0; i < subthreads.length; i++) {
                if (!subthreads[i].isPause()) {
                    return;
                }
            }
            entity.setStatus(DownloadEntity.Status.pause);
            postStatus();
        }
    }

    @Override
    public synchronized void downloadCancel() {
        if (subthreads != null && subthreads.length > 0) {
            for (int i = 0; i < subthreads.length; i++) {
                if (!subthreads[i].isCancel()) {
                    return;
                }
            }
            entity.reSet();
            entity.setStatus(DownloadEntity.Status.cancel);
            String path = Environment.getExternalStorageDirectory() + "/1test/"
                    + entity.getUrl().substring(entity.getUrl().lastIndexOf("/") + 1);
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            postStatus();
        }
    }
}
