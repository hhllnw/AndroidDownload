package com.example.hhllnw.download.listener;

/**
 * Created by hhl on 2016/12/26.
 */

public interface DownloadCallBack {
    void onChangeUI(int index, int progress);

    void downloadComplete(int index);

    void error(String message);

    void downloadPause();
}
