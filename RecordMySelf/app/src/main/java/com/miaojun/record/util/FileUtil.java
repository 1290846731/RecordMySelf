package com.miaojun.record.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具
 * Created by LvMeng on 16/5/23.
 */
public class FileUtil {
    private FileListener fileListener;

    /**
     * 判断手机SDCard是否可以
     *
     * @return true可用;false 不可用
     */
    public boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public File getCacheDir(Context context) {
        return externalMemoryAvailable() ? context.getExternalCacheDir() : context.getCacheDir();
    }

    /**
     * 删除文件
     *
     * @param file File
     */
    public void deleteFile(File file) {
        //判断文件是否存在
        if (file == null || !file.exists()) {
            return;
        }
        //如果是目录则递归计算其内容的总大小
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                return;
            }
            for (File f : children) {
                if (f.isDirectory()) {
                    deleteFile(f);
                }
                if (!f.delete()) {
                    f.deleteOnExit();
                }
            }
        } else {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    /**
     * 获取文件夹中文件的体积
     *
     * @param file 文件夹Path
     * @return 文件大小
     */
    public long getDirSize(File file) {
        //判断文件是否存在
        if (file != null && file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {
                return file.length();
            }
        } else {
            return 0;
        }
    }

    /**
     * 复制单个文件
     *
     * @param sourcePath 源文件
     * @param newPath    目标文件
     */
    public void copyFile(String sourcePath, String newPath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourcePath);
            copyFile(inputStream, newPath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制单个文件
     */
    public void copyFile(InputStream inStream, String newPath) {
        FileOutputStream outputStream = null;
        try {
            int length = 0;
            outputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            while ((length = inStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从Assets中复制文件
     *
     * @param context    Context
     * @param assetsPath Assets 中的位置
     * @param targetPath 目标位置
     * @param targetPath 目标位置
     */
    public void copyFileFromAssets(Context context, String assetsPath, String targetPath) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetsPath);
            copyFile(inputStream, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除指定文件夹下所有文件
     * @param root
     */
    public void deleteAll(File root){
        File files[] = root.listFiles();
        if (files != null && files.length != 0)
            for(int i = 0; i <= files.length; i++){
                if(files.length != 0 && i == files.length && fileListener != null){
                    fileListener.onListener();
                    return;
                }
                if (files[i].isDirectory()) { // 判断是否为文件夹
                    deleteAll(files[i]);
                    try {
                        files[i].delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (files[i].exists()) { // 判断是否存在
                        deleteAll(files[i]);
                        try {
                            files[i].delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
//            for (File f : files) {
//                if (f.isDirectory()) { // 判断是否为文件夹
//                    deleteAll(f);
//                    try {
//                        f.delete();
//                    } catch (Exception e) {
//                    }
//                } else {
//                    if (f.exists()) { // 判断是否存在
//                        deleteAll(f);
//                        try {
//                            f.delete();
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            }
    }

    public void setFileListener(FileListener fileListener) {
        this.fileListener = fileListener;
    }

    /**
     * fileListenenr
     */
    public interface FileListener{
        void onListener();
    }
}
