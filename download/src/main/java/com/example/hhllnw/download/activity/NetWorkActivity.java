package com.example.hhllnw.download.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.hhllnw.download.R;
import com.example.hhllnw.download.receiver.NetWorkBroadcastReceiver;

/**
 * Created by hhl on 2017/1/3.
 */

public class NetWorkActivity extends AppCompatActivity {
    private int network;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_dialog);
        network = getIntent().getIntExtra("KEY_NETWORK", NetWorkBroadcastReceiver.INTENT_NETWORK_USE);
        if (network == NetWorkBroadcastReceiver.INTENT_NETWORK_USE) {
            this.finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        network = intent.getIntExtra("KEY_NETWORK", NetWorkBroadcastReceiver.INTENT_NETWORK_USE);
        if (network == NetWorkBroadcastReceiver.INTENT_NETWORK_USE) {
            this.finish();
        }
    }
}
