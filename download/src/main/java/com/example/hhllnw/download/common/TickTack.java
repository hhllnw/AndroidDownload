package com.example.hhllnw.download.common;

/**
 * Created by hhl on 2016/12/28.
 */

public class TickTack {
    private static TickTack mInstance;
    private final long TIME_INTERVAL = 1000;
    private long currentTime = 0;

    private TickTack() {
    }

    public static TickTack getInstance() {
        if (mInstance == null) {
            synchronized (TickTack.class) {
                if (mInstance == null) {
                    mInstance = new TickTack();
                }
            }
        }
        return mInstance;
    }

    public boolean isNotify() {
        long tem = System.currentTimeMillis();
        if (tem - currentTime > TIME_INTERVAL) {
            currentTime = tem;
            return true;
        }
        return false;
    }

}
