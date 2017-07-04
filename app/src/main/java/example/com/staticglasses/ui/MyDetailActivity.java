package example.com.staticglasses.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.topplus.staticglasses.ui.widget.GlassTextureView;

import example.com.staticglasses.R;
import example.com.staticglasses.ui.adapter.GlassIconAdapter;
import example.com.staticglasses.ui.listener.MoveGestureDetector;
import example.com.staticglasses.util.FileUtil;
import example.com.staticglasses.util.handler.WeakHandler;
import example.com.staticglasses.util.helper.DialogHelper;
import example.com.staticglasses.util.helper.ResHelper;

/**
 * @author fandong
 * @date 2016/9/27
 * @description
 */

public class MyDetailActivity extends BaseActivity {
    public static final int WHAT_LOAD_GLASS = 0;

    public static final String HEAD_PATH = "head_path";
    public static final String GLASS_DIR = "glass_dir";
    public static final String GLASS_PATH = "glass_path";
    public static final String SHOW_LIST = "show_list";

    //texture的父控件
    private RelativeLayout mTextureLayout;

    private GlassTextureView mStaticGlassTexture;
    private RecyclerView mRecyclerView;

    private MoveGestureDetector mMoveDetector;

    //生成的序列图的sd卡位置
    private String mHeadPath;

    private String mGlassDir;

    //眼镜在鼻梁上的角度
    private float mGlassNosePadPos = 0.2f;
    //眼镜在鼻梁上的角度
    private float mGlassAngle = 0.5f;
    //镜腿虚化的位置
    private float mGlassFeatherDistance = 0.9f;
    //眼镜的相对大小
    private float mGlassScale = 0.5f;

    private int mImageIndex = -1;

    //上一次选择的位置
    private int lastSelected;
    //RecyclerView适配器
    private GlassIconAdapter mGlassIconAdapter;
    //ItemClick事件
    private GlassIconAdapter.OnItemClickListener mOnItemClickListener;
    private RecyclerView.OnScrollListener mOnScrollListener;

    private Dialog mDialog;
    //
    private String mCurrentGlassDir;

    private WeakHandler mHandler;

