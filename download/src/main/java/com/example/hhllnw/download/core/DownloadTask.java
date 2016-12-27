package com.example.hhllnw.download.core;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.hhllnw.download.common.Constants;
import com.example.hhllnw.download.entities.DownloadEntity;
import com.example.hhllnw.download.listener.ConnectThreadCallBack;
import com.example.hhllnw.download.listener.DownloadCallBack;
import com.j256.ormlite.stmt.query.In;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by hhl on 2016/12/20.
 */

public class DownloadTask implements ConnectThreadCallBack, DownloadCallBack {
    private final DownloadEntity entity;
    private final Handler handler;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    private Message msg;
    private ExecutorService executors;
    private ConnectThread connectThread;
    private SubsectionDownloadThread[] subthreads;

    public DownloadTask(DownloadEntity entity, Handler handler, ExecutorService executors) {
        this.entity = entity;
        this.handler = handler;
        this.executors = executors;
    }

    public void start() {
        entity.setStatus(DownloadEntity.Status.connecting);
        postStatus();
        connectThread = new ConnectThread(entity.getUrl(), this);
        executors.execute(connectThread);
    }

    private void postStatus() {
        msg = Message.obtain();
        msg.obj = entity;
        handler.sendMessage(msg);
    }

    public void pause() {
        isPause = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (subthreads != null) {
            for (int i = 0; i < subthreads.length; i++) {
                if (subthreads[i] != null && subthreads[i].isRunning()) {
                    subthreads[i].pause();
                }
            }
        }

    }

    public void cancel() {
        isCancel = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
    }

    @Override
    public void connectResult(boolean isSupportRange, int totalLength) {
        entity.setSupportRange(isSupportRange);
        entity.setTotalLength(totalLength);
        if (isSupportRange) {//multi threads
            multilDownload(totalLength);
        } else {//single threads
            singleDownload();
        }
    }

    private void multilDownload(int totalLength) {
        entity.setTotalLength(totalLength);
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

    private void singleDownload() {

    }

    @Override
    public void onChangeUI(int index, int progress) {
        int range = entity.getRanges().get(index) + progress;
        entity.getRanges().put(index, range);
        int curLength = 0;
        for (int i = 0; i < entity.getRanges().size(); i++) {
            curLength += entity.getRanges().get(i);
        }
        entity.setCurLength(curLength);

        if (curLength == entity.getTotalLength()) {
            entity.setPercent(100);
            entity.setStatus(DownloadEntity.Status.completed);
            postStatus();
        } else {
            int percent = (int) (curLength * 100l / entity.getTotalLength());
            if ((percent > entity.getPercent())) {
                entity.setPercent(percent);
                postStatus();
            }
        }
    }

    @Override
    public void downloadComplete(int index) {

    }

    @Override
    public void error(String message) {
        entity.setStatus(DownloadEntity.Status.err);
        postStatus();
    }

    @Override
    public void downloadPause() {
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
}
