package com.example.hhllnw.download.manager;

import android.content.Context;
import android.content.Intent;

import com.example.hhllnw.download.common.Constants;
import com.example.hhllnw.download.core.DataChanger;
import com.example.hhllnw.download.core.DataWatcher;
import com.example.hhllnw.download.entities.DownloadEntity;
import com.example.hhllnw.download.service.DownloadService;

import java.util.ArrayList;

/**
 * Created by hhl on 2016/12/20.
 * 下载管理
 */

public class DownloadManager {

    private static DownloadManager mDownloadManager;
    private final Context mContext;
    private int MAX_TASKS = 5;//最大线程数

    private DownloadManager(Context context) {
        this.mContext = context;
        context.startService(new Intent(context, DownloadService.class));
    }

    public static DownloadManager getInstance(Context context) {
        if (mDownloadManager == null) {
            mDownloadManager = new DownloadManager(context);
        }
        return mDownloadManager;
    }

    public void add(DownloadEntity entity) {
        toDownloadService(entity, Constants.KEY_INTENT_ADD);
    }

    public void pause(DownloadEntity entity) {
        toDownloadService(entity, Constants.KEY_INTENT_PAUSE);
    }

    public void resume(DownloadEntity entity) {
        toDownloadService(entity, Constants.KEY_INTENT_RESUME);
    }

    public void cancel(DownloadEntity entity) {
        toDownloadService(entity, Constants.KEY_INTENT_CANCEL);
    }

    /**
     * 暂停正在下载或等待的任务
     */
    public void pauseAll() {
        Intent it = new Intent(mContext, DownloadService.class);
        it.putExtra(Constants.KEY_INTENT_ACTION, Constants.KEY_INTENT_PAUSE_ALL);
        mContext.startService(it);
    }

    /**
     * 把暂停状态恢复至暂停前状态
     */
    public void recoverAll() {
        Intent it = new Intent(mContext, DownloadService.class);
        it.putExtra(Constants.KEY_INTENT_ACTION, Constants.KEY_INTENT_RECOVER_ALL);
        mContext.startService(it);
    }

    private void toDownloadService(DownloadEntity entity, int action) {
        Intent it = new Intent(mContext, DownloadService.class);
        it.putExtra(Constants.KEY_INTENT_ENTITY, entity);
        it.putExtra(Constants.KEY_INTENT_ACTION, action);
        it.putExtra(Constants.KEY_INTENT_MAX_TASKS, MAX_TASKS);
        mContext.startService(it);
    }

    /**
     * 最大下载数
     *
     * @param MAX_TASKS
     */
    public void setMaxTasks(int MAX_TASKS) {
        this.MAX_TASKS = MAX_TASKS;
    }

    /**
     * 根据id获取DownloadEntity
     *
     * @param id
     * @return
     */
    public DownloadEntity findDownloadEntityById(String id) {
        return DataChanger.getInstance(mContext).queryDownloadEntityById(id);
    }

    /**
     * 注册观察者
     *
     * @param watcher
     */
    public void registerObserver(DataWatcher watcher) {
        DataChanger.getInstance(mContext).addObserver(watcher);
    }

    /**
     * 注销观察者
     *
     * @param watcher
     */
    public void unregisterObserver(DataWatcher watcher) {
        DataChanger.getInstance(mContext).deleteObserver(watcher);
    }


}
