package example.com.staticglasses.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import example.com.staticglasses.BuildConfig;
import example.com.staticglasses.StaticApplication;


/**
 * time: 15/6/7
 * description: 文件管理类
 *
 * @author fandong
 */
public class FileUtil {
    public static final int DIR_TYPE_HOME = 0x01;
    public static final int DIR_TYPE_CACHE = 0x02;
    public static final int DIR_TYPE_IMAGE = 0x03;
    public static final int DIR_TYPE_LOG = 0x04;
    public static final int DIR_TYPE_APK = 0x05;
    public static final int DIR_TYPE_DOWNLOAD = 0x06;
    public static final int DIR_TYPE_TEMP = 0x07;
    public static final int DIR_TYPE_CIRCLE = 0x08;
    public static final int DIR_TYPE_COPY_DB = 0x09;
    /* 默认最小需要的空间*/
    public static final long MIN_SPACE = 10 * 1024 * 1024;
    /*存放copy过来的db*/
    private static final String DIR_COPY_DB = "db";
    /* 该文件用来在图库中屏蔽本应用的图片.*/
    private static final String DIR_NO_MEDIA_FILE = ".nomedia";
    private static final String DIR_IMAGE = "image";
    private static final String DIR_CACHE = "cache";
    private static final String DIR_LOG = "log";
    private static final String DIR_APK = "apk";
    private static final String DIR_DOWNLOAD = "download";
    private static final String DIR_TEMP = "temp";
    public static String EXTERNAL_STORAGE;

    public static String getHomeDir(Context context) {
        if (BuildConfig.DEBUG) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/topplus/" + BuildConfig.APPLICATION_ID;
        }
        return context.getFilesDir().getAbsolutePath();
    }

    public static String getSdcardVideoPath(Context context) {
        if (BuildConfig.DEBUG) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/topplus/" + BuildConfig.APPLICATION_ID + "/temp/";
        }
        return context.getFilesDir().getAbsolutePath() + "/video";
    }

    public static void mkSdcardVideoPath(Context context) {
        File dir = new File(getSdcardVideoPath(context));
        if (!dir.exists() || dir.isDirectory()) {
            dir.mkdirs();
        }
    }

    public static String getPathByType(int type) {
        return getPathByType(StaticApplication.gContext, type);
    }

    public static String getPathByType(Context context, int type) {
        String dir = "/";
        StringBuilder filePath = new StringBuilder()
                .append(getHomeDir(context))
                .append("/");

        switch (type) {
            case DIR_TYPE_CACHE:
                filePath.append(DIR_CACHE);
                break;

            case DIR_TYPE_IMAGE:
                filePath.append(DIR_IMAGE);
                break;

            case DIR_TYPE_LOG:
                filePath.append(DIR_LOG);
                break;

            case DIR_TYPE_APK:
                filePath.append(DIR_APK);
                break;

            case DIR_TYPE_DOWNLOAD:
                filePath.append(DIR_DOWNLOAD);
                break;

            case DIR_TYPE_TEMP:
                filePath.append(DIR_TEMP);
                break;

            case DIR_TYPE_COPY_DB:
                filePath.append(DIR_COPY_DB);
                break;
            default:
                break;
        }

        File file = new File(filePath.toString());
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }

        if (file.exists()) {
            if (file.isDirectory()) {
                dir = file.getAbsolutePath();
            }
        } else {
            // 文件没创建成功，可能是sd卡不存在，但是还是把路径返回
            dir = filePath.toString();
        }

        return dir + "/";
    }

    //得到模特儿图的路径
    public static String getModelPath() {
        return getPathByType(StaticApplication.gContext, FileUtil.DIR_TYPE_CACHE) + "modelgirl";
    }

    public static String getGstPath() {
        return getPathByType(StaticApplication.gContext, FileUtil.DIR_TYPE_CACHE) + "gst";
    }

    public static String getVideoPath() {
        return getPathByType(StaticApplication.gContext, FileUtil.DIR_TYPE_DOWNLOAD) + "head_models";
    }

    /**
     * SdCard是否存在
     *
     * @return
     */
    public static boolean isSDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 判断存储空间是否足够,默认需要 {@link FileUtil#MIN_SPACE}
     *
     * @return
     */
    public static boolean hasEnoughSpace() {
        return hasEnoughSpace(MIN_SPACE);
    }

    /**
     * 判断存储空间是否足够
     *
     * @param needSize
     * @return
     */
    public static boolean hasEnoughSpace(float needSize) {
        if (isSDCardExist()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());

            long blockSize;
            long availCount;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = sf.getBlockSizeLong();
                availCount = sf.getAvailableBlocksLong();
            } else {
                blockSize = sf.getBlockSize();
                availCount = sf.getAvailableBlocks();
            }

            long restSize = availCount * blockSize;
            if (restSize > needSize) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delete file or folder.
     *
     * @param deleteFile
     * @return
     */
    public static boolean deleteFile(File deleteFile) {
        if (deleteFile != null) {
            if (!deleteFile.exists()) {
                return true;
            }
            if (deleteFile.isDirectory()) {
                // 处理目录
                File[] files = deleteFile.listFiles();
                //循环删除目录
                if (null != files) {
                    for (File file : files) {
                        deleteFile(file);
                    }
                }
                //删除目录自己
                return deleteFile.delete();
            } else {
                // 如果是文件，删除
                return deleteFile.delete();
            }
        }
        return false;
    }


    public static void inputStreamToFile(InputStream is, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);

            int bytesRead = 0;
            byte[] buffer = new byte[3072];

            while ((bytesRead = is.read(buffer, 0, 3072)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 避免图片放入到图库中（屏蔽其他软件扫描）.
     */
    public static void hideMediaFile() {
        File file = new File(DIR_NO_MEDIA_FILE);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件名字的前面部分，不包括文件名
     *
     * @param fileName e.g:2012.zip
     * @return 2012
     */
    public static String getFilePreName(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    /**
     * 解压zip文件
     *
     * @param zipFile
     * @param folderPath
     * @throws IOException
     */
    public static int upZipFile(File zipFile, String folderPath) throws IOException {
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                String dirstr = folderPath + File.separator + ze.getName();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                File f = new File(dirstr);
                f.mkdirs();
                continue;
            }
            OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        return 0;
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    private static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    substr = new String(substr.getBytes("8859_1"), "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
                ret = new File(ret, substr);
                if (!ret.exists()) {
                    ret.mkdirs();
                }
            }
            substr = dirs[dirs.length - 1];
            try {
                substr = new String(substr.getBytes("8859_1"), "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ret = new File(ret, substr);
            return ret;

        }
        return ret;
    }

    /**
     * 写入信息到文件
     *
     * @param file
     * @param content
     */
    public static void writeFile(File file, byte[] content) throws IOException {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IOException("not crete file=" + file.getAbsolutePath());
            }
        }
        FileOutputStream fileOutputStream = null;
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(content);
            fileOutputStream = new FileOutputStream(file, false);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = bis.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param file
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("not crete file=" + file.getAbsolutePath());
        }
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream(64);
            int length = 0;
            byte[] buffer = new byte[1024];
            while ((length = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toByteArray();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
        }
    }

    public static boolean copy(File source, File target) {
        if (source == null || target == null || !source.exists() || source.length() < 100) {
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(target);
            FileInputStream fis = new FileInputStream(source);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public static void DeleteRecursive(File fileOrDirectory, boolean deleteSelf) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child, true);
        if (deleteSelf)
            fileOrDirectory.delete();
    }
}
