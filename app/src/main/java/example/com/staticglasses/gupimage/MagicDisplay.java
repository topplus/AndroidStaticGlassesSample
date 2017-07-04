package example.com.staticglasses.gupimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class MagicDisplay implements Renderer {

    protected final GLSurfaceView mGLSurfaceView;
    protected final FloatBuffer mGLCubeBuffer;
    protected final FloatBuffer mGLTextureBuffer;
    private final LinkedList<Runnable> mRunOnCreate;
    protected GPUImageFilter mFilters;
    protected int mTextureId = OpenGLUtils.NO_TEXTURE;
    protected SaveTask mSaveTask;


    protected int mSurfaceWidth, mSurfaceHeight;


    protected int mImageWidth, mImageHeight;

    protected Context mContext;
    private boolean mSurfaceCreated = false;

    public MagicDisplay(Context context, GLSurfaceView glSurfaceView) {
        mContext = context;
        mGLSurfaceView = glSurfaceView;
        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mRunOnCreate = new LinkedList<Runnable>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!mRunOnCreate.isEmpty()) {
            mRunOnCreate.removeFirst().run();
        }
        mSurfaceCreated = true;
    }

    public void setFilter(final GPUImageFilter filter) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mFilters != null)
                    mFilters.destroy();
                mFilters = null;
                mFilters = filter;
                if (mFilters != null)
                    mFilters.init();
                onFilterChanged();
            }
        };
        if (mSurfaceCreated) {
            mGLSurfaceView.queueEvent(runnable);
        } else {
            mRunOnCreate.addLast(runnable);
        }
        mGLSurfaceView.requestRender();
    }

    protected void onFilterChanged() {
        if (mFilters == null)
            return;
        mFilters.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
        mFilters.onOutputSizeChanged(mImageWidth, mImageHeight);
    }

    protected void onResume() {

    }

    protected void onPause() {
        if (mSaveTask != null)
            mSaveTask.cancel(true);
    }

    protected void onDestroy() {
        mSurfaceCreated = false;
    }

    protected void getBitmapFromGL(final Bitmap bitmap, final boolean newTexture) {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int[] mFrameBuffers = new int[1];
                int[] mFrameBufferTextures = new int[1];
                GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
                GLES20.glGenTextures(1, mFrameBufferTextures, 0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                        GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
                GLES20.glViewport(0, 0, width, height);
                mFilters.onOutputSizeChanged(width, height);
                mFilters.onDisplaySizeChanged(mImageWidth, mImageHeight);
                int textureId = OpenGLUtils.NO_TEXTURE;
                if (newTexture)
                    textureId = OpenGLUtils.loadTexture(bitmap, OpenGLUtils.NO_TEXTURE, true);
                else
                    textureId = mTextureId;
                mFilters.onDrawFrame(textureId);
                IntBuffer ib = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
                Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mBitmap.copyPixelsFromBuffer(IntBuffer.wrap(ib.array()));
                if (newTexture)
                    GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
                GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
                GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
                GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
                mFilters.destroy();
                mFilters.init();
                mFilters.onOutputSizeChanged(mImageWidth, mImageHeight);
                onGetBitmapFromGL(mBitmap);
            }
        });
    }

    protected void onGetBitmapFromGL(Bitmap bitmap) {

    }

    protected void deleteTextures() {
        if (mTextureId != OpenGLUtils.NO_TEXTURE)
            mGLSurfaceView.queueEvent(new Runnable() {

                @Override
                public void run() {
                    GLES20.glDeleteTextures(1, new int[]{
                            mTextureId
                    }, 0);
                    mTextureId = OpenGLUtils.NO_TEXTURE;
                }
            });
    }

    public void requestRender() {
        mGLSurfaceView.requestRender();
    }
}
