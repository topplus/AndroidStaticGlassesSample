package example.com.staticglasses.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import example.com.staticglasses.R;
import example.com.staticglasses.manager.HeadManager;

/**
 * @author fandong
 * @date 2016/9/27
 * @description
 */

public class HistoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mHeadPaths;
    private HeadManager mHeadManager;

    public HistoryAdapter(Context context) {
        this.mContext = context;
        this.mHeadManager = HeadManager.getInstance(context);
        this.mHeadPaths = mHeadManager.getHeadModels();
    }

    public String getHeadModelPath(int position) {
        return mHeadPaths.get(position);
    }

    public int getCount() {
        return mHeadPaths.size();
    }

    public void deleteHead(int position) {
        String headPath = mHeadPaths.get(position);
        mHeadPaths.remove(headPath);
        mHeadManager.deleteHeadModel(headPath);
    }

    public void refreshDataSetChanged() {
        this.mHeadPaths = mHeadManager.getHeadModels();
        super.notifyDataSetChanged();
    }

    public String getItem(int position) {
        return mHeadPaths.get(position);
    }


    public long getItemId(int position) {
        return position;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_history, null);
            convertView.setTag(new HistoryAdapter.HeadViewHolder(convertView));
        }
        HistoryAdapter.HeadViewHolder viewHolder = (HistoryAdapter.HeadViewHolder) convertView.getTag();
        String headPath = mHeadPaths.get(position);
        viewHolder.iv.setImageResource(R.drawable.back_image);
        String path = mHeadManager.getMiddleJpegPath(headPath);
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                viewHolder.iv.setImageURI(Uri.fromFile(file));
            }
            return convertView;
        }
        viewHolder.iv.setImageResource(R.mipmap.ic_launcher);
        return convertView;
    }

    public String getHeadPath(int position) {
        return mHeadPaths.get(position);
    }


    public static class HeadViewHolder {
        ImageView iv;
//        RippleView layout;

        public HeadViewHolder(View itemView) {
            this.iv = (ImageView) itemView.findViewById(R.id.history_item);
//            this.layout = (RippleView) itemView.findViewById(R.id.RippleView);
        }
    }
}
