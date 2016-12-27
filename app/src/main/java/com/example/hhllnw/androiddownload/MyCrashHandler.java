package com.example.hhllnw.androiddownload;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Date;

public class MyCrashHandler implements UncaughtExceptionHandler {
    private static MyCrashHandler myCrashHandler;
    private Context context;
    private String TAG = MyCrashHandler.class.getSimpleName();

    private MyCrashHandler() {

    }

    public static synchronized MyCrashHandler getInstance() {
        if (myCrashHandler == null) {
            myCrashHandler = new MyCrashHandler();
        }
        return myCrashHandler;
    }

    public void init(Context context) {
        this.context = context;
    }

    /*
     * 程序发生异常的时候调用的方法
     *
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "~~~~~~~~出现崩溃~~~~~~~ ");

        StringBuilder sb = new StringBuilder();
        // 1.获取当前应用程序的版本号.
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packinfo = pm.getPackageInfo(context.getPackageName(), 0);
            sb.append(new Date());
            sb.append("\n");
            sb.append("程序的版本号?" + packinfo.versionName);
            sb.append("\n");

            // 2.获取手机的硬件信�?.
            Field[] fields = Build.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                String name = fields[i].getName();
                sb.append(name + " = ");
                String value = fields[i].get(null).toString();
                sb.append(value);
                sb.append("\n");
            }
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);

            String result = writer.toString();
            sb.append(result);
            sb.append("***********************************end******************************************");

            Log.e(TAG, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // // 自杀
        // android.os.Process.killProcess(android.os.Process.myPid());
        // ActivityManager activityMgr = (ActivityManager) context
        // .getSystemService(Context.ACTIVITY_SERVICE);
        // activityMgr.killBackgroundProcesses(context.getPackageName());
    }
}
