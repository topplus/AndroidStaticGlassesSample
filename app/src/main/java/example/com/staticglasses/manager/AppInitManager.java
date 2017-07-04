package example.com.staticglasses.manager;

import android.content.res.AssetManager;
import android.os.AsyncTask;

import java.io.File;

import example.com.staticglasses.StaticApplication;
import example.com.staticglasses.util.FileUtil;
import topplus.com.commonutils.Library;
import topplus.com.commonutils.util.AssetsHelper;

/**
 * @author fandong
 * @date 2016/9/27
 * @description
 */

public class AppInitManager {

    private static AppInitManager sManager;

    private boolean isInitialized;

    private AppInitManager() {

    }

    public static AppInitManager getInstance() {
        if (sManager == null) {
            sManager = new AppInitManager();
        }
        return sManager;
    }

    //初始化
    public void init() {
        if (isInitialized || null == StaticApplication.gContext) return;
        isInitialized = true;
        Library.init(StaticApplication.gContext, "", "", false);
        //初始化文件
        initResource();
        FileUtil.mkSdcardVideoPath(StaticApplication.gContext);
    }

    private void initResource() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(getCopyTask());
    }

    private Runnable getCopyTask() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    File modelGirlPath = new File(FileUtil.getModelPath());
                    File gstPath = new File(FileUtil.getGstPath());
                    //1.复制model
                    if (!modelGirlPath.exists()) {
                        AssetsHelper.copyAssetDir(StaticApplication.gContext, "modelgirl", modelGirlPath.getAbsolutePath(), false);
                    }
                    //2.复制眼镜模型文件
                    if (gstPath.exists()) {
                        return;
                    }
                    if (gstPath.mkdirs()) {
                        AssetManager assetManager = StaticApplication.gContext.getAssets();
                        String[] allAssets = assetManager.list("");
                        for (String filename : allAssets) {
                            if (filename.endsWith(".gst")) {
                                AssetsHelper.copyAssetFile(StaticApplication.gContext, filename, FileUtil.getGstPath() + "/" + filename, false);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
