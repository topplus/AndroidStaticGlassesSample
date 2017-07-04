package example.com.staticglasses.ui.view;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;

import example.com.staticglasses.gupimage.CameraEngine;

/**
 * Created by ssbai on 2015/12/14.
 */
public class VideoRecord extends FrameLayout {
    private MediaRecorder mMediaRecorder;
    private GLSurfaceView mPreviewView;

    public VideoRecord(Context context) {
        super(context);
        init(context);
    }

    public VideoRecord(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoRecord(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPreviewView = new GLSurfaceView(context);
        addView(mPreviewView);
    }

    public GLSurfaceView getGlSurfaceView() {
        return mPreviewView;
    }

    /**
     * 开始录制视频
     *
     * @param mp4Path 视频mp4文件的存储路径
     */
    public boolean startRecord(String mp4Path) {
        Camera camera = CameraEngine.getCamera();
        if (camera == null)
            return false;
        File mp4File = new File(mp4Path);
        if (mp4File.exists()) {
            mp4File.delete();
        }
        if (!CamcorderProfile.hasProfile(CameraEngine.getCameraId(), CamcorderProfile.QUALITY_720P)) {
            return false;
        }
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CameraEngine.getCameraId(), CamcorderProfile.QUALITY_720P);
        camcorderProfile.videoCodec = MediaRecorder.VideoEncoder.H264;
        camcorderProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        camera.unlock();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(camera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setOutputFile(mp4Path);
        mMediaRecorder.setOrientationHint(CameraEngine.getImageRotation(getContext()));
        mMediaRecorder.setProfile(camcorderProfile);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 停止录制视频
     */
    public void stopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            Camera camera = CameraEngine.getCamera();
            if (camera != null) {
                camera.lock();
            }
        }
    }

    /**
     * 录制视频时相机的35mm等效焦距
     *
     * @return 35mm等效焦距
     */
    public float getFocusLength() {
        return CameraEngine.getFocalLength();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;
            float scale = 1.2f;
            int previewWidth = CameraEngine.mPreviewHeight;
            int previewHeight = CameraEngine.mPreviewWidth;
            // Center the child SurfaceView within the parent.
            if (width * previewHeight < height * previewWidth) {
                final int scaledChildWidth = (int) (previewWidth * height / previewHeight * scale);
                child.layout((width - scaledChildWidth) / 2, (int) ((height - height * scale) / 2),
                        (width + scaledChildWidth) / 2, (int) ((height + height * scale) / 2));
            } else {
                final int scaledChildHeight = (int) (previewHeight * width / previewWidth * scale);
                child.layout((int) ((width - width * scale) / 2), (height - scaledChildHeight) / 2,
                        (int) ((width + width * scale) / 2), (height + scaledChildHeight) / 2);
            }
        }
    }
}
