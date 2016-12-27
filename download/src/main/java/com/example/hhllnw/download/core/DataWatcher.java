package com.example.hhllnw.download.core;


import com.example.hhllnw.download.entities.DownloadEntity;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by hhl on 2016/12/20.
 * 观察者
 */

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable observable, Object o) {
        notifyUI((DownloadEntity) o);
    }

    public abstract void notifyUI(DownloadEntity o);
}
