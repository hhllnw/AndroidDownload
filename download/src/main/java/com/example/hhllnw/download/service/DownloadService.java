package com.example.hhllnw.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.hhllnw.download.common.Constants;
import com.example.hhllnw.download.core.DataChanger;
import com.example.hhllnw.download.core.DownloadTask;
import com.example.hhllnw.download.db.OrmDBController;
import com.example.hhllnw.download.entities.DownloadEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by hhl on 2016/12/20.
 * 下载服务
 */

public class DownloadService extends Service {
    private HashMap<String, DownloadTask> tasks = new HashMap<>();
    private LinkedBlockingDeque<DownloadEntity> waitDeques;
    private ExecutorService executors;
    private DataChanger mDataChanger;
    private int MAX_TASKS = 5;
    private OrmDBController dbController;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executors = Executors.newCachedThreadPool();
        waitDeques = new LinkedBlockingDeque<>();
        mDataChanger = DataChanger.getInstance(getApplicationContext());
        dbController = OrmDBController.getmInstance(getApplicationContext());
        dbDataToCache();
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntity entity = (DownloadEntity) msg.obj;
            if (entity == null) return;
            switch (entity.getStatus()) {
                case completed:
                case cancel:
                case pause:
                    tasks.remove(entity);
                    nextDownload();
                    break;
            }
            mDataChanger.postStatus(entity);
        }
    };

    /**
     * 如果有等待队列，当正在执行的任务中有completed、cancel、pause状态时，开始下载等待队列中的任务
     */
    private void nextDownload() {
        if (waitDeques != null) {
            DownloadEntity entity = waitDeques.poll();
            startDownload(entity);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntity entity = null;
        int action = -1;
        if (intent.hasExtra(Constants.KEY_INTENT_ENTITY)) {
            entity = (DownloadEntity) intent.getSerializableExtra(Constants.KEY_INTENT_ENTITY);
        }

        if (intent.hasExtra(Constants.KEY_INTENT_ACTION)) {
            action = intent.getIntExtra(Constants.KEY_INTENT_ACTION, -1);
        }

        if (intent.hasExtra(Constants.KEY_INTENT_MAX_TASKS)) {
            MAX_TASKS = intent.getIntExtra(Constants.KEY_INTENT_MAX_TASKS, MAX_TASKS);
        }

        doAction(entity, action);
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(DownloadEntity entity, int action) {

        switch (action) {
            case Constants.KEY_INTENT_ADD:
                addDownload(entity);
                break;
            case Constants.KEY_INTENT_PAUSE:
                pauseDownload(entity);
                break;
            case Constants.KEY_INTENT_RESUME:
                resumeDownload(entity);
                break;
            case Constants.KEY_INTENT_CANCEL:
                cancelDownload(entity);
                break;
            case Constants.KEY_INTENT_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_INTENT_RECOVER_ALL:
                recoverAll();
                break;
        }
    }


    private void addDownload(DownloadEntity entity) {

        if (tasks != null && tasks.size() >= MAX_TASKS) {
            entity.setStatus(DownloadEntity.Status.waiting);
            waitDeques.offer(entity);
            mDataChanger.postStatus(entity);
        } else {
            startDownload(entity);
        }
    }

    private void startDownload(DownloadEntity entity) {
        if (entity == null)
            return;
        DownloadTask task = new DownloadTask(entity, mHandle, executors);
        task.start();
        tasks.put(entity.getId(), task);

        //executors.execute(task);
    }

    private void pauseDownload(DownloadEntity entity) {
        if (entity == null)
            return;
        if (tasks != null && tasks.size() > 0) {
            DownloadTask task = tasks.remove(entity.getId());
            task.pause();
        } else {
            waitDeques.remove(entity);
        }
    }

    private void resumeDownload(DownloadEntity entity) {
        addDownload(entity);
    }

    private void cancelDownload(DownloadEntity entity) {

        if (entity == null)
            return;
        if (tasks != null && tasks.size() > 0) {
            DownloadTask task = tasks.remove(entity.getId());
            task.cancel();
        } else {
            waitDeques.remove(entity);
        }
    }

    /**
     * 暂停所有任务
     */
    private void pauseAll() {

        if (waitDeques != null && waitDeques.size() > 0) {
            Iterator<DownloadEntity> it = waitDeques.iterator();
            while (it.hasNext()) {
                DownloadEntity entity = it.next();
                entity.setStatus(DownloadEntity.Status.pause);
                mDataChanger.postStatus(entity);
            }
            waitDeques.clear();
        }

        if (tasks != null && tasks.size() > 0) {
            for (Map.Entry<String, DownloadTask> entry : tasks.entrySet()) {
                entry.getValue().pause();
            }
            tasks.clear();
        }
    }

    /**
     * 恢复所有任务
     */
    private void recoverAll() {
        ArrayList<DownloadEntity> data = mDataChanger.getAllPauseStatus();
        if (data != null && data.size() > 0) {
            for (DownloadEntity entity : data) {
                addDownload(entity);
            }
        }
    }

    /**
     * 把数据库数据添加至缓存数据中
     */
    private void dbDataToCache() {
        ArrayList<DownloadEntity> entities = dbController.queryAll();
        if (entities != null && entities.size() > 0) {
            Iterator<DownloadEntity> iterator = entities.listIterator();
            while (iterator.hasNext()) {
                DownloadEntity entity = iterator.next();
                if (entity != null) {
                    if (entity.getStatus() == DownloadEntity.Status.downloading) {
                        entity.setStatus(DownloadEntity.Status.downloading);
                    }
                    mDataChanger.addEntityToMap(entity);
                }
            }
        }
    }


}
