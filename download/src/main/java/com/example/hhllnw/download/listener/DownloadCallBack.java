package com.example.hhllnw.download.listener;

/**
 * Created by hhl on 2016/12/26.
 * 下载回调
 */

public interface DownloadCallBack {
    /**
     * 更新UI
     *
     * @param index
     * @param progress
     */
    void onChangeUI(int index, int progress);

    /**
     * 某一线程下载完成
     *
     * @param index
     */
    void downloadComplete(int index);

    /**
     * 下载出错
     *
     * @param index
     * @param message
     */
    void onDownloadError(int index, String message);

    /**
     * 暂停下载
     */
    void downloadPause();

    /**
     * 取消下载
     */
    void downloadCancel();
}
