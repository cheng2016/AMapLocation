package com.wecare.app.util;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.wecare.app.App;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUtils {

    /**
     * 获取路径下的所有文件/文件夹
     * @param directoryPath 需要遍历的文件夹路径
     * @param isAddDirectory 是否将子文件夹的路径也添加到list集合中
     * @return
     */
    public static List<String> getAllFile(String directoryPath, boolean isAddDirectory) {
        List<String> list = new ArrayList<>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if(isAddDirectory){
                    list.add(file.getAbsolutePath());
                }
                list.addAll(getAllFile(file.getAbsolutePath(),isAddDirectory));
            } else {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }

    /**
     * 获取目录下所有视频的路径
     * @param strPath
     * @return
     */
    public static List<String> getImagePathList(String strPath) {
        List<String> list = new ArrayList<>();
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getImagePathList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("jpg")) { // 判断文件名是否以.avi结尾
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    list.add(files[i].getAbsolutePath());
                } else {
                    continue;
                }
            }

        }
        return list;
    }

    /**
     * 获取目录下所有图片的路径
     * @param strPath
     * @return
     */
    public static List<File> getImageFileList(String strPath) {
        List<File> filelist = new ArrayList<>();
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getVideoFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("jpg")) { // 判断文件名是否以.avi结尾
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    filelist.add(files[i]);
                } else {
                    continue;
                }
            }

        }
        return filelist;
    }

    /**
     * 获取目录下所有视频的路径
     * @param strPath
     * @return
     */
    public static List<File> getVideoFileList(String strPath) {
        List<File> filelist = new ArrayList<>();
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getVideoFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("mp4")) { // 判断文件名是否以.avi结尾
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    filelist.add(files[i]);
                } else {
                    continue;
                }
            }

        }
        return filelist;
    }

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

    /**
     * Create a random file based on mimeType.
     *
     * @param file file.
     *
     * @return file object.
     */
    public static File createRandomFile(MultipartFile file) {
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(file.getContentType().toString());
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeMap.getFileExtensionFromUrl(file.getFilename());
        }
        String uuid = UUID.randomUUID().toString();
        return new File(App.getInstance().getAppCacheDir(), uuid + "." + extension);
    }

    /**
     * SD is available.
     *
     * @return true, otherwise is false.
     */
    public static boolean storageAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sd.canWrite();
        } else {
            return false;
        }
    }

    public static void main(String[] args){


        System.out.print("时间戳："+ System.currentTimeMillis());
    }
}
