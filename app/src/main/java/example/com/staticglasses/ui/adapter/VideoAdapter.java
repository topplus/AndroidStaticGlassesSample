package example.com.staticglasses.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.staticglasses.R;
import example.com.staticglasses.util.FileUtil;

/**
 * @author fandong
 * @date 2016/10/11.
 * @description
 */
public class VideoAdapter extends BaseAdapter {

    private Context mContext;

    private List<File> mFiles;

    public VideoAdapter(Context context) {
        this.mContext = context;
        //初始化数据
        this.mFiles = new ArrayList<>();
        //扫描文件
        File dir = new File(FileUtil.getPathByType(FileUtil.DIR_TYPE_TEMP));
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!this.mFiles.contains(file)) {
                    this.mFiles.add(0, file);
                }
            }
        }
        //扫描sd卡上面的视频文件
        dir = new File(FileUtil.getSdcardVideoPath(context));
        files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!this.mFiles.contains(file)) {
                    this.mFiles.add(0, file);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        if (mFiles.size() > 0)
            return mFiles.get(position);
        else return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.vw_list_text_item, null);
        }
        ((TextView) convertView.findViewById(R.id.tv)).setText(mFiles.get(position).getName());
        return convertView;
    }
}
