package example.com.staticglasses.gupimage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import threethird.it.sephiroth.android.library.exif2.ExifInterface;
import threethird.it.sephiroth.android.library.exif2.ExifTag;

public class CameraEngine {
    private final static String FOCALLENGTH = "focalLength";
    private final static int DEFAULT_FOCAL_LENGTH = 24;
    public static int mPreviewWidth = 1280;
    public static int mPreviewHeight = 720;
    private static Camera mCamera = null;
    private static int mCameraID = 0;
    private static int mPictureWidth = 1280;
    private static int mPictureHeight = 720;
    private static SharedPreferences mSharedPreferences;
    private static Map<Integer, Float> mFocalLengths = new HashMap<Integer, Float>();
    private static Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            ExifInterface exifInterface = new ExifInterface();
            try {
                exifInterface.readExif(data, ExifInterface.Options.OPTION_IFD_EXIF);
                ExifTag focalTag = exifInterface.getTag(ExifInterface.TAG_FOCAL_LENGTH_IN_35_MM_FILE);
                focalTag.getIfd();
                int focalLength = focalTag.getValueAsInt(0);
                if (focalLength < 15 || focalLength > 40) {
                    focalLength = DEFAULT_FOCAL_LENGTH;
                }
                putFocalLength(mCameraID, focalLength);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                putFocalLength(mCameraID, DEFAULT_FOCAL_LENGTH);
            }
            camera.startPreview();
        }
    };

    public static Camera getCamera() {
        return mCamera;
    }

    public static int getCameraId() {
        return mCameraID;
    }

    public static boolean openCamera() {
        if (mCamera == null) {
            try {
                int cameraCount = Camera.getNumberOfCameras();
                for (int i = 0; i < cameraCount; i++) {
                    CameraInfo cameraInfo = new CameraInfo();
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                        mCameraID = i;
                    }
                }
                mCamera = Camera.open(mCameraID);
                setDefaultParameters();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    private static void putFocalLength(int id, float focalLength) {
        mFocalLengths.put(id, focalLength);
        if (mSharedPreferences != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putFloat(FOCALLENGTH + id, focalLength);
            editor.commit();
        }
    }

    public static void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private static void setDefaultParameters() {
        Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            Size previewSize = getOptimalSize(sizes, mPreviewWidth, mPreviewHeight);
            mPreviewWidth = previewSize.width;
            mPreviewHeight = previewSize.height;
            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            sizes = mCamera.getParameters().getSupportedPictureSizes();
            Size pictureSize = getOptimalSize(sizes, mPictureWidth, mPictureHeight);
            mPictureWidth = pictureSize.width;
            mPictureHeight = pictureSize.height;
            parameters.setPictureSize(mPictureWidth, mPictureHeight);
        }
        mCamera.setParameters(parameters);
    }

    private static Size getOptimalSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;

        if (sizes == null)
            return null;

        Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;

            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private static Size getLargePreviewSize() {
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                if (temp.width < sizes.get(i).width)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }

    private static Size getLargePictureSize() {
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
            Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                float scale = (float) (sizes.get(i).height) / sizes.get(i).width;
                if (temp.width < sizes.get(i).width && scale < 0.6f && scale > 0.5f)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }

    public static Size getPreviewSize() {
        return mCamera.getParameters().getPreviewSize();
    }

    public static void startPreview(Context context, SurfaceTexture surfaceTexture) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences("android_camera_info", 0);
            int cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                float focalLengh = mSharedPreferences.getFloat(FOCALLENGTH + i, -1.f);
                if (focalLengh > 0) {
                    mFocalLengths.put(i, focalLengh);
                }
            }
        }
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
            if (!mFocalLengths.containsKey(mCameraID)) {
                mCamera.takePicture(null, null, mJpegCallback);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void startPreview(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences("android_camera_info", 0);
            int cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                float focalLengh = mSharedPreferences.getFloat(FOCALLENGTH + i, -1.f);
                if (focalLengh > 0) {
                    mFocalLengths.put(i, focalLengh);
                }
            }
        }
        if (mCamera != null) {
            mCamera.startPreview();
            if (!mFocalLengths.containsKey(mCameraID)) {
                mCamera.takePicture(null, null, mJpegCallback);
            }
        }
    }

    public static void stopPreview() {
        mCamera.stopPreview();
    }

    public static CameraInfo getCameraInfo() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);
        return cameraInfo;
    }

    public static int getOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);
        return cameraInfo.orientation;
    }

    public static boolean isFlipHorizontal() {
        return CameraEngine.getCameraInfo().facing == CameraInfo.CAMERA_FACING_FRONT ? true : false;
    }

    public static void setRotation(int rotation) {
        Parameters params = mCamera.getParameters();
        params.setRotation(rotation);
        mCamera.setParameters(params);
    }

    public static void takePicture(Camera.ShutterCallback shutterCallback, Camera.PictureCallback rawCallback,
                                   Camera.PictureCallback jpegCallback) {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    /**
     * 获取相机对应的图片，需要旋转多少度才能回到正常
     *
     * @param cameraId 相机的cameraId
     * @return 图片需要顺时针旋转的度数
     */
    public static int getImageRotation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static float getFocalLength() {
        float focus = -1;
        if (mFocalLengths.containsKey(mCameraID)) {
            focus = mFocalLengths.get(mCameraID);
        }
        return focus;
    }

    public void resumeCamera() {
        openCamera();
    }

    public Parameters getParameters() {
        if (mCamera != null)
            mCamera.getParameters();
        return null;
    }

    public void setParameters(Parameters parameters) {
        mCamera.setParameters(parameters);
    }
}
