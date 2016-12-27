package com.example.hhllnw.download.db;

import android.content.Context;
import android.util.Log;

import com.example.hhllnw.download.entities.DownloadEntity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by hhl on 2016/12/23.
 */

public class OrmDBController {
    private static OrmDBController mInstance;
    private static OrmDBHelper ormDBHelper;

    private OrmDBController(Context context) {
        ormDBHelper = OrmDBHelper.getmInstance(context);
        ormDBHelper.getWritableDatabase();
    }

    /**
     * 获取OrmDBController单列
     *
     * @param context
     * @return
     */
    public static OrmDBController getmInstance(Context context) {
        if (mInstance == null) {
            synchronized (OrmDBController.class) {
                if (mInstance == null) {
                    mInstance = new OrmDBController(context);
                }
            }
        }
        return mInstance;
    }

    public synchronized void createOrUpdate(DownloadEntity entity) {
        try {
            Dao<DownloadEntity, String> dao = ormDBHelper.getDao(DownloadEntity.class);
            dao.createOrUpdate(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<DownloadEntity> queryAll() {
        try {
            Dao<DownloadEntity, String> dao = ormDBHelper.getDao(DownloadEntity.class);
            return (ArrayList<DownloadEntity>) dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized DownloadEntity findById(String id) {
        try {
            if (id != null && !"".equals(id)) {
                Dao<DownloadEntity, String> dao = ormDBHelper.getDao(DownloadEntity.class);
                return dao.queryForId(id);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized void deleteById(String id) {

        try {
            if (id != null && !"".equals(id)) {
                Dao<DownloadEntity, String> dao = ormDBHelper.getDao(DownloadEntity.class);
                dao.deleteById(id);
            } else {
                Log.e("err", "id is not null.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