    private boolean mShowList;
    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mMoveDetector.onTouchEvent(event);
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP
                    && mShowList) {
                saveMaa();
            }
            return true;
        }
    };

    {
        this.mHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == WHAT_LOAD_GLASS) {
                    String glassPath = (String) msg.obj;
                    mStaticGlassTexture.loadGlass(glassPath);
                }
                return false;
            }
        });
        this.lastSelected = -1;
        this.mGlassDir = FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "gst";
        this.mCurrentGlassDir = mGlassDir + "/1.gst";

        this.mOnItemClickListener = new GlassIconAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != lastSelected) {
                    lastSelected = position;
                    //1.改变背景颜色
                    for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                        View v = mRecyclerView.getChildAt(i).findViewById(R.id.iv_ripple_view);
                        if (v != null) {
                            v.setBackgroundColor(0xFFE1E1E1);
                        }
                    }
                    view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    //2.RecyclerView侧滑动
                    int left = ((ViewGroup) view.getParent()).getLeft();
                    int distance = (int) (left - ResHelper.getScreenWidth() / 2.f + ResHelper.getDimen(R.dimen.recycler_item_size) / 2.f);
                    mRecyclerView.smoothScrollBy(distance, 0);
                    //3.加载眼镜
                    mCurrentGlassDir = mGlassDir + "/" + (position + 1) + ".gst";
                    int result = mStaticGlassTexture.loadGlass(mCurrentGlassDir);
                    if (result == 4) {
                        showTipDialog();
                    }
                }
            }
        };
        this.mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mGlassIconAdapter.setLoaded(true);
                }
            }
        };
    }

    public static void launch(Context context, String glassDir, String headPath) {
        Intent intent = new Intent(context, MyDetailActivity.class);
        intent.putExtra(HEAD_PATH, headPath);
        intent.putExtra(GLASS_DIR, glassDir);
        context.startActivity(intent);
    }

    public static void launch(Context context, String glassPath, String headPath, boolean showList) {
        Intent intent = new Intent(context, MyDetailActivity.class);
        intent.putExtra(HEAD_PATH, headPath);
        intent.putExtra(GLASS_PATH, glassPath);
        intent.putExtra(SHOW_LIST, showList);
        context.startActivity(intent);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1.初始化，加载本地库
        setContentView(R.layout.act_detail);
        //2.解析intent
        parseIntent(getIntent());
        //3.处理标题
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar mActionBar = getSupportActionBar();
        if (null != mActionBar) {
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("详情");
        }
        //4.视图
        mTextureLayout = (RelativeLayout) findViewById(R.id.texture_layout);
        mMoveDetector = new MoveGestureDetector(getApplicationContext(), new MoveListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasResumed()) {
            //添加static__glass控件
            addStaticGlassTexture();
            mRecyclerView = (RecyclerView) findViewById(R.id.glass_list);
            if (mShowList) {
                //选择器
                this.mRecyclerView.addOnScrollListener(mOnScrollListener);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                this.mRecyclerView.setLayoutManager(layoutManager);
                this.mGlassIconAdapter = new GlassIconAdapter(this);
                this.mGlassIconAdapter.setOnItemClickListener(mOnItemClickListener);
                mRecyclerView.setAdapter(mGlassIconAdapter);
            } else {
                mRecyclerView.setVisibility(View.GONE);
            }
        }

    }

    //初始化staticglasstexture
    private void addStaticGlassTexture() {
        mMoveDetector = new MoveGestureDetector(getApplicationContext(), new MoveListener());
        mStaticGlassTexture = new GlassTextureView(this);
        mStaticGlassTexture.setOnTouchListener(mTouchListener);
        mStaticGlassTexture.setMemoryFirst(true);
        int size = ResHelper.getScreenWidth();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mTextureLayout.removeAllViews();
        mTextureLayout.addView(mStaticGlassTexture, params);
        //加载序列图
        loadSequence();
        //首先显示序列图
        if (!TextUtils.isEmpty(mCurrentGlassDir)) {
            Message message = Message.obtain();
            message.what = WHAT_LOAD_GLASS;
            message.obj = mCurrentGlassDir;
            mHandler.sendMessageDelayed(message, 400);
        }
    }

    private void loadSequence() {
        if (null != mStaticGlassTexture) {
            if (mHeadPath.endsWith("modelgirl")) {
                mStaticGlassTexture.loadPicture(mHeadPath);
            } else {
                mStaticGlassTexture.loadPicture(mHeadPath, true);
                mGlassNosePadPos = mStaticGlassTexture.getGlassNosePadPos();
                mGlassAngle = mStaticGlassTexture.getGlassAngle();
                mGlassFeatherDistance = mStaticGlassTexture.getGlassFeatherDistance();
                mGlassScale = mStaticGlassTexture.getGlassScale();
            }
        }
    }

    //解析intent
    private void parseIntent(Intent intent) {
        //1.得到头像的路径
        this.mHeadPath = intent.getStringExtra(HEAD_PATH);
        //2.得到眼镜模型的路径
        this.mGlassDir = intent.getStringExtra(GLASS_DIR);
        String currentGlassDir = intent.getStringExtra(GLASS_PATH);
        if (!TextUtils.isEmpty(currentGlassDir)) {
            this.mCurrentGlassDir = currentGlassDir;
        }
        this.mShowList = intent.getBooleanExtra(SHOW_LIST, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mShowList) {
            getMenuInflater().inflate(R.menu.detail_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_list) {
            int[] startingLocation = new int[2];
            startingLocation[1] = (int) (ResHelper.getStatusBarHeight() + ResHelper.getDimen(R.dimen.title_bar_height) / 2.f);
            startingLocation[0] = (int) (ResHelper.getScreenWidth() - ResHelper.getDimen(R.dimen.title_bar_height) / 2.f);
            MyGlassListActivity.launch(this, mHeadPath, mGlassDir, startingLocation);
            overridePendingTransition(0, 0);
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void saveMaa() {
        if (mStaticGlassTexture != null) {
            if (mHeadPath != null
                    && !mHeadPath.equals(FileUtil.getPathByType(FileUtil.DIR_TYPE_CACHE) + "modelgirl")) {
                mStaticGlassTexture.saveMaa();
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (null != mStaticGlassTexture) {
            mStaticGlassTexture.destroy();
            mStaticGlassTexture = null;
        }
        super.onDestroy();
        System.gc();
    }

    private class MoveListener implements MoveGestureDetector.MoveListener {
        public boolean onScroll(int fingerCount, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (fingerCount == 1) {
                    if (distanceX < 0) {
                        mStaticGlassTexture.preSeqImageSync();
                        mImageIndex = mStaticGlassTexture.getImageIndex();

                    } else if (distanceX > 0) {
                        mStaticGlassTexture.nextSeqImageSync();
                        mImageIndex = mStaticGlassTexture.getImageIndex();
                    }
                } else if (fingerCount == 2) {
                    if (distanceX > 0) {
                        mGlassFeatherDistance += 0.02f;
                        if (mGlassFeatherDistance > 1.0f) {
                            mGlassFeatherDistance = 1.0f;
                        }
                        mStaticGlassTexture.updateFeatherDistance(mGlassFeatherDistance);

                    } else if (distanceX < 0) {
                        mGlassFeatherDistance -= 0.02f;
                        if (mGlassFeatherDistance < 0f) {
                            mGlassFeatherDistance = 0f;
                        }
                        mStaticGlassTexture.updateFeatherDistance(mGlassFeatherDistance);

                    }
                }
            } else if (Math.abs(distanceY) > Math.abs(distanceX)) {
                if (fingerCount == 1) {
                    if (distanceY > 0) {
                        mGlassNosePadPos += 0.02f;
                        if (mGlassNosePadPos > 1.0f) {
                            mGlassNosePadPos = 1.0f;
                        }
                        mStaticGlassTexture.updateNosePadPos(mGlassNosePadPos);

                    } else if (distanceY < 0) {
                        mGlassNosePadPos -= 0.02f;
                        if (mGlassNosePadPos < 0f) {
                            mGlassNosePadPos = 0f;
                        }
                        mStaticGlassTexture.updateNosePadPos(mGlassNosePadPos);

                    }
                } else if (fingerCount == 2) {
                    if (distanceY < 0) {
                        mGlassAngle += 0.02f;
                        if (mGlassAngle > 1.0f) {
                            mGlassAngle = 1.0f;
                        }
                        mStaticGlassTexture.updateVerticalAngle(mGlassAngle);

                    } else if (distanceY > 0) {
                        mGlassAngle -= 0.02f;
                        if (mGlassAngle < 0f) {
                            mGlassAngle = 0f;
                        }
                        mStaticGlassTexture.updateVerticalAngle(mGlassAngle);

                    }
                }
            }
            return true;
        }

        @Override
        public boolean onScale(float factor) {
            mGlassScale *= factor;
            mGlassScale = Math.max(0.001f, Math.min(mGlassScale, 1.0f));
            mStaticGlassTexture.updateModelScale(mGlassScale);
            return true;
        }
    }    //监控和处理滑屏事件
}
