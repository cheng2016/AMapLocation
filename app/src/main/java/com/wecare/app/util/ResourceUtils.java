package com.wecare.app.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class ResourceUtils {

    /**
     * android 获取Asset中Properties文件配置的键值对
     * @param context
     * @param key
     * @return
     */
    public static String getProperties(Context context, String key){
        String defaultValues = "";
        InputStream inputStream = null;
        try {
            AssetManager assetManager = context.getApplicationContext().getAssets();
            String confFile = "kudalocation.properties";
            inputStream = assetManager.open(confFile);
            Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, "utf-8"));
            String value = properties.getProperty(key,defaultValues);
            inputStream.close();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultValues;
    }


    /**
     * 读取assets中的文件
     *
     * @param fileName 文件名
     * @return
     */
    public static String getFileFromAssets(Context context, String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder("");
            InputStreamReader in = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            in.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取raw中的文件
     *
     * @param context
     * @param resid
     * @return
     */
    public static String getFileFromRaw(Context context, int resid) {
        InputStreamReader in = null;
        BufferedReader br = null;
        try {
            StringBuffer sb = new StringBuffer("");
            in = new InputStreamReader(context.getResources().openRawResource(resid));
            br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static InputStream getAssert(Context context, String name) {
        try {
            InputStream inputStream = context.getAssets().open(name);
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
