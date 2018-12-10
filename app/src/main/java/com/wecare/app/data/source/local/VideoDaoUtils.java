package com.wecare.app.data.source.local;

import android.content.Context;
import android.util.Log;

import com.wecare.app.data.entity.VideoData;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/22 11:01
 */
public class VideoDaoUtils {
    private static final String TAG = "VideoDaoUtils";
    private DaoManager mManager;

    public VideoDaoUtils(Context context) {
        this.mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成VideoData记录的插入，如果表未创建，先创建VideoData表
     * @param VideoData
     * @return
     */
    public boolean insert(VideoData VideoData){
        boolean flag = false;
        flag = mManager.getDaoSession().getVideoDataDao().insert(VideoData) == -1 ? false : true;
        Log.i(TAG, "insert VideoData :" + flag + "-->" + VideoData.toString());
        return flag;
    }


    /**
     * 插入多条数据，在子线程操作
     * @param VideoDataList
     * @return
     */
    public boolean insert(final List<VideoData> VideoDataList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (VideoData VideoData : VideoDataList) {
                        mManager.getDaoSession().insertOrReplace(VideoData);
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
     * @param VideoData
     * @return
     */
    public boolean updateVideoData(VideoData VideoData){
        boolean flag = false;
        try {
            mManager.getDaoSession().update(VideoData);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     * @param VideoData
     * @return
     */
    public boolean deleteVideoData(VideoData VideoData){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(VideoData);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    public boolean deleteVideoData(final List<VideoData> VideoDataList){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (VideoData VideoData : VideoDataList) {
                        mManager.getDaoSession().delete(VideoData);
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
            mManager.getDaoSession().deleteAll(VideoData.class);
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
    public List<VideoData> queryAllVideoData(){
        return mManager.getDaoSession().loadAll(VideoData.class);
    }

    public List<VideoData> queryVideoData(int limit){
        QueryBuilder<VideoData> qb = mManager.getDaoSession().queryBuilder(com.wecare.app.data.entity.VideoData.class).limit(limit);
        return qb.list();
    }

    /**
     * 根据主键id查询记录
     * @param key
     * @return
     */
    public VideoData queryVideoDataById(long key){
        return mManager.getDaoSession().load(VideoData.class, key);
    }


    /**
     * 使用native sql进行查询操作
     */
    public List<VideoData> queryByNativeSql(String sql, String[] conditions){
        return mManager.getDaoSession().queryRaw(VideoData.class, sql, conditions);
    }
}
