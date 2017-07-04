package example.com.staticglasses.gupimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MagicImageDisplay extends MagicDisplay {

    private final GPUImageFilter mImageInput;

    private Bitmap mOriginBitmap;

    private boolean mIsSaving = false;

    public MagicImageDisplay(Context context, GLSurfaceView glSurfaceView) {
        super(context, glSurfaceView);
        mImageInput = new GPUImageFilter();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        mImageInput.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        adjustImageDisplaySize();
        onFilterChanged();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mTextureId == OpenGLUtils.NO_TEXTURE)
            mTextureId = OpenGLUtils.loadTexture(mOriginBitmap, OpenGLUtils.NO_TEXTURE);
        if (mFilters == null) {
            mImageInput.onDrawFrame(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
        } else {
            mFilters.onDrawFrame(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled())
            return;
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        mOriginBitmap = bitmap;
        adjustImageDisplaySize();
        refreshDisplay();
    }

    private void refreshDisplay() {
        deleteTextures();
        mGLSurfaceView.requestRender();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void adjustImageDisplaySize() {
        float ratio1 = (float) mSurfaceWidth / mImageWidth;
        float ratio2 = (float) mSurfaceHeight / mImageHeight;
        //float ratioMax = Math.max(ratio1, ratio2);
        float ratioMax = Math.min(ratio1, ratio2) / 1.2f;
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / (float) mSurfaceWidth;
        float ratioHeight = imageHeightNew / (float) mSurfaceHeight;

        float[] cube = new float[]{
                TextureRotationUtil.CUBE[0] / ratioHeight, TextureRotationUtil.CUBE[1] / ratioWidth,
                TextureRotationUtil.CUBE[2] / ratioHeight, TextureRotationUtil.CUBE[3] / ratioWidth,
                TextureRotationUtil.CUBE[4] / ratioHeight, TextureRotationUtil.CUBE[5] / ratioWidth,
                TextureRotationUtil.CUBE[6] / ratioHeight, TextureRotationUtil.CUBE[7] / ratioWidth,
        };
        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
    }

    protected void onGetBitmapFromGL(Bitmap bitmap) {
        mOriginBitmap = bitmap;
        if (mIsSaving) {
            mSaveTask.execute(mOriginBitmap);
            mIsSaving = false;
        }
    }

    public void restore() {
        if (mFilters != null) {
            setFilter(null);
        } else {
            setImageBitmap(mOriginBitmap);
        }
    }

    public void commit() {
        if (mFilters != null) {
            getBitmapFromGL(mOriginBitmap, false);
            deleteTextures();
        } else {
            mOriginBitmap.recycle();
        }
    }

    public void savaImage(File output, SaveTask.onPictureSaveListener listener) {
        mSaveTask = new SaveTask(mContext, output, listener);
        mIsSaving = true;
        if (mFilters != null)
            getBitmapFromGL(mOriginBitmap, false);
        else
            onGetBitmapFromGL(mOriginBitmap);
    }
}
