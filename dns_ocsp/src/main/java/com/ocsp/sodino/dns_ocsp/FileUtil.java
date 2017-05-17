package com.ocsp.sodino.dns_ocsp;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sodino on 2017/5/11.
 */

public class FileUtil {

    public static void write(@NonNull String line, @NonNull String folderPath, @NonNull String fileName){
        byte[] data = line.getBytes();
        writeFileToSDCard(data, folderPath, fileName, true, true);
    }

     /** 此方法为android程序写入sd文件文件，用到了android-annotation的支持库@
     *
             * @param buffer   写入文件的内容
     * @param folder   保存文件的文件夹名称,如log；可为null，默认保存在sd卡根目录
     * @param fileName 文件名称，默认app_log.txt
     * @param append   是否追加写入，true为追加写入，false为重写文件
     * @param autoLine 针对追加模式，true为增加时换行，false为增加时不换行
     */
    public static void writeFileToSDCard(@NonNull final byte[] buffer, @Nullable final String folder,
                                                      @Nullable final String fileName, final boolean append, final boolean autoLine) {

        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        String folderPath = "";
        if (sdCardExist) {
            //TextUtils为android自带的帮助类
            if (TextUtils.isEmpty(folder)) {
                //如果folder为空，则直接保存在sd卡的根目录
                folderPath = Environment.getExternalStorageDirectory()
                        + File.separator;
            } else {
                folderPath = Environment.getExternalStorageDirectory()
                        + File.separator + folder + File.separator;
            }
        } else {
            return;
        }

        File fileDir = new File(folderPath);
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                return;
            }
        }
        File file;
        //判断文件名是否为空
        if (TextUtils.isEmpty(fileName)) {
            file = new File(folderPath + "app_log.txt");
        } else {
            file = new File(folderPath + fileName);
        }
        RandomAccessFile raf = null;
        FileOutputStream out = null;
        try {
            if (append) {
                //如果为追加则在原来的基础上继续写文件
                raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(buffer);
                if (autoLine) {
                    raf.write("\n".getBytes());
                }
            } else {
                //重写文件，覆盖掉原来的数据
                out = new FileOutputStream(file);
                out.write(buffer);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
