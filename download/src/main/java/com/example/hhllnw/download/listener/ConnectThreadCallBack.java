package com.example.hhllnw.download.listener;

/**
 * Created by hhl on 2016/12/26.
 * 线程连接回掉
 */

public interface ConnectThreadCallBack {
    void connectResult(boolean isSupportRange, int totalLength);

    void connectError(String message);
}
