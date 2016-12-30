package com.example.hhllnw.androiddownload;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;


import com.example.hhllnw.download.common.DownloadConfig;
import com.example.hhllnw.download.manager.DownloadManager;

import java.util.List;


public class MyApplication extends Application {
    private final String TAG = MyApplication.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName(this, android.os.Process.myPid());
        if (processName != null) {
            boolean defaultProcess = processName
                    .equals(getPackageName());
            if (defaultProcess) {
                initAppForMainProcess();
            }
        }

        DownloadManager.getInstance(getApplicationContext());
        DownloadConfig config = DownloadConfig.getInstance();
        config.setMAX_TASKS(5);
        config.setMAX_THREADS(3);
        config.setDirectoryName("1_download");

    }


    private void initAppForMainProcess() {
        try {
            System.out.println("MyApplication : ");
            // 把自定义的异常处理类设置 给主线程
            MyCrashHandler myCrashHandler = MyCrashHandler.getInstance();
            myCrashHandler.init(getApplicationContext());
            Thread.setDefaultUncaughtExceptionHandler(myCrashHandler);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}