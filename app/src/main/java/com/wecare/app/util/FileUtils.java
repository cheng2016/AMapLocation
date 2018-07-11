package com.wecare.app.util;

import java.io.File;

public class FileUtils {

    /**
     * mkdir() 创建单个文件夹，要确保它的上级文件夹存在。
     * @param folderPath
     */
    public static void createFile(String folderPath){
        File dir = new File(folderPath);
        if(!dir.exists()){
            dir.mkdir();
        }
    }

    /**
     * mkdirs() 创建多个文件夹，并且不需要保证它的上级文件夹存在。
     * @param folderPath
     */
    public static void createFiles(String folderPath){
        File dir = new File(folderPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    /**
     * 删除该目录并删除该目录下所有文件
     * @param path
     * @return
     */
    public static boolean deleteDir(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static void main(String[] args){


        System.out.print("时间戳："+ System.currentTimeMillis());
    }
}
