package example.com.staticglasses.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;

import example.com.staticglasses.R;
import example.com.staticglasses.ui.adapter.MyGlassAdapter;
import example.com.staticglasses.ui.view.MyListView;
import example.com.staticglasses.ui.view.RevealBackgroundView;
import example.com.staticglasses.util.handler.WeakHandler;

/**
 * @author fandong
 * @date 2016/9/23
 * @description
 */

public class MyGlassListActivity extends BaseActivity {
    //自定义的listView
    private MyListView mListView;
    //眼镜试戴的适配器
    private MyGlassAdapter adapter;

    private AbsListView.OnScrollListener mOnScrollListener;

    private RevealBackgroundView mRevealBackgroundView;

    private RevealBackgroundView.OnStateChangeListener mOnStateChangeListener;

    private MyListView.OnGestureListener mOnGestureListener;

    private String mSeqDir;
    private String mGlassDir;
    private Toolbar mToolbar;
    private WeakHandler mHandler;


    {
        this.mHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    adapter.loadCurrentGlass();
                }
                return false;
            }
        });
        this.mOnScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
                    mHandler.sendEmptyMessageDelayed(1, 500);
                    adapter.setIsScrolling(false);
                } else {
                    adapter.setLoaded(true);
                    mHandler.removeMessages(1);
                    adapter.setIsScrolling(true);
                }
                mListView.setIsFling(AbsListView.OnScrollListener.SCROLL_STATE_FLING == scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        };


        this.mOnStateChangeListener = new RevealBackgroundView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                if (RevealBackgroundView.STATE_FINISHED == state) {
                    mToolbar.setTranslationY(-40.f);
                    mListView.setTranslationY(100.f);
                    mToolbar.animate()
                            .alpha(1.f)
                            .translationY(0.f)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(300)
                            .start();
                    mListView.animate()
                            .alpha(1.f)
                            .translationY(0.f)
                            .setDuration(300)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();

                }
            }
        };
        this.mOnGestureListener = new MyListView.OnGestureListener() {
            private float lastDistanceX = 0.f;

            @Override
            public boolean onGesture(float distanceX, float distanceY, MotionEvent event) {
                if (distanceX > lastDistanceX || (lastDistanceX == 0.f && distanceX > 0)) {
                    adapter.preSeqImage();
                } else if (distanceX < lastDistanceX || (lastDistanceX == 0.f && distanceX < 0)) {
                    adapter.nextSeqImage();
                }
                lastDistanceX = distanceX;
                return true;
            }
        };
    }


    public static void launch(Context context, String seqDir,
                              String glassDir, int[] startingLocation) {
        Intent intent = new Intent(context, MyGlassListActivity.class);
        intent.putExtra("seqDir", seqDir);
        intent.putExtra("glassDir", glassDir);
        intent.putExtra("location", startingLocation);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list);
        mRevealBackgroundView = (RevealBackgroundView) findViewById(R.id.vRevealBackground);
        setupRevealBackground(savedInstanceState);
        Intent intent = getIntent();
        mSeqDir = intent.getStringExtra("seqDir");
        mGlassDir = intent.getStringExtra("glassDir");

        mListView = (MyListView) findViewById(R.id.listView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setAlpha(0.f);
        mListView.setAlpha(0.f);

        mListView.setOnScrollListener(mOnScrollListener);
        mListView.setOnGestureListener(mOnGestureListener);


        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar mActionBar = getSupportActionBar();
        if (null != mActionBar) {
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("列表");
        }

    }

    private void initAdapter() {
        adapter = new MyGlassAdapter(MyGlassListActivity.this, mSeqDir, mGlassDir);
        mListView.setAdapter(adapter);
        adapter.loadCurrentGlass();
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        mRevealBackgroundView.setFillPaintColor(0xFF16181a);
        mRevealBackgroundView.setOnStateChangeListener(mOnStateChangeListener);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra("location");
            mRevealBackgroundView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRevealBackgroundView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mRevealBackgroundView.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            mRevealBackgroundView.setToFinishedFrame();
        }
    }


    @Override
    protected void onResume() {

        if (!hasResumed()) {
            initAdapter();
        }
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {

        if (adapter != null) {
            adapter.onDestroy();
            adapter = null;
        }
        super.onDestroy();
    }

}
