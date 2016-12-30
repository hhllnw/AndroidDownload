package com.example.hhllnw.download.receiver;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.hhllnw.download.R;
import com.example.hhllnw.download.activity.NetWorkActivity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by hhl on 2017/1/3.
 * 网络链接状态广播
 */

public class NetWorkBroadcastReceiver extends BroadcastReceiver {
    public final static int INTENT_NETWORK_USE = 1000;
    public final static int INTENT_NETWORK_NOT_USE = 1001;
    private boolean isNetWork = true;


    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!mobile.isConnected() && !wifi.isConnected()) {
            context.startActivity(new Intent(context, NetWorkActivity.class)
                    .setFlags(FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("KEY_NETWORK", INTENT_NETWORK_NOT_USE));
            isNetWork = false;
        } else {
            if (!isNetWork) {
                context.startActivity(new Intent(context, NetWorkActivity.class)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("KEY_NETWORK", INTENT_NETWORK_USE));
            }
        }
    }


}
