package com.example.hhllnw.download.listener;

/**
 * Created by hhl on 2016/12/26.
 */

public interface DownloadCallBack {
    void onChangeUI(int index, int progress);

    void downloadComplete(int index);

    void onDownloadError(int index, String message);

    void downloadPause();

    void downloadCancel();
}
