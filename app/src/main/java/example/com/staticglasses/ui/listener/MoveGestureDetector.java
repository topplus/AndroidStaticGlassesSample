package example.com.staticglasses.ui.listener;

import android.content.Context;
import android.view.MotionEvent;

import topplus.com.commonutils.util.DimensionUtil;

public class MoveGestureDetector {
    protected static final float PRESSURE_THRESHOLD = 0.67f;
    private final static int STATE_UNKOWN = 0;
    private final static int STATE_FINGER1_LEFT_RIGTH = 1;
    private final static int STATE_FINGER1_UP_DOWN = 2;
    private final static int STATE_FINGER2_LEFT_RIGHT = 3;
    private final static int STATE_FINGER2_UP_DOWN = 4;
    private final static int STATE_SCALE = 5;
    private MoveListener mMoveListener;
    private Context mContext;
    private int mFingerCount = 0;
    private int mState = STATE_UNKOWN;
    private MotionEvent mPreEvent;
    private float mFirstSpan = 0f;

    public MoveGestureDetector(Context context, MoveListener listener) {
        mContext = context;
        mMoveListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mState = STATE_UNKOWN;
                mFingerCount = 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mFingerCount = 2;
                    mState = STATE_UNKOWN;
                    mFirstSpan = getSpan(event, 0, 1);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mFingerCount = 0;
                mState = STATE_UNKOWN;
                break;
            case MotionEvent.ACTION_UP:
                mFingerCount = 0;
                mState = STATE_UNKOWN;
                break;
            case MotionEvent.ACTION_CANCEL:
                mFingerCount = 0;
                mState = STATE_UNKOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mFingerCount) {
                    case 1:
                        if (mPreEvent != null && event.getPressure() / mPreEvent.getPressure() > PRESSURE_THRESHOLD) {
                            float distanceX = DimensionUtil.convertPixelsToDp(event.getX(0) - mPreEvent.getX(0), mContext);
                            float distanceY = DimensionUtil.convertPixelsToDp(event.getY(0) - mPreEvent.getY(0), mContext);
                            switch (mState) {
                                case STATE_UNKOWN:
                                    if (Math.abs(distanceX) > 4 * Math.abs(distanceY) &&
                                            Math.abs(distanceX) > 1) {
                                        mState = STATE_FINGER1_LEFT_RIGTH;
                                    }
                                    if (Math.abs(distanceY) > 4 * Math.abs(distanceX) &&
                                            Math.abs(distanceY) > 1) {
                                        mState = STATE_FINGER1_UP_DOWN;
                                    }
                                    break;
                                case STATE_FINGER1_LEFT_RIGTH:
                                    result = mMoveListener.onScroll(1, distanceX, 0);
                                    break;
                                case STATE_FINGER1_UP_DOWN:
                                    result = mMoveListener.onScroll(1, 0, distanceY);
                                    break;
                            }
                        }
                        break;
                    case 2:
                        if (mPreEvent != null && mPreEvent.getPointerCount() == 2
                                && event.getPressure() / mPreEvent.getPressure() > PRESSURE_THRESHOLD) {
                            float preX = (mPreEvent.getX(0) + mPreEvent.getX(1)) / 2;
                            float preY = (mPreEvent.getY(0) + mPreEvent.getY(1)) / 2;
                            float curX = (event.getX(0) + event.getX(1)) / 2;
                            float curY = (event.getY(0) + event.getY(1)) / 2;
                            float distanceX = DimensionUtil.convertPixelsToDp(curX - preX, mContext);
                            float distanceY = DimensionUtil.convertPixelsToDp(curY - preY, mContext);
                            switch (mState) {
                                case STATE_UNKOWN:
                                    float currentSpan = getSpan(event, 0, 1);
                                    if (Math.abs(mFirstSpan - currentSpan) < 3) {
                                        if (Math.abs(distanceX) > 4 * Math.abs(distanceY) &&
                                                Math.abs(distanceX) > 1) {
                                            mState = STATE_FINGER2_LEFT_RIGHT;
                                        }
                                        if (Math.abs(distanceY) > 4 * Math.abs(distanceX) &&
                                                Math.abs(distanceY) > 1) {
                                            mState = STATE_FINGER2_UP_DOWN;
                                        }
                                    } else {
                                        mState = STATE_SCALE;
                                    }
                                    break;
                                case STATE_FINGER2_LEFT_RIGHT:
                                    result = mMoveListener.onScroll(2, distanceX, 0);
                                    break;
                                case STATE_FINGER2_UP_DOWN:
                                    result = mMoveListener.onScroll(2, 0, distanceY);
                                    break;
                                case STATE_SCALE: {
                                    float preSpan = getSpan(mPreEvent, 0, 1);
                                    float curSpan = getSpan(event, 0, 1);
                                    result = mMoveListener.onScale(curSpan / preSpan);
                                }
                                break;
                            }
                            break;
                        }
                }
                break;
        }
        mPreEvent = MotionEvent.obtain(event);
        return result;
    }

    private float getSpan(MotionEvent event, int first, int second) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        float span = (float) Math.sqrt(x * x + y * y);
        return DimensionUtil.convertPixelsToDp(span, mContext);
    }

    public interface MoveListener {
        public boolean onScroll(int pointCount, float distanceX, float distanceY);

        public boolean onScale(float factor);
    }
}
