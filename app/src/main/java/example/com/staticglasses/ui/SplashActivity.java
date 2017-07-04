package example.com.staticglasses.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import example.com.staticglasses.R;
import example.com.staticglasses.manager.AppInitManager;
import example.com.staticglasses.util.handler.WeakHandler;
import example.com.staticglasses.util.helper.ResHelper;

public class SplashActivity extends BaseActivity {

    private WeakHandler mHandler;
    private Handler.Callback mCallback;
    private ImageView mImageView;
    private RelativeLayout mRootView;
    //是否已经跳转
    private boolean isSkiped;

    {
        this.mCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1 && !isSkiped) {
                    isSkiped = true;
                    cancelFullScreen();
                    mHandler.sendEmptyMessageDelayed(4, 1000);
                } else if (msg.what == 2) {
                    //checkPermission();
                } else if (msg.what == 3) {
                    finish();
                } else if (msg.what == 4) {
                    HistoryActivity.launch(SplashActivity.this);
                    mHandler.sendEmptyMessageDelayed(3, 1000);
                }
                return false;
            }
        };
        this.mHandler = new WeakHandler(this.mCallback);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1.解决安装后直接打开，home键切换到后台再启动重复出现闪屏页面的问题
        // http://stackoverflow.com/questions/2280361/app-always-starts-fresh-from-root-activity-instead-of-resuming-background-state
        if (!isTaskRoot()) {
            if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
                finish();
                return;
            }
        }
        setContentView(R.layout.act_splash);
        mRootView = (RelativeLayout) findViewById(R.id.welcome_view);
        //加载中间的图片icon
        mImageView = new ImageView(this);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageView.setImageResource(R.drawable.welcome);
        mImageView.setAlpha(0.f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) ResHelper.getDimen(R.dimen.splash_icon_width),
                (int) ResHelper.getDimen(R.dimen.splash_icon_height));
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        //底部高度
        params.bottomMargin = (int) ((ResHelper.getScreenHeight() - params.height) / 2.f);
        mRootView.addView(mImageView, params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != mImageView) {
            mImageView.animate()
                    .alpha(0.f)
                    .alphaBy(1.f)
                    .setDuration(800)
                    .start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkPermission();
//        } else {
        init(1000);
//        }
    }

    /*
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case 42: {
                    int i = 0;
                    for (; i < permissions.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            break;
                        }
                    }
                    if (permissions.length == i) {
                        init(800);
                    } else {
                        DialogHelper.create()
                                .title("权限提示")
                                .content("没有获取到必要权限，请进入设置界面授权之后进入")
                                .leftButton("取消")
                                .leftBtnClickListener(new DialogHelper.OnDialogClickListener() {
                                    @Override
                                    public void onClick(Dialog dialog, View view) {
                                        if (null != dialog && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    }
                                })
                                .rightButton("确定")
                                .rightBtnClickListener(new DialogHelper.OnDialogClickListener() {
                                    @Override
                                    public void onClick(Dialog dialog, View view) {
                                        if (null != dialog && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                        finish();
                                    }
                                })
                                .show();
                    }

                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }


        @TargetApi(Build.VERSION_CODES.M)
        private void checkPermission() {
            int write_storage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int camera = checkSelfPermission(Manifest.permission.CAMERA);
            int audioRecord = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            List<String> permissions = new ArrayList<String>();
            if (write_storage != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (camera != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (audioRecord != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 42);
            } else {
                init(1200);
            }
        }
    */
    private void init(long millis) {
        //初始化
        AppInitManager.getInstance().init();
        //延时
        mHandler.sendEmptyMessageDelayed(1, millis);
    }

    private void cancelFullScreen() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(params);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }


}