package example.com.staticglasses.gupimage;

import android.content.Context;
import android.opengl.GLES20;

import example.com.staticglasses.R;

public class MagicBeautyFilter extends GPUImageFilter {
    private final String shader;
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private float[] mParams;

    public MagicBeautyFilter(Context context) {
        super(NO_FILTER_VERTEX_SHADER, OpenGLUtils.readShaderFromRawResource(context, R.raw.beautify_fragment_low));
        shader = OpenGLUtils.readShaderFromRawResource(context, R.raw.beautify_fragment);
        setBeautyLevel(5);
    }

    protected void onInit() {
        if (MagicFilterParam.mGPUPower == 1) {
            changeFragmentShader(shader);
        }
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        setFloatVec4(mParamsLocation, mParams);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[]{2.0f / w, 2.0f / h});
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    public void setBeautyLevel(int level) {
        switch (level) {
            case 1:
                if (mIsInitialized) {
                    setFloatVec4(mParamsLocation, new float[]{1.0f, 1.0f, 0.15f, 0.15f});
                } else {
                    mParams = new float[]{1.0f, 1.0f, 0.15f, 0.15f};
                }
                break;
            case 2:
                if (mIsInitialized) {
                    setFloatVec4(mParamsLocation, new float[]{0.8f, 0.9f, 0.2f, 0.2f});
                } else {
                    mParams = new float[]{0.8f, 0.9f, 0.2f, 0.2f};
                }
                break;
            case 3:
                if (mIsInitialized) {
                    setFloatVec4(mParamsLocation, new float[]{0.6f, 0.8f, 0.25f, 0.25f});
                } else {
                    mParams = new float[]{0.6f, 0.8f, 0.25f, 0.25f};
                }
                break;
            case 4:
                if (mIsInitialized) {
                    setFloatVec4(mParamsLocation, new float[]{0.4f, 0.7f, 0.38f, 0.3f});
                } else {
                    mParams = new float[]{0.4f, 0.7f, 0.38f, 0.3f};
                }
                break;
            case 5:
                if (mIsInitialized) {
                    setFloatVec4(mParamsLocation, new float[]{0.33f, 0.63f, 0.4f, 0.35f});
                } else {
                    mParams = new float[]{0.33f, 0.63f, 0.4f, 0.35f};
                }
                break;
            default:
                break;
        }
    }

    public void adjustFilter(float[] params) {
        if (params == null || params.length != 4) {
            return;
        }
        if (mIsInitialized) {
            setFloatVec4(mParamsLocation, params);
        } else {
            mParams = params;
        }
    }
}
