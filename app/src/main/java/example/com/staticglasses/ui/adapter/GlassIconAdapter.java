package example.com.staticglasses.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import example.com.staticglasses.R;
import example.com.staticglasses.ui.view.RippleView;
import example.com.staticglasses.util.BitmapUtil;
import example.com.staticglasses.util.helper.ResHelper;

/**
 * @author fandong
 * @date 2016/9/27
 * @description
 */

public class GlassIconAdapter extends RecyclerView.Adapter<GlassIconAdapter.ViewHolder> {

    private static final String[] NAMES = new String[]{"1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg",
            "6.jpg", "7.jpg", "8.jpg", "9.jpg",};

    private Context mContext;

    private int mSelectedPosition;

    private OnItemClickListener mOnItemClickListener;

    private boolean mIsLoaded;

    public GlassIconAdapter(Context context) {
        this.mContext = context;
        this.mSelectedPosition = 0;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }


    @Override
    public GlassIconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.vw_glass_icon, parent, false));
    }

    @Override
    public void onBindViewHolder(GlassIconAdapter.ViewHolder holder, final int position) {

        BitmapUtil.loadImage(holder.iv, "assets://" + NAMES[position]);
        holder.rippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPosition = position;
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
        if (position != mSelectedPosition) {
            holder.rippleView.setBackgroundColor(0xFFE1E1E1);
        } else {
            holder.rippleView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        }
        if (!mIsLoaded) {
            holder.itemView.setTranslationY(ResHelper.getDimen(R.dimen.recycler_item_size));
            holder.itemView.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setStartDelay(position * 100);
        }

    }

    @Override
    public int getItemCount() {
        return NAMES.length;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setLoaded(boolean loaded) {
        this.mIsLoaded = loaded;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        RippleView rippleView;

        public ViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.iv_icon);
            rippleView = (RippleView) itemView.findViewById(R.id.iv_ripple_view);
        }
    }
}
