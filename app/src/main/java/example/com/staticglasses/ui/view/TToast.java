package example.com.staticglasses.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import example.com.staticglasses.R;

/**
 * @author fandong
 * @date 2016/9/24
 * @description
 */
public class TToast extends Toast {
    public static final int TOAST_DURATION = 3000;
    public static final int TOAST_DURATION_SHORT = 1500;

    private View mToast;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            WindowManager manager = (WindowManager) mToast.getContext()
                    .getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            manager.removeView(mToast);
            mToast = null;
        }
    };

    public TToast(Context context, int textResourceId, int drawableId) {
        super(context);
        Resources resources = context.getResources();
        View view = LayoutInflater.from(context).inflate(R.layout.vw_toast, null);
        TextView textView = (TextView) view.findViewById(R.id.id_cc_toast_text);
        String text = resources.getString(textResourceId);
        textView.setText(text);
        setDuration(Toast.LENGTH_SHORT);
        setView(view);
    }

    public TToast(Context context, CharSequence text, Drawable drawable) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.vw_toast, null);
        TextView textView = (TextView) view.findViewById(R.id.id_cc_toast_text);
        textView.setText(text);
        setDuration(Toast.LENGTH_SHORT);
        setView(view);
    }

    public static TToast show(String msg, Context context) {
        TToast toast = new TToast(context, msg, null);
        toast.show();
        return toast;
    }

    public static TToast show(int msg, Context context) {
        TToast toast = new TToast(context, context.getText(msg), null);
        toast.show();
        return toast;
    }

}
