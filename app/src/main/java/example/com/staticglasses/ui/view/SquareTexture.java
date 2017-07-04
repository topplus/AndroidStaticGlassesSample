package example.com.staticglasses.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.topplus.staticglasses.ui.widget.GlassTextureView;

/**
 * @author fandong
 * @date 2016/9/28
 * @description
 */

public class SquareTexture extends GlassTextureView {
    public SquareTexture(Context context) {
        super(context);
    }

    public SquareTexture(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareTexture(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int width = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        super.onMeasure(width, width);
    }
}
