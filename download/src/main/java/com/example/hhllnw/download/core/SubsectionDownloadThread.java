package com.example.hhllnw.download.core;

import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import com.example.hhllnw.download.entities.DownloadEntity;
import com.example.hhllnw.download.listener.DownloadCallBack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hhl on 2016/12/20.
 * 分段下载线程
 */

public class SubsectionDownloadThread implements Runnable {
    private String url;
    private int startPosition;
    private int endPosition;
    private int index;
    private DownloadCallBack callBack;
    private String path;
    private DownloadEntity.Status mStatus;
    private volatile boolean isPause;
    private volatile boolean isCancelled;
    private volatile boolean isError;
    private boolean isSingle;
    private int CONNECT_TIME_OUT = 1000 * 45;
    private int READ_TIME_OUT = 1000 * 45;

    public SubsectionDownloadThread(String url, int index, int startPosition, int endPosition, DownloadCallBack callBack) {
        this.url = url;
        this.index = index;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.callBack = callBack;
        this.path = Environment.getExternalStorageDirectory() + "/1test/";
        if (startPosition == 0 && endPosition == 0) {
            isSingle = true;
        } else {
            isSingle = false;
        }
    }


    @Override
    public void run() {
        mStatus = DownloadEntity.Status.downloading;
        HttpURLConnection connection = null;
        //多线程分段下载写入文件
        RandomAccessFile accessFile = null;
        //单线程下载写入文件
        FileOutputStream fos = null;
        InputStream in = null;
        try {
            if (!URLUtil.isNetworkUrl(url)) {
                Log.e("err", "the url is not valid");
            }
            URL mUrl = new URL(url);
            connection = (HttpURLConnection) mUrl.openConnection();
            connection.setRequestMethod("GET");
            if (!isSingle) {
                connection.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
            }
            connection.setConnectTimeout(CONNECT_TIME_OUT);
            connection.setReadTimeout(READ_TIME_OUT);
            int status = connection.getResponseCode();
            makeRootDirectory(path);
            File file = new File(path + url.substring(url.lastIndexOf("/") + 1));

            if (status == HttpURLConnection.HTTP_PARTIAL) {//多线程分段下载 206
                accessFile = new RandomAccessFile(file, "rw");
                accessFile.seek(startPosition);
                in = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    if (isPause || isCancelled || isError) {
                        break;
                    }
                    accessFile.write(buffer, 0, len);
                    if (callBack != null) {
                        callBack.onChangeUI(index, len);
                    }
                }
            } else if (status == HttpURLConnection.HTTP_OK) {//单线程下载
                fos = new FileOutputStream(file);
                in = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    if (isPause || isCancelled || isError) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    if (callBack != null) {
                        callBack.onChangeUI(index, len);
                    }
                }
            } else {
                mStatus = DownloadEntity.Status.err;
                callBack.onDownloadError(index, "server is error.");
                return;
            }

            if (callBack != null) {
                if (isPause) {
                    mStatus = DownloadEntity.Status.pause;
                    callBack.downloadPause();
                } else if (isCancelled) {
                    mStatus = DownloadEntity.Status.cancel;
                    callBack.downloadCancel();
                } else if (isError) {
                    mStatus = DownloadEntity.Status.err;
                    callBack.onDownloadError(index, "err");
                } else {
                    mStatus = DownloadEntity.Status.completed;
                    callBack.downloadComplete(index);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callBack != null) {
                if (isPause) {
                    mStatus = DownloadEntity.Status.pause;
                    callBack.downloadPause();
                } else if (isCancelled) {
                    mStatus = DownloadEntity.Status.cancel;
                    callBack.downloadCancel();
                } else {
                    mStatus = DownloadEntity.Status.err;
                    callBack.onDownloadError(index, e.getMessage());
                }
            }
        } finally {
            if (connection != null)
                connection.disconnect();
            try {
                if (accessFile != null) {
                    accessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建目录
     *
     * @param filePath
     */
    public void makeRootDirectory(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return mStatus == DownloadEntity.Status.downloading;
    }

    public void pause() {
        isPause = true;
        Thread.currentThread().interrupt();
    }

    public boolean isPause() {
        return mStatus == DownloadEntity.Status.pause || mStatus == DownloadEntity.Status.completed;
    }

    public void cancel() {
        isCancelled = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCancel() {
        return mStatus == DownloadEntity.Status.cancel || mStatus == DownloadEntity.Status.completed;
    }

    public boolean isError() {
        return mStatus == DownloadEntity.Status.err;
    }

    public void cancelByError() {
        isError = true;
        Thread.currentThread().interrupt();
    }

    public boolean isComplete() {
        return mStatus == DownloadEntity.Status.completed;
    }
}
