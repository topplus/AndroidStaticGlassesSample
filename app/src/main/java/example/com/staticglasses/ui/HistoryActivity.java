package example.com.staticglasses.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.topplus.staticglasses.SequenceMaker;

import java.io.File;

import example.com.staticglasses.BuildConfig;
import example.com.staticglasses.R;
import example.com.staticglasses.manager.HeadManager;
import example.com.staticglasses.ui.adapter.HistoryAdapter;
import example.com.staticglasses.ui.view.TToast;
import example.com.staticglasses.util.FileUtil;
import example.com.staticglasses.util.PixelUtil;
import example.com.staticglasses.util.helper.DialogHelper;
import example.com.staticglasses.util.helper.ResHelper;

/**
 * @author fandong
 * @date 2016/9/27
 * @description
 */

public class HistoryActivity extends BaseActivity {

    private Toolbar mToolbar;
    private SwipeMenuListView mHeadListView;
    private HistoryAdapter mHistoryAdapter;
    private FloatingActionButton mFab;
    private FloatingActionButton mFabVideo;

    private View.OnClickListener mOnClickListener;


    {
        this.mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.fab) {
                    //位置
                    int[] location = new int[2];
                    location[0] = (int) (ResHelper.getScreenWidth() / 2.f);
                    location[1] = (int) (ResHelper.getScreenHeight() - 50);
                    VideoRecordActivity.launch(HistoryActivity.this, 0, location);
                    overridePendingTransition(0, 0);
                } else if (v.getId() == R.id.fab_video) {
                    VideoListActivity.launch(HistoryActivity.this);
                }

            }
        };
    }

    //启动
    public static void launch(Context context) {
        Intent intent = new Intent(context, HistoryActivity.class);
        context.startActivity(intent);
    }

    protected LayoutAnimationController getAnimationController() {
        int duration = 500;
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.8f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_history);
        //初始化标题
        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        this.mHeadListView = (SwipeMenuListView) findViewById(R.id.history_list);
        this.mHeadListView.setLayoutAnimation(getAnimationController());
        this.mHistoryAdapter = new HistoryAdapter(this);
        this.mHeadListView.setAdapter(this.mHistoryAdapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                openItem.setBackground(new ColorDrawable(0x00000000));
                openItem.setWidth((int) PixelUtil.dp2px(60));
                openItem.setIcon(android.R.drawable.ic_menu_delete);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);
            }
        };
        mHeadListView.setMenuCreator(creator);
        mHeadListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (0 == position) {
                    TToast.show("模特儿序列图不能被删除！", HistoryActivity.this);
                    mHeadListView.setAdapter(mHistoryAdapter);
                } else {
                    mHistoryAdapter.deleteHead(position);
                    mHistoryAdapter.notifyDataSetChanged();
                }

                return false;
            }
        });

        mHeadListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mHeadListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyDetailActivity.launch(HistoryActivity.this, FileUtil.getGstPath(), mHistoryAdapter.getHeadPath(position));
                /*
                int[] startingLocation = new int[2];
                startingLocation[1] = (int) (ResHelper.getStatusBarHeight() + ResHelper.getDimen(R.dimen.title_bar_height) / 2.f);
                startingLocation[0] = (int) (ResHelper.getScreenWidth() - ResHelper.getDimen(R.dimen.title_bar_height) / 2.f);
                MyGlassListActivity.launch(HistoryActivity.this, mHistoryAdapter.getHeadPath(position), FileUtil.getGstPath(), startingLocation);
                overridePendingTransition(0, 0);
                */
//                DetailActivity.launch(HistoryActivity.this, FileUtil.getGstPath(), mHistoryAdapter.getHeadPath(position));
            }
        });

        this.mFab = (FloatingActionButton) findViewById(R.id.fab);
        this.mFab.setOnClickListener(mOnClickListener);
        if (BuildConfig.DEBUG) {
            this.mFabVideo = (FloatingActionButton) findViewById(R.id.fab_video);
            this.mFabVideo.setOnClickListener(mOnClickListener);
            this.mFabVideo.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onLazyLoad() {
        this.mFab.setScaleX(0.f);
        this.mFab.setScaleY(0.f);
        this.mFab.setAlpha(1.f);
        this.mFab.animate()
                .scaleY(1.f)
                .scaleX(1.f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        if (BuildConfig.DEBUG) {
            this.mFabVideo.setScaleX(0.f);
            this.mFabVideo.setScaleY(0.f);
            this.mFabVideo.setAlpha(1.f);
            this.mFabVideo.animate()
                    .scaleY(1.f)
                    .scaleX(1.f)
                    .setDuration(400)
                    .setStartDelay(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (0 == resultCode && null != data) {
            String videoPath = data.getStringExtra("videoPath");
            if (!TextUtils.isEmpty(videoPath)) {
                float focus = data.getFloatExtra("focus", 22);
                executeSequenceMakeTask(videoPath, focus);
            }
        }
    }

    //生成序列图
    private void executeSequenceMakeTask(final String videoPath, final float focus) {
        new AsyncTask<Void, Void, Integer>() {
            private String path;
            private Dialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = DialogHelper.create(DialogHelper.TYPE_PROGRESS)
                        .activity(HistoryActivity.this)
                        .progressText("正在生成序列图，请稍候...")
                        .show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                float[] magicParams = new float[4];
                //控制磨皮效果,值越小磨皮效果越明显
                magicParams[0] = 0.33f;
                //对人脸增亮
                magicParams[1] = 0.63f;
                //滤色、柔光等混合效果
                magicParams[2] = 0.4f;
                //调节饱和度
                magicParams[3] = 0.35f;
                path = HeadManager.getInstance(HistoryActivity.this).getNextHeadDir();
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                SequenceMaker.setBeautifyParams(magicParams);
                //sample当中关闭镜像
                SequenceMaker.setMirror(false);
                return SequenceMaker.makeSequence(videoPath, path, focus);
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (null != dialog && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (result == 0) {
                    HeadManager.getInstance(HistoryActivity.this).addHeadDir(path);
                    mHistoryAdapter.refreshDataSetChanged();
                } else {
                    File file = new File(path);
                    FileUtil.deleteFile(file);
                    DialogHelper.create()
                            .activity(HistoryActivity.this)
                            .title("生成序列图")
                            .content("未采集到人脸，请重新录制")
                            .rightButton("确定")
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
            }
        }.execute();
    }
}
