package com.wecare.app.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.wecare.app.data.source.local.greendao.DaoMaster;
import com.wecare.app.data.source.local.greendao.LocationDataDao;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Growth on 2016/3/3.
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Log.i("version", oldVersion + "---先前和更新之后的版本---" + newVersion);
        if (oldVersion < newVersion) {
            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
                @Override
                public void onCreateAllTables(Database db, boolean ifNotExists) {
                    DaoMaster.createAllTables(db, ifNotExists);
                }
                @Override
                public void onDropAllTables(Database db, boolean ifExists) {
                    DaoMaster.dropAllTables(db, ifExists);
                }
            },LocationDataDao.class);
        }
    }
}
