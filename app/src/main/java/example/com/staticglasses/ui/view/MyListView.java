package example.com.staticglasses.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import example.com.staticglasses.util.PixelUtil;

/**
 * @author fandong
 * @date 2016/9/24
 * @description
 */

public class MyListView extends ListView {

    private float lastX = 0.f;
    private float lastY = 0.f;
    private OnGestureListener mOnGestureListener;

    private boolean isLocked;

    private boolean isScrolling;

    private float limit;

    private float interceptLastX;
    private float interceptLastY;
    private boolean isFling;

    public MyListView(Context context) {
        super(context);
        limit = PixelUtil.dp2px(4.f);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        limit = PixelUtil.dp2px(4.f);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        limit = PixelUtil.dp2px(4.f);
    }

    @TargetApi(21)
    public MyListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        limit = PixelUtil.dp2px(4.f);
    }

    public void setIsFling(boolean isFling) {
        this.isFling = isFling;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        if (isFling) {
            return result;
        }
        int action = MotionEventCompat.getActionMasked(ev);
        if (MotionEvent.ACTION_DOWN == action) {
            interceptLastX = ev.getRawX();
            interceptLastY = ev.getRawY();
            result = false;
        } else if (MotionEvent.ACTION_MOVE == action) {
            if (interceptLastY <= 0 && interceptLastX <= 0) {
                interceptLastX = ev.getRawX();
                interceptLastY = ev.getRawY();
            } else {
                float distanceX = Math.abs(interceptLastX - ev.getRawX());
                float distanceY = Math.abs(interceptLastY - ev.getRawY());
                if (distanceX > limit || distanceY > limit) {
                    result = true;
                }
            }

        } else if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_POINTER_UP) {
            interceptLastX = -1;
            interceptLastY = -1;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFling) {
            return super.onTouchEvent(event);
        }
        //判断是否应该锁定
        float x = event.getRawX();
        float y = event.getRawY();
        float distanceX = lastX - x;
        float distanceY = lastY - y;
        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_DOWN) {
            lastX = x;
            lastY = y;
            isLocked = false;
            isScrolling = false;
        } else if (action == MotionEvent.ACTION_MOVE && !isScrolling) {
            if (0.f == lastX || 0.f == lastY) {
                lastX = x;
                lastY = y;
                isLocked = false;
            } else {
                if (distanceX == 0.f && distanceY == 0.f) {
                    isScrolling = false;
                } else {
                    isScrolling = true;
                }
                if (!isLocked) {
                    if (Math.abs(distanceX) > 0.f && Math.abs(distanceY / distanceX) < 1.5) {
                        isLocked = true;
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_POINTER_UP) {
            isLocked = false;
            isScrolling = false;
            lastX = 0.f;
            lastY = 0.f;
        }
        if (isLocked && null != mOnGestureListener) {
            return mOnGestureListener.onGesture(distanceX, distanceY, event);
        }
        return super.onTouchEvent(event);
    }


    public void setOnGestureListener(OnGestureListener onGestureListener) {
        this.mOnGestureListener = onGestureListener;
    }

    public interface OnGestureListener {
        boolean onGesture(float distanceX, float distanceY, MotionEvent event);
    }
}
