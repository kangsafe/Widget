package com.ks.plugin.widget.launcher;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SDCardUtils {

    public static File getImageDir(Context context){
        return new File(getSDCardPath());
    }

    /**
     * 判断 SDCard 是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable()
    {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

    }

    /**
     * 获取 SD 卡路径
     *
     * @return
     */
    public static String getSDCardPath()
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator;
    }

    /**
     * 获取 SD 卡的剩余容量, 单位 byte
     *
     * @return
     */
    public static long getSDCardAllSize()
    {
        if (isSDCardEnable())
        {
            StatFs stat = new StatFs(getSDCardPath());
            // 获取空闲的数据块的数量
            long availableBlocks = (long) stat.getAvailableBlocks() - 4;
            // 获取单个数据块的大小（byte）
            long freeBlocks = stat.getAvailableBlocks();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }

    /**
     * 获取指定路径所在空间的剩余可用容量字节数，单位byte
     *
     * @param filePath
     * @return 容量字节 SDCard可用空间，内部存储可用空间
     */
    public static long getFreeBytes(String filePath)
    {
        // 如果是sd卡的下的路径，则获取sd卡可用容量
        if (filePath.startsWith(getSDCardPath()))
        {
            filePath = getSDCardPath();
        } else
        {// 如果是内部存储的路径，则获取内存存储的可用容量
            filePath = Environment.getDataDirectory().getAbsolutePath();
        }
        StatFs stat = new StatFs(filePath);
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }

    /**
     * 获取系统存储路径
     *
     * @return
     */
    public static String getRootDirectoryPath()
    {
        return Environment.getRootDirectory().getAbsolutePath();
    }

    /**
     * Check the SD card
     *
     * @return
     */
    public static boolean checkSDCardAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    /**
     * Check if the file is exists
     *
     * @param filePath
     * @param fileName
     * @return
     */

    public static boolean isFileExistsInSDCard(String filePath, String fileName) {

        boolean flag = false;

        if (checkSDCardAvailable()) {

            File file = new File(filePath, fileName);

            if (file.exists()) {

                flag = true;

            }

        }

        return flag;

    }


    /**
     * Write file to SD card
     *
     * @param filePath
     * @param filename
     * @param content
     * @return
     * @throws Exception
     */

    public static boolean saveFileToSDCard(String filePath, String filename, String content)

            throws Exception {

        boolean flag = false;

        if (checkSDCardAvailable()) {

            File dir = new File(filePath);

            if (!dir.exists()) {

                dir.mkdir();

            }

            File file = new File(filePath, filename);

            FileOutputStream outStream = new FileOutputStream(file);

            outStream.write(content.getBytes());

            outStream.close();

            flag = true;

        }

        return flag;

    }


    /**
     * Read file as stream from SD card
     *
     * @param fileName String PATH =
     *                 <p/>
     *                 Environment.getExternalStorageDirectory().getAbsolutePath() +
     *                 <p/>
     *                 "/dirName";
     * @return
     */

    public static byte[] readFileFromSDCard(String filePath, String fileName) {

        byte[] buffer = null;

        try {

            if (checkSDCardAvailable()) {

                String filePaht = filePath + "/" + fileName;

                FileInputStream fin = new FileInputStream(filePaht);

                int length = fin.available();

                buffer = new byte[length];

                fin.read(buffer);

                fin.close();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return buffer;

    }


    /**
     * Delete file
     *
     * @param filePath
     * @param fileName filePath =
     *                 <p/>
     *                 android.os.Environment.getExternalStorageDirectory().getPath()
     * @return
     */

    public static boolean deleteSDFile(String filePath, String fileName) {

        File file = new File(filePath + "/" + fileName);

        if (file == null || !file.exists() || file.isDirectory())

            return false;

        return file.delete();

    }

}