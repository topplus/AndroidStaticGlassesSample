package example.com.staticglasses.util.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import example.com.staticglasses.R;
import example.com.staticglasses.StaticApplication;
import example.com.staticglasses.util.PixelUtil;


/**
 * time: 2015/9/15
 * description:统一对话框的样式
 *
 * @author fandong
 */
public class DialogHelper {
    public static final int TYPE_OTHER = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_PROGRESS = TYPE_NORMAL << 1;
    public static final int TYPE_LIST = TYPE_PROGRESS << 1;
    public static final int TYPE_RADIO = TYPE_LIST << 1;
    public static final int TYPE_EDIT = TYPE_RADIO << 1;
    //1.进度条相关的控制
    private int mType;
    //2.标题文字
    private String mTitle;
    //3.内容文字
    private String mContent;
    //4.输入框的hint
    private String mEditHint;
    //5.输入框的文字
    private String mEditText;
    //输入框的背景
    private int mEditTextDrawable;
    //6.进度条的文字
    private String mProgressTxt;
    //7.checkbox的文字提示
    private String mCheckBox;
    //8.左侧的按钮文字样式
    private int mLeftBtnColor;
    private String mLeftBtn;
    private int mLeftBtnBgDrawable;
    //9.右侧的按钮文字样式
    private int mRightBtnColor;
    //右侧的按钮背景
    private int mRightBtnBgDrawable;
    private String mRightBtn;
    //10.下边栏的文字样式
    private int mBottomBtnColor;
    //底下按钮的背景
    private int mBottomBtnDrawable;
    private String mBottomBtn;
    //11.左边按钮的点击事件
    private OnDialogClickListener mLeftOnClickListener;
    //12.右边按钮的点击事件
    private OnDialogClickListener mRightOnClickListener;
    //13.下边栏对应的点击事件
    private OnDialogClickListener mBottomOnClickListener;
    //14.文字显示样式的集合，用于显示一个list
    private View[] mListContent;
    //15.每个条目点击效果
    private OnItemClickListener mOnItemClickListener;
    //16.点击区域外的部分是否消失
    private boolean mCanceledOnTouchOutside = true;
    //17.点击返回键，对话框是否消失
    private boolean mCancelable = true;
    //17.RadioButton的选择序号
    private int mRadioIndex = -1;
    //18.对话框消失对应的回调
    private Dialog.OnDismissListener mOnDismissListener;
    //19.RadioGroup的点击事件
    private OnRadioGroupSelectListener mOnRadioGroupSelectListener;
    //20.当前显示的activity
    private Activity mActivity;

    private DialogHelper(int type) {
        this.mType = type;

    }

    public static DialogHelper create() {
        return new DialogHelper(TYPE_NORMAL);
    }

    public static DialogHelper create(int type) {
        return new DialogHelper(type);
    }

    public DialogHelper canceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.mCanceledOnTouchOutside = canceledOnTouchOutside;
        return this;
    }

    public DialogHelper activity(Activity activity) {
        this.mActivity = activity;
        return this;
    }

    public DialogHelper onRadioGroupSelectListener(OnRadioGroupSelectListener onRadioGroupSelectListener) {
        this.mOnRadioGroupSelectListener = onRadioGroupSelectListener;
        return this;
    }

    public DialogHelper cancelable(boolean cancelable) {
        this.mCancelable = cancelable;
        return this;
    }

    public DialogHelper onDismissListener(Dialog.OnDismissListener dismissListener) {
        this.mOnDismissListener = dismissListener;
        return this;
    }

    public DialogHelper title(String title) {
        this.mTitle = title;
        return this;
    }

    public DialogHelper listContent(View... views) {
        this.mListContent = views;
        return this;
    }

    public DialogHelper content(String content) {
        this.mContent = content;
        return this;
    }

    public DialogHelper editHint(String hint) {
        this.mEditHint = hint;
        return this;
    }

    public DialogHelper editText(String text) {
        this.mEditText = text;
        return this;
    }

    public DialogHelper editBackGround(int drawable) {
        this.mEditTextDrawable = drawable;
        return this;
    }

    public DialogHelper progressText(String text) {
        this.mProgressTxt = text;
        return this;
    }


    public DialogHelper checkBox(String text) {
        this.mCheckBox = text;
        return this;
    }

    public DialogHelper radioSelectedIndex(int index) {
        this.mRadioIndex = index;
        return this;
    }

    public DialogHelper leftButton(String leftButton) {
        this.mLeftBtn = leftButton;
        this.mLeftBtnColor = 0x787878;
        return this;
    }

    public DialogHelper leftButton(String leftButton, int color) {
        this.mLeftBtn = leftButton;
        this.mLeftBtnColor = color;
        return this;
    }

    public DialogHelper leftButton(String leftButton, int color, int drawable) {
        this.mLeftBtn = leftButton;
        this.mLeftBtnColor = color;
        this.mLeftBtnBgDrawable = drawable;
        return this;
    }

    public DialogHelper rightButton(String rightButton) {
        this.mRightBtn = rightButton;
        this.mRightBtnColor = 0xBEA977;
        return this;
    }

    public DialogHelper rightButton(String rightButton, int color) {
        this.mRightBtn = rightButton;
        this.mRightBtnColor = color;
        return this;
    }

    public DialogHelper rightButton(String rightButton, int color, int drawable) {
        this.mRightBtn = rightButton;
        this.mRightBtnColor = color;
        this.mRightBtnBgDrawable = drawable;
        return this;
    }

    public DialogHelper bottomButton(String bottomButton) {
        this.mBottomBtn = bottomButton;
        this.mBottomBtnColor = 0xBEA977;
        return this;
    }

    public DialogHelper bottomButton(String bottomButton, int color, int drawable) {
        this.mBottomBtn = bottomButton;
        this.mBottomBtnColor = color;
        this.mBottomBtnDrawable = drawable;
        return this;
    }

    public DialogHelper leftBtnClickListener(OnDialogClickListener listener) {
        this.mLeftOnClickListener = listener;
        return this;
    }

    public DialogHelper rightBtnClickListener(OnDialogClickListener listener) {
        this.mRightOnClickListener = listener;
        return this;
    }


    public DialogHelper bottomBtnClickListener(OnDialogClickListener listener) {
        this.mBottomOnClickListener = listener;
        return this;
    }

    public DialogHelper onItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        return this;
    }

    public Dialog show() {
        //1.得到activity
        if (null == mActivity) {
            mActivity = StaticApplication.getCurrentActivity();
        }
        Dialog dialog = new Dialog(mActivity, R.style.BaseDialog);
        //2.设置显示样式
        View view = createDialogView(dialog, mActivity);
        dialog.setContentView(view);
        dialog.setCancelable(mCancelable);
        dialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
        //3.设置对话框的宽度
        int margin = mActivity.getResources().getDimensionPixelSize(R.dimen.dialog_width_margin);
        int screenWidth = mActivity.getResources().getDisplayMetrics().widthPixels;
        int width = screenWidth - margin * 2;
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = width;
        window.setAttributes(lp);
        //4.添加监听
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (null != mOnDismissListener) {
                    mOnDismissListener.onDismiss(dialog);
                }
                mActivity = null;
            }
        });
        //5.显示出来
        dialog.show();
        return dialog;
    }

    /**
     * 根据输入的相关参数显示一个视图
     *
     * @param context activity
     * @return
     */
    private View createDialogView(final Dialog dialog, Context context) {
        View view = View.inflate(context, R.layout.vw_dialog, null);
        LinearLayout normalLayout = (LinearLayout) view.findViewById(R.id.normal_layout);
        LinearLayout progressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        if (mType == TYPE_NORMAL) {
            normalLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
            //1.得到控件
            TextView title = (TextView) normalLayout.findViewById(R.id.dialog_title);
            TextView content = (TextView) normalLayout.findViewById(R.id.dialog_content);

            RelativeLayout bottomLayout = (RelativeLayout) normalLayout.findViewById(R.id.btn_layout);
            TextView leftBtn = (TextView) bottomLayout.findViewById(R.id.cancel_btn);
            TextView rightBtn = (TextView) bottomLayout.findViewById(R.id.confirm_btn);
            //2.根据参数显示
            //2.1处理标题
            if (!TextUtils.isEmpty(mTitle)) {
                title.setVisibility(View.VISIBLE);
                title.setText(mTitle);
            } else {
                title.setVisibility(View.GONE);
            }
            //2.2处理内容
            if (!TextUtils.isEmpty(mContent)) {
                content.setVisibility(View.VISIBLE);
                content.setText(mContent);
            } else {
                content.setVisibility(View.GONE);
            }
            //2.3 处理按钮
            boolean hasLeft = !TextUtils.isEmpty(mLeftBtn);
            boolean hasRight = !TextUtils.isEmpty(mRightBtn);
            if (hasLeft || hasRight) {
                bottomLayout.setVisibility(View.VISIBLE);
                leftBtn.setVisibility(View.VISIBLE);
                rightBtn.setVisibility(View.VISIBLE);
                if (hasLeft) {
                    leftBtn.setText(mLeftBtn);
                    if (mLeftOnClickListener != null) {
                        leftBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mLeftOnClickListener.onClick(dialog, v);
                            }
                        });
                    }
                }
                if (hasRight) {
                    rightBtn.setText(mRightBtn);
                    if (mRightBtnBgDrawable != 0) {
                        rightBtn.setBackgroundResource(mRightBtnBgDrawable);
                    }
                    if (mRightOnClickListener != null) {
                        rightBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mRightOnClickListener.onClick(dialog, v);
                            }
                        });
                    }
                }
            } else {
                bottomLayout.setVisibility(View.GONE);
            }
        } else if (mType == TYPE_PROGRESS) {
            progressLayout.setVisibility(View.VISIBLE);
            normalLayout.setVisibility(View.GONE);
            TextView tv = (TextView) progressLayout.findViewById(R.id.dialog_progress_txt);
            if (TextUtils.isEmpty(mProgressTxt)) {
                tv.setText("loading");
            } else {
                tv.setText(mProgressTxt);
            }
        }
        return view;
    }

    private View getBottomLine(Context context) {
        View view = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        params.leftMargin = (int) PixelUtil.dp2px(16);
        params.rightMargin = (int) PixelUtil.dp2px(16);
        view.setLayoutParams(params);
        view.setBackgroundColor(0x33000000);
        return view;
    }

    public interface OnItemClickListener {
        void onItemClick(Dialog dialog, int position, View view);
    }

    public interface OnDialogClickListener {
        void onClick(Dialog dialog, View view);
    }

    public interface OnRadioGroupSelectListener {
        void onLeftClick();

        void onRightClick();
    }

}
