package example.com.staticglasses.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.topplus.staticglasses.ui.widget.GlassTextureView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import example.com.staticglasses.R;
import example.com.staticglasses.manager.ThreadTask;
import example.com.staticglasses.ui.MyDetailActivity;
import example.com.staticglasses.util.helper.DialogHelper;

/**
 * @author fandong
 * @date 2016/9/23
 * @description
 */

public class MyGlassAdapter extends BaseAdapter {
    private static final Object sLock = new Object();
    private Context mContext;
    //模特儿图片地址
    private String mPicturePath;
    //眼镜模型地址
    private String mGlassPath;
    private List<String> mGlassPaths;

    private int mCount;
    private int mIndex;

    private LinkedList<GlassTextureView> mGlassTextures;

    //是否初始化过
    private boolean isLoaded;

    private Dialog mDialog;
    private boolean mIsScrolling;


    {
        this.mIsScrolling = false;
        this.mCount = -1;
        this.mIndex = -1;
        this.mGlassTextures = new LinkedList<GlassTextureView>();
    }


    public MyGlassAdapter(Context context, String picturePath, String glassPath) {
        this.mContext = context;
        this.mPicturePath = picturePath;
        this.mGlassPath = glassPath;
        this.mGlassPaths = new ArrayList<String>();
        this.initGlassResource();
    }

    private void initGlassResource() {
        File glassDir = new File(this.mGlassPath);
        if (glassDir.exists()) {
            File[] files = glassDir.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".gst")) {
                    //模拟线上环境
                    mGlassPaths.add("http://" + file.getAbsolutePath());
                }
            }
        }
    }

    public void preSeqImage() {
        if (mCount > 0 && mIndex > 0) {
            mIndex--;
            for (GlassTextureView texture : mGlassTextures) {
                texture.showImageSync(mIndex);
            }
        }
    }

    public void nextSeqImage() {
        if (mCount > 0 && mIndex < mCount - 1) {
            mIndex++;
            for (GlassTextureView texture : mGlassTextures) {
                texture.showImageSync(mIndex);
            }
        }
    }

    public void loadCurrentGlass() {
        for (GlassTextureView texture : mGlassTextures) {
            loadCurrentClass(texture);
        }
    }


    @Override
    public int getCount() {
        return mGlassPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mGlassPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        boolean isModelGirl = mPicturePath.endsWith("modelgirl");
        if (convertView == null) {
            //1.初始化视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_list_item2, null);
            //2.初始化texture的触摸事件
            GlassTextureView glassTexture = (GlassTextureView) convertView.findViewById(R.id.glass_texture);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) glassTexture.getLayoutParams();
            params.height = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 3.f / 5);
            if (!mGlassTextures.contains(glassTexture)) {
                mGlassTextures.add(glassTexture);
            }
            glassTexture.setImageIndex(mIndex);
            //3.初始化ViewHolder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mGlassTexture = glassTexture;
            viewHolder.position = position;
            if (!isModelGirl) {
                viewHolder.mGlassTexture.loadPicture(mPicturePath, true);
            } else {
                viewHolder.mGlassTexture.loadPicture(mPicturePath);
            }
            convertView.setTag(viewHolder);
        }
        //4.得到总数和index
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.mGlassTexture.setTag(position);
        viewHolder.mGlassTexture.setMemoryFirst(true);
        if (mIndex > -1) {
            viewHolder.mGlassTexture.showImage(mIndex);
        }
        if (!isLoaded) {
            loadCurrentClass(viewHolder.mGlassTexture);
        }
        viewHolder.mGlassTexture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer pos = (Integer) v.getTag();
                if (pos >= 0 && pos < mGlassPaths.size()) {
                    MyDetailActivity.launch(mContext, getGlassPathByPosition(pos), mPicturePath, false);
                }
            }
        });
        viewHolder.position = position;
        return convertView;
    }

    //加载当前眼镜模型
    private void loadCurrentClass(GlassTextureView texture) {
        //1.得到当前的gstpath
        if (null == texture) {
            return;
        }
        new ThreadTask<Object, Void, String>() {
            private Integer p;

            @Override
            protected String doInBackground(Object... params) {
                this.p = (Integer) params[0];
                String gstPath = mGlassPaths.get(p);
                if (gstPath.startsWith("http://")) {
                    try {
                        Thread.sleep(2000);
                        //下载完成，保存文件
                        gstPath = gstPath.substring(7);
                        synchronized (sLock) {
                            mGlassPaths.remove(p.intValue());
                            mGlassPaths.add(p, gstPath);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return gstPath;
            }

            @Override
            protected void onPostExecute(String s) {
                GlassTextureView texture = getStaticGlassTexture(p);
                if (null != texture) {
                    checkCount(texture);
                    texture.setGlassPath(s);
                    if (mIsScrolling) {
                        return;
                    }
                    texture.loadCurrentGlass();
                }
            }
        }.execute(texture.getTag());
    }

    private String getGlassPathByPosition(int position) {
        String path = mGlassPaths.get(position);
        if (path.startsWith("http://")) {
            path = path.substring(7);
        }
        return path;
    }

    private void checkCount(GlassTextureView textureView) {
        if (null != textureView && mCount < 0) {
            mCount = textureView.getImageCount();
            mIndex = textureView.getImageIndex();
        }
    }


    private GlassTextureView getStaticGlassTexture(Integer position) {
        for (GlassTextureView texture : mGlassTextures) {
            if (null != texture.getTag() && position == ((Integer) texture.getTag()).intValue()) {
                return texture;
            }
        }
        return null;
    }

    public void setIsScrolling(boolean isScrolling) {
        this.mIsScrolling = isScrolling;
    }

    private void showTipDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = DialogHelper.create()
                .title("提示")
                .content("授权失败，请检查您的授权码！")
                .rightButton("我知道了")
                .rightBtnClickListener(new DialogHelper.OnDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog, View view) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                })
                .show();
    }

    public void onDestroy() {
        if (mGlassTextures != null && !mGlassTextures.isEmpty()) {
            for (GlassTextureView texture : mGlassTextures) {
                if (null != texture) {
                    texture.destroy();
                }
            }
            mGlassTextures.clear();
        }
    }

    public int getAdapterCount() {
        return 0;
    }

    public int getAdapterIndex() {
        return mIndex;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    private class ViewHolder {
        GlassTextureView mGlassTexture;
        int position;
    }
}
