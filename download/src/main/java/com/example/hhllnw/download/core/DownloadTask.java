package com.example.hhllnw.download.core;


import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.hhllnw.download.common.Constants;
import com.example.hhllnw.download.common.DownloadConfig;
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
    private DownloadEntity.Status[] statuses;
    private File file;

    public DownloadTask(Context context, DownloadEntity entity, Handler handler, ExecutorService executors) {
        this.context = context;
        this.entity = entity;
        this.handler = handler;
        this.executors = executors;
        this.file = DownloadConfig.getInstance().getFile(entity.getUrl());
        this.entity.setLocalPath(this.file.getPath());
    }

    public void start() {
        if (entity.getTotalLength() > 0) {
            startDownload();
        } else {
            entity.setStatus(DownloadEntity.Status.connecting);
            postStatus(Constants.HANDLER_CONNECTING);
            connectThread = new ConnectThread(entity.getUrl(), this);
            executors.execute(connectThread);
        }
    }

    /**
     * 开始下载
     */
    private void startDownload() {
        if (!file.exists()) {
            entity.reSet();
        } else {
            if (entity.getTotalLength() > 0 && entity.getCurLength() == entity.getTotalLength()) {
                Toast.makeText(context, "文件已经下载完成", Toast.LENGTH_SHORT).show();
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
    private void postStatus(int what) {
        Message msg = Message.obtain();
        msg.obj = entity;
        msg.what = what;
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

        entity.setStatus(DownloadEntity.Status.downloading);
        postStatus(Constants.HANDLER_DOWNLOADING);
        subthreads = new SubsectionDownloadThread[DownloadConfig.getInstance().getMAX_THREADS()];
        statuses = new DownloadEntity.Status[DownloadConfig.getInstance().getMAX_THREADS()];
        int block = totalLength / DownloadConfig.getInstance().getMAX_THREADS();
        int startPosition = 0;
        int endPosition = 0;
        if (entity.getRanges() == null) {
            entity.setRanges(new HashMap<Integer, Integer>());
            for (int i = 0; i < DownloadConfig.getInstance().getMAX_THREADS(); i++) {
                entity.getRanges().put(i, 0);
            }
        }
        for (int i = 0; i < DownloadConfig.getInstance().getMAX_THREADS(); i++) {
            startPosition = i * block + entity.getRanges().get(i);
            if (i == DownloadConfig.getInstance().getMAX_THREADS() - 1) {
                endPosition = totalLength;
            } else {
                endPosition = (i + 1) * block - 1;
            }
            if (startPosition < endPosition) {
                subthreads[i] = new SubsectionDownloadThread(entity.getUrl(), file, i, startPosition, endPosition, this);
                statuses[i] = DownloadEntity.Status.downloading;
                executors.execute(subthreads[i]);
            } else {
                statuses[i] = DownloadEntity.Status.completed;
            }
        }
    }

    /**
     * 单线程下载
     */
    private void singleDownload() {
        entity.setStatus(DownloadEntity.Status.downloading);
        postStatus(Constants.HANDLER_DOWNLOADING);
        subthreads = new SubsectionDownloadThread[1];
        subthreads[0] = new SubsectionDownloadThread(entity.getUrl(), file, 0, 0, 0, this);
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
            postStatus(isPause ? Constants.HANDLER_PAUSE : Constants.HANDLER_CANCEL);
        } else {
            entity.setStatus(DownloadEntity.Status.err);
            postStatus(Constants.HANDLER_ERROR);
        }

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
            postStatus(Constants.HANDLER_UPDATE);
        }

    }

    @Override
    public synchronized void downloadComplete(int index) {
        statuses[index] = DownloadEntity.Status.completed;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] != null && statuses[i] != DownloadEntity.Status.completed) {
                return;
            }
        }

        if (entity.getTotalLength() > 0 && entity.getCurLength() != entity.getTotalLength()) {
            entity.setStatus(DownloadEntity.Status.err);
            //下载出现异常，删除文件
            entity.reSet();
            postStatus(Constants.HANDLER_ERROR);
        } else {
            entity.setStatus(DownloadEntity.Status.completed);
            postStatus(Constants.HANDLER_COMPLETED);
        }


    }

    @Override
    public synchronized void onDownloadError(int index, String message) {

        statuses[index] = DownloadEntity.Status.err;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] != null && statuses[i] != DownloadEntity.Status.completed && statuses[i] != DownloadEntity.Status.err) {
                subthreads[i].cancelByError();
                return;
            }
        }
        entity.setStatus(DownloadEntity.Status.err);
        postStatus(Constants.HANDLER_ERROR);
    }

    @Override
    public synchronized void downloadPause(int index) {

        statuses[index] = DownloadEntity.Status.pause;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] != null && statuses[i] != DownloadEntity.Status.completed && statuses[i] != DownloadEntity.Status.pause) {
                return;
            }
        }

        entity.setStatus(DownloadEntity.Status.pause);
        postStatus(Constants.HANDLER_PAUSE);

    }

    @Override
    public synchronized void downloadCancel(int index) {

        statuses[index] = DownloadEntity.Status.cancel;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] != null && statuses[i] != DownloadEntity.Status.completed && statuses[i] != DownloadEntity.Status.cancel) {
                return;
            }
        }
        entity.reSet();
        entity.setStatus(DownloadEntity.Status.cancel);
        postStatus(Constants.HANDLER_CANCEL);
    }
}
