package com.example.hhllnw.download.listener;

/**
 * Created by hhl on 2016/12/26.
 * 线程连接回掉
 */

public interface ConnectThreadCallBack {
    public void connectResult(boolean isSupportRange, int totalLength);

    public void error(String message);
}
