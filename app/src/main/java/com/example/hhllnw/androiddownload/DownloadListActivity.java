package com.example.hhllnw.androiddownload;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hhllnw.download.core.DataWatcher;
import com.example.hhllnw.download.entities.DownloadEntity;
import com.example.hhllnw.download.manager.DownloadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhl on 2016/12/21.
 */

public class DownloadListActivity extends AppCompatActivity {

    private DownloadManager downloadManager;
    private ListView mListView;
    private List<DownloadEntity> data;
    private MyAdapter myAdapter;
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUI(DownloadEntity o) {
            int index = data.indexOf(o);
            if (index != -1) {
                data.remove(index);
                data.add(index, o);
                myAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        mListView = (ListView) findViewById(R.id.mListView);
        downloadManager = DownloadManager.getInstance(this);
        downloadManager.setMaxTasks(3);

        data = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            DownloadEntity entity = new DownloadEntity("http://shouji.360tpcdn.com/150723/de6fd89a346e304f66535b6d97907563/com.sina.weibo_2057.apk");
            entity.setId(i + "30");
            data.add(entity);
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                DownloadEntity entity = downloadManager.findDownloadEntityById(data.get(i).getId());
                if (entity != null) {
                    data.remove(i);
                    data.add(i, entity);
                }
            }
        }

        myAdapter = new MyAdapter();
        mListView.setAdapter(myAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadManager.registerObserver(watcher);
    }

    @Override
    protected void onStop() {
        super.onStop();
        downloadManager.unregisterObserver(watcher);
    }

    private class MyAdapter extends BaseAdapter {
        private ViewHolder viewHolder;

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public DownloadEntity getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup parent) {
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
                viewHolder.tv_name = (TextView) view.findViewById(R.id.item_name);
                viewHolder.btn = (Button) view.findViewById(R.id.item_btn);
                viewHolder.tv_progress = (TextView) view.findViewById(R.id.item_progress);
                viewHolder.btn_cancel = (Button) view.findViewById(R.id.item_btn_cancel);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.tv_name.setText(getItem(i).getName());
            if (getItem(i).getStatus().equals(DownloadEntity.Status.downloading)) {
                viewHolder.btn.setText("暂停");
            } else if (getItem(i).getStatus().equals(DownloadEntity.Status.idle)) {
                viewHolder.btn.setText("开始");
            } else if (getItem(i).getStatus().equals(DownloadEntity.Status.completed)) {
                viewHolder.btn.setText("下载结束");
            } else if (getItem(i).getStatus().equals(DownloadEntity.Status.pause)) {
                viewHolder.btn.setText("继续下载");
            } else if (getItem(i).getStatus() == DownloadEntity.Status.waiting) {
                viewHolder.btn.setText("等待下载");
            }
            viewHolder.tv_progress.setText(getItem(i).getCurLength() + "/" + getItem(i).getTotalLength() + "  状态：" + getItem(i).getStatus());

            viewHolder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getItem(i).getStatus().equals(DownloadEntity.Status.downloading)) {
                        getItem(i).setStatus(DownloadEntity.Status.pause);
                        downloadManager.pause(getItem(i));
                    } else if (getItem(i).getStatus().equals(DownloadEntity.Status.idle)
                            || getItem(i).getStatus().equals(DownloadEntity.Status.pause)
                            || getItem(i).getStatus() == DownloadEntity.Status.cancel) {
                        getItem(i).setStatus(DownloadEntity.Status.downloading);
                        downloadManager.add(getItem(i));
                    }
                }
            });
            viewHolder.btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getItem(i).setStatus(DownloadEntity.Status.cancel);
                    downloadManager.cancel(getItem(i));
                }
            });
            return view;
        }


        private class ViewHolder {
            TextView tv_name;
            TextView tv_progress;
            Button btn;
            Button btn_cancel;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pauseAll:
                downloadManager.pauseAll();
                break;
            case R.id.recoverAll:
                downloadManager.recoverAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
