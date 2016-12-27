package com.example.hhllnw.download.core;

import android.content.Context;
import android.util.Log;

import com.example.hhllnw.download.db.OrmDBController;
import com.example.hhllnw.download.entities.DownloadEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by hhl on 2016/12/20.
 * 被观察者
 */

public class DataChanger extends Observable {

    private static DataChanger mInstance;
    private static Context mContext;
    private LinkedHashMap<String, DownloadEntity> allMaps;

    private DataChanger(Context context) {
        this.mContext = context;
        allMaps = new LinkedHashMap<>();
    }

    public synchronized static DataChanger getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataChanger(context);
        }
        return mInstance;
    }

    public void postStatus(DownloadEntity entity) {
        allMaps.put(entity.getId(), entity);
        OrmDBController.getmInstance(mContext).createOrUpdate(entity);
        setChanged();
        notifyObservers(entity);
    }

    /**
     * 获取状态为暂停的数据
     *
     * @return
     */
    public ArrayList<DownloadEntity> getAllPauseStatus() {
        ArrayList<DownloadEntity> list = new ArrayList<>();
        if (allMaps.size() > 0) {
            for (Map.Entry<String, DownloadEntity> entry : allMaps.entrySet()) {
                if (entry != null && entry.getValue() != null && entry.getValue().getStatus() == DownloadEntity.Status.pause) {
                    list.add(entry.getValue());
                }
            }
        }
        return list;
    }

    public DownloadEntity queryDownloadEntityById(String id) {
        if (allMaps != null && allMaps.get(id) != null) {
            return allMaps.get(id);
        }
        return null;
    }

    public void addEntityToMap(DownloadEntity entity) {
        allMaps.put(entity.getId(), entity);
    }
}
