package example.com.staticglasses.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import example.com.staticglasses.R;
import example.com.staticglasses.gupimage.MagicBeautyFilter;
import example.com.staticglasses.gupimage.MagicCameraDisplay;
import example.com.staticglasses.ui.listener.DefaultAnimatorListener;
import example.com.staticglasses.ui.view.RevealBackgroundView;
import example.com.staticglasses.ui.view.TToast;
import example.com.staticglasses.ui.view.VideoRecord;
import example.com.staticglasses.util.FileUtil;
import example.com.staticglasses.util.handler.WeakHandler;
import example.com.staticglasses.util.helper.ResHelper;

public class VideoRecordActivity extends BaseActivity implements View.OnClickListener {

    private final static float MOVE_RATIO = 0.35f;
    private final static long MOVE_DURATION = 14000;
    private VideoRecord mVideoRecord;
    private TextView mNoteText;
    private ImageView mMoveLine;
    private ImageView mLeftLine, mRightLine, mCenterLine;
    private ImageView mHeadView;
    private ObjectAnimator mMoveAnimtor;//mCenterToLeft,mLeftToCenter,mCenterToRight,mRightToCenter;
    private String mTempDir;
    //本次录制的视频的名称
    private String mVideoName;
    private FloatingActionButton mRecordBtn;
    private SensorManager mSensorManager;
    //用户是否垂直拿着手机
    private Sensor mAccelerSensor;
    private float[] mAccelerData;
    private Sensor mMagneticSensor;
    private float[] mMagneticData;
    private Sensor mGravitySensor;
    private float[] mGravityData;
    //姿态角数据，由mMagneticData和mAccelerSensor计算出来
    private float[] mOrientData;
    private boolean mRecording;
    private MediaPlayer mMediaPlayer;

    private float[] mMagicParams;
    private MagicBeautyFilter mMagicBeautyFilter;
    private MagicCameraDisplay mMagicCameraDisplay;
    private RelativeLayout mContent;
    private RevealBackgroundView mRevealBackgroundView;
    private RevealBackgroundView.OnStateChangeListener mOnStateChangeListener;

    private WeakHandler mHandler;
    private boolean isHeadViewValidate;
    //传感器数据接收
    private SensorEventListener mSensorEventListener;

