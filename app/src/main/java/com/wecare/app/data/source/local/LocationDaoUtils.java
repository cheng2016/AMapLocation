package com.wecare.app.data.source.local;

import android.content.Context;
import android.util.Log;

import com.wecare.app.data.entity.LocationData;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class LocationDaoUtils {
    private static final String TAG = "LocationDaoUtils";
    private DaoManager mManager;

    public LocationDaoUtils(Context context) {
        this.mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成LocationData记录的插入，如果表未创建，先创建LocationData表
     * @param LocationData
     * @return
     */
    public boolean insert(LocationData LocationData){
        boolean flag = false;
        flag = mManager.getDaoSession().getLocationDataDao().insert(LocationData) == -1 ? false : true;
        Log.i(TAG, "insert LocationData :" + flag + "-->" + LocationData.toString());
        return flag;
    }


    /**
     * 插入多条数据，在子线程操作
     * @param LocationDataList
     * @return
     */
    public boolean insert(final List<LocationData> LocationDataList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (LocationData LocationData : LocationDataList) {
                        mManager.getDaoSession().insertOrReplace(LocationData);
                    }
                }
            });
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改一条数据
     * @param LocationData
     * @return
     */
    public boolean updateLocationData(LocationData LocationData){
        boolean flag = false;
        try {
            mManager.getDaoSession().update(LocationData);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     * @param LocationData
     * @return
     */
    public boolean deleteLocationData(LocationData LocationData){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(LocationData);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    public boolean deleteLocationData(final List<LocationData> LocationDataList){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (LocationData LocationData : LocationDataList) {
                        mManager.getDaoSession().delete(LocationData);
                    }
                }
            });
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     * @return
     */
    public boolean deleteAll(){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().deleteAll(LocationData.class);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     * @return
     */
    public List<LocationData> queryAllLocationData(){
        return mManager.getDaoSession().loadAll(LocationData.class);
    }

    public List<LocationData> queryLocationData(int limit){
        QueryBuilder<LocationData> qb = mManager.getDaoSession().queryBuilder(com.wecare.app.data.entity.LocationData.class).limit(limit);
        return qb.list();
    }

    /**
     * 根据主键id查询记录
     * @param key
     * @return
     */
    public LocationData queryLocationDataById(long key){
        return mManager.getDaoSession().load(LocationData.class, key);
    }


    /**
     * 使用native sql进行查询操作
     */
    public List<LocationData> queryByNativeSql(String sql, String[] conditions){
        return mManager.getDaoSession().queryRaw(LocationData.class, sql, conditions);
    }
}
