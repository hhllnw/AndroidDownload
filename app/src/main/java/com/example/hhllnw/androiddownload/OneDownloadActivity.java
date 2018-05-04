package com.example.hhllnw.androiddownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.hhllnw.download.common.ZipExtractorTask;
import com.example.hhllnw.download.core.DataWatcher;
import com.example.hhllnw.download.entities.DownloadEntity;
import com.example.hhllnw.download.manager.DownloadManager;

/**
 * Created by hhl on 2016/12/20.
 */

public class OneDownloadActivity extends AppCompatActivity {
    private DownloadEntity entity;
    private DownloadManager downloadManager;
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUI(DownloadEntity o) {
            Log.e("hh", "" + o.getCurLength() + "/" + o.getTotalLength());
            Log.i("hh", "status:" + o.getStatus());
            if (o.getStatus().equals(DownloadEntity.Status.completed)){
                String inPath = o.getLocalPath();
                String outPath = inPath.substring(0,inPath.lastIndexOf("/")+1);
                Log.i("hh", "inPath:" + inPath);
                Log.i("hh", "outPath:" + outPath);
                doZipExtractorWork(inPath,outPath);
            }
            entity = o;
        }
    };


    public void doZipExtractorWork(String inPath,String outPath){
        ZipExtractorTask task = new ZipExtractorTask(inPath,outPath);
        task.setOnResolveZipListener(new ZipExtractorTask.OnResolveZipListener() {
            @Override
            public void startResolveZip() {
                Log.e("hh","startResolveZip");
            }

            @Override
            public void progress(long currentLength, long totalLength) {
                Log.e("hh","currentLength:"+currentLength+"/totalLength:"+totalLength);
            }

            @Override
            public void complete() {
                Log.e("hh","complete");
            }

            @Override
            public void Exception(Exception e) {

            }
        });
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_download);
        downloadManager = DownloadManager.getInstance(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        //entity = new DownloadEntity("http://shouji.360tpcdn.com/150723/de6fd89a346e304f66535b6d97907563/com.sina.weibo_2057.apk");
        //entity = new DownloadEntity("http://f.hiphotos.baidu.com/image/pic/item/0bd162d9f2d3572ce46b99dd8813632762d0c322.jpg");
        entity = new DownloadEntity("16","https://github.com/hhllnw/CustomWidgetDemo/raw/master/test.zip"/*"http://58.214.246.162:10119/file/a6acb416-d1cd-4d3c-b48f-72db8117180d.png"*/);
        //entity.setId(5 + "20");
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadManager.add(entity);//
            }
        });

        findViewById(R.id.btn_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadManager.pause(entity);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadManager.registerObserver(watcher);
    }

    @Override
    protected void onStop() {
        super.onStop();
        downloadManager.unregisterObserver(watcher);
    }
}