    {
        this.mOrientData = new float[3];
        this.mMagicParams = new float[4];
        this.mOnStateChangeListener = new RevealBackgroundView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                if (RevealBackgroundView.STATE_FINISHED == state) {
//                    if (hasResumed() && mMagicCameraDisplay != null) {
//                        mMagicCameraDisplay.onResume();
//                    }
                    loadContent();
                }
            }
        };
        this.mHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        float focusL = mVideoRecord.getFocusLength();
                        if (focusL > 0) {
                            Intent intent = new Intent();
                            intent.putExtra("videoPath", mTempDir + mVideoName);
                            intent.putExtra("focus", mVideoRecord.getFocusLength());
                            VideoRecordActivity.this.setResult(0, intent);
                            VideoRecordActivity.this.finish();
                        } else {
                            mHandler.sendEmptyMessageDelayed(1, 100);
                        }
                        break;
                    case 2:
                        if (!isHeadViewValidate) break;
                        mNoteText.setText(example.com.staticglasses.R.string.page6_note_step0);
                        mRecordBtn.setEnabled(true);
                        mHeadView.setBackgroundResource(R.drawable.green_rect);
                        break;
                    case 3:
                        if (!isHeadViewValidate) break;
                        mNoteText.setText(example.com.staticglasses.R.string.page6_note_step_1);
                        mRecordBtn.setEnabled(false);
                        mHeadView.setBackgroundResource(R.drawable.red_rect);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        this.mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mAccelerData = event.values.clone();
                } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mMagneticData = event.values.clone();
                } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                    mGravityData = event.values.clone();
                }
                if (mGravityData != null && mMagneticData != null) {
                    float I[] = new float[9];
                    float R[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravityData, mMagneticData);
                    if (success) {
                        SensorManager.getOrientation(R, mOrientData);
                        if (-1.60f < mOrientData[1] && mOrientData[1] < -1.40f) {
                            if (!mRecording && !mRecordBtn.isEnabled()) {
                                mHandler.sendEmptyMessage(2);
                            }
                        } else {
                            if (!mRecording && mRecordBtn.isEnabled()) {
                                mHandler.sendEmptyMessage(3);
                            }
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }


    public static void launch(BaseActivity context, int requestCode, int[] startingLocation) {
        Intent intent = new Intent(context, VideoRecordActivity.class);
        intent.putExtra("location", startingLocation);
        context.startActivityForResult(intent, requestCode);
    }


    private void setupRevealBackground(Bundle savedInstanceState) {
        mRevealBackgroundView.setFillPaintColor(0xFFF4F4F4);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_record);
        mRevealBackgroundView = (RevealBackgroundView) findViewById(R.id.vRevealBackground);
        setupRevealBackground(savedInstanceState);
        mContent = (RelativeLayout) findViewById(R.id.camera_content);
        mVideoRecord = (VideoRecord) findViewById(R.id.videoRecord);
        mNoteText = (TextView) findViewById(R.id.page6_note_textView);
        mMoveLine = (ImageView) findViewById(R.id.page6_move_line);
        mLeftLine = (ImageView) findViewById(R.id.page6_left_line);
        mRightLine = (ImageView) findViewById(R.id.page6_right_line);
        mCenterLine = (ImageView) findViewById(R.id.page6_center_line);
        mRecordBtn = (FloatingActionButton) findViewById(R.id.page6_video_btn);
        mHeadView = (ImageView) findViewById(R.id.page6_head_view);
        mRecordBtn.setEnabled(false);
        mNoteText.setText(R.string.page6_note_step_1);
        mTempDir = FileUtil.getPathByType(FileUtil.DIR_TYPE_TEMP);
        File tempDir = new File(mTempDir);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        ViewTreeObserver centerObserver = mCenterLine.getViewTreeObserver();
        centerObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float tranX = mCenterLine.getX() * MOVE_RATIO;
                mRightLine.setTranslationX(tranX);
                mLeftLine.setTranslationX(-tranX);
            }
        });
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(mSensorEventListener, mAccelerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        mMagicCameraDisplay = new MagicCameraDisplay(this, mVideoRecord.getGlSurfaceView());
        //默认的美颜参数,可通过adjustFilter调整。
        mMagicParams[0] = 0.33f;
        mMagicParams[1] = 0.63f;
        mMagicParams[2] = 0.4f;
        mMagicParams[3] = 0.35f;
        mMagicBeautyFilter = new MagicBeautyFilter(this);
        //mMagicBeautyFilter.adjustFilter(mMagicParams);
        mMagicCameraDisplay.setFilter(mMagicBeautyFilter);
        //初始化录制的名称
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);
        mVideoName = dateFormat.format(new Date()) + ".mp4";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMagicCameraDisplay != null) {
            mMagicCameraDisplay.onResume();
        }
    }

    private void loadContent() {
        mContent.setTranslationY(100.f);
        mContent.animate()
                .translationY(0.f)
                .setListener(new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mContent.setVisibility(View.VISIBLE);
                    }
                })
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        //1.竖直方向的动画效果
        mLeftLine.setTranslationY(ResHelper.getScreenHeight());
        mRightLine.setTranslationY(ResHelper.getScreenHeight());
        mCenterLine.setTranslationY(-ResHelper.getScreenHeight());
        mLeftLine.animate()
                .translationY(0.f)
                .setDuration(500)
                .setListener(new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mLeftLine.setVisibility(View.VISIBLE);
                    }
                })
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        mRightLine.animate()
                .translationY(0.f)
                .setListener(new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mRightLine.setVisibility(View.VISIBLE);
                    }
                })
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        mCenterLine.animate()
                .translationY(0.f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(500)
                .setListener(new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mCenterLine.setVisibility(View.VISIBLE);
                    }
                })
                .start();
        mHeadView.setScaleX(0.f);
        mHeadView.setScaleY(0.f);
        mHeadView.animate()
                .scaleY(1.f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .scaleX(1.f)
                .setDuration(500)
                .setListener(new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isHeadViewValidate = true;
                        mHeadView.setBackgroundResource(R.drawable.red_rect);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mHeadView.setVisibility(View.VISIBLE);
                    }
                })
                .start();

        mRecordBtn.setScaleX(0.f);
        mRecordBtn.setScaleY(0.f);
        mRecordBtn.setEnabled(true);
        mRecordBtn.animate()
                .scaleY(1.f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .scaleX(1.f)
                .setListener(new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mRecordBtn.setVisibility(View.VISIBLE);
                    }
                })
                .setDuration(500)
                .start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMagicCameraDisplay != null) {
            mMagicCameraDisplay.onDestroy();
        }
        mSensorManager.unregisterListener(mSensorEventListener);
        if (mMoveAnimtor != null && mMoveAnimtor.isRunning()) {
            mMoveAnimtor.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.page6_video_btn == v.getId()) {
            if (mRecording) {
                return;
            }
            mRecordBtn.setImageResource(android.R.drawable.presence_video_busy);
            FileUtil.DeleteRecursive(new File(mTempDir), false);
            if (!mVideoRecord.startRecord(mTempDir + mVideoName)) {
                TToast.show("不支持录制视频!", v.getContext());
                VideoRecordActivity.this.finish();
                return;
            }
            mNoteText.setText(R.string.page6_note_step1);
            if (mMoveAnimtor == null) {
                mMoveAnimtor = ObjectAnimator.ofFloat(mMoveLine, "translationX"
                        , 0f, -mCenterLine.getX() * MOVE_RATIO, 0f
                        , mCenterLine.getX() * MOVE_RATIO, 0f);
                mMoveAnimtor.setDuration(MOVE_DURATION);
                mMoveAnimtor.setInterpolator(new LinearInterpolator());
                mMoveAnimtor.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mMoveLine.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mMediaPlayer != null) {
                            mMoveLine.setVisibility(View.GONE);
                            mVideoRecord.stopRecord();
                            mHandler.sendEmptyMessage(1);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mRecording = false;
                        mMoveLine.setVisibility(View.GONE);
                        mVideoRecord.stopRecord();
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mMoveAnimtor.setStartDelay(500);
            }
            mMoveAnimtor.start();
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.record);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.release();
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return true;
                }
            });
            mMediaPlayer.start();
            mRecording = true;
            mRecordBtn.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMagicCameraDisplay != null) {
            mMagicCameraDisplay.onPause();
        }
    }
}
