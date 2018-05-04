package com.example.hhllnw.download.common;

import android.os.Environment;

import java.io.File;

/**
 * Created by hhl on 2016/12/29.
 * 参数配置
 */

public class DownloadConfig {
    private static DownloadConfig instance;
    private int MAX_TASKS = 3;
    private int MAX_THREADS = 3;
    private File file;
    private String directoryName = "1";
    private boolean isRecover;

    private DownloadConfig() {
        file = Environment.getExternalStorageDirectory();
    }

    public static DownloadConfig getInstance() {
        if (instance == null) {
            synchronized (DownloadConfig.class) {
                if (instance == null) {
                    instance = new DownloadConfig();
                }
            }
        }
        return instance;
    }

    public int getMAX_TASKS() {
        return MAX_TASKS;
    }

    public void setMAX_TASKS(int MAX_TASKS) {
        this.MAX_TASKS = MAX_TASKS;
    }

    public int getMAX_THREADS() {
        return MAX_THREADS;
    }

    public void setMAX_THREADS(int MAX_THREADS) {
        this.MAX_THREADS = MAX_THREADS;
    }


    public boolean isRecover() {
        return isRecover;
    }

    public void setRecover(boolean recover) {
        isRecover = recover;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public File getFile(String url) {
        String path = file + File.separator + getDirectoryName() + File.separator;
        makeRootDirectory(path);
        path = path + url.substring(url.lastIndexOf("/") + 1);
        return new File(path);
    }

    /**
     * 创建目录
     *
     * @param filePath
     */
    private void makeRootDirectory(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
