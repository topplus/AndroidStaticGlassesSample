package example.com.staticglasses.manager;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import example.com.staticglasses.util.FileUtil;

public class HeadManager {
    private static HeadManager gHeadManager;
//    private final static String HEAD_MODEL_DIR = "head_models";

    private Context mContext;
    private String mHeadModelsDir;
    //    private int mLastNameIndex = 0;
    private List<String> mModels = new ArrayList<String>();
    private Comparator<String> mFileComparator = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            File lFile = new File(lhs);
            File rFile = new File(rhs);
            return (int) (lFile.lastModified() - rFile.lastModified());
        }
    };

    private HeadManager(Context context) {
        mHeadModelsDir = FileUtil.getVideoPath();
        File manageDir = new File(mHeadModelsDir);
        if (!manageDir.exists()) {
            manageDir.mkdirs();
        }
        mContext = context;
        scanModels(manageDir);
    }

    public static HeadManager getInstance(Context context) {
        if (gHeadManager == null) {
            synchronized (HeadManager.class) {
                if (gHeadManager == null)
                    gHeadManager = new HeadManager(context);
            }
        }
        return gHeadManager;
    }

    //扫描所有model文件
    private void scanModels(File manageDir) {
        File modelGirl = new File(FileUtil.getModelPath());
        if (modelGirl.exists()) {
            if (!mModels.contains(modelGirl.getAbsolutePath())) {
                mModels.add(0, modelGirl.getAbsolutePath());
            }
        }

        Log.e("Topplus", "modelGirl:" + modelGirl.getAbsolutePath());

        String[] files = manageDir.list();
        if (files == null || files.length <= 0) return;
        for (String filename : manageDir.list()) {
            if (!filename.equals(".") && !filename.equals("..")) {
                File dir = new File(manageDir.getAbsolutePath() + "/" + filename);
                if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
                    if (!mModels.contains(dir.getAbsolutePath())) {
                        mModels.add(dir.getAbsolutePath());
                    }
                }
            }
        }
        Collections.sort(mModels, mFileComparator);

    }

    /**
     * 保存对应的人脸模型到统一的管理目录
     *
     * @param modelDir 人脸模型目录
     */

    public void saveToHeadDir(String modelDir) {
        File srcFile = new File(modelDir);
        String nextHead = getNextHeadDir();
        File dstFile = new File(nextHead);
        try {
            FileUtil.copyDirectory(srcFile, dstFile);
            mModels.add(nextHead);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHeadDir(String headDir) {
        mModels.add(headDir);
    }

    public void addHeadDir(int position, String headDir) {
        mModels.add(position, headDir);
    }

    //获取所有的人脸模型文件list
    public List<String> getHeadModels() {
        return mModels;
    }

    //获取下一个用于放置人脸模型的目录
    public String getNextHeadDir() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);
        return mHeadModelsDir + "/" + (simpleDateFormat.format(new Date()));
    }

    //删除对应的人脸模型目录
    public void deleteHeadModel(String HeadDir) {
        mModels.remove(HeadDir);
        File headDir = new File(HeadDir);
        FileUtil.DeleteRecursive(headDir, true);
    }

    //获取中间人脸图片路径
    public String getMiddleJpegPath(String HeadDir) {
        String result = null;
        File dir = new File(HeadDir);
        if (dir.exists()) {
            FilenameFilter fileFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".jpg");
                }
            };
            List<String> jpegFiles = Arrays.asList(dir.list(fileFilter));
            if (!jpegFiles.isEmpty()) {
                result = HeadDir + "/" + jpegFiles.size() / 2 + ".jpg";
            }
        }
        return result;
    }
}
