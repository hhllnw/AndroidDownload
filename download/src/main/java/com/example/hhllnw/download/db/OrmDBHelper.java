package com.example.hhllnw.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.hhllnw.download.entities.DownloadEntity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by hhl on 2016/12/23.
 */

public class OrmDBHelper extends OrmLiteSqliteOpenHelper {
    private static final String DB_NAME = "db_download";
    private static final int DB_VERSION = 1;
    private static OrmDBHelper mInstance;


    private OrmDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public OrmDBHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, DownloadEntity.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    /**
     * 获取单列
     *
     * @param context
     * @return
     */
    public static OrmDBHelper getmInstance(Context context) {

        if (mInstance == null) {
            synchronized (OrmDBHelper.class) {
                if (mInstance == null) {
                    mInstance = new OrmDBHelper(context);
                }
            }
        }
        return mInstance;
    }


}
