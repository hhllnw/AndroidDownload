package com.example.hhllnw.download.core;

import android.util.Log;
import android.webkit.URLUtil;

import com.example.hhllnw.download.listener.ConnectThreadCallBack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hhl on 2016/12/26.
 */

public class ConnectThread implements Runnable {

    private final String url;
    private ConnectThreadCallBack callBack;
    private boolean running;

    public ConnectThread(String url, ConnectThreadCallBack callBack) {
        this.url = url;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            running = true;

            if (!URLUtil.isNetworkUrl(url)) {
                Log.e("err", "network url is not valid");
            }
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            //connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            connection.setConnectTimeout(45 * 1000);
            connection.setReadTimeout(45 * 1000);
            int status = connection.getResponseCode();
            Log.e("hh", "status:" + status);
            int totalLength = connection.getContentLength();
            boolean isSupportRange = false;
            if (status == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                if (callBack != null) {
                    callBack.connectResult(isSupportRange, totalLength);
                }
            } else {
                if (callBack != null)
                    callBack.connectError("server err:" + status);
            }
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
            if (callBack != null) {
                callBack.connectError(e.getMessage());
            }
        } finally {
            running = false;
            if (connection != null)
                connection.disconnect();
        }
    }


    public void cancel() {
        Thread.currentThread().interrupt();
    }

    public boolean isRunning() {
        return running;
    }
}
