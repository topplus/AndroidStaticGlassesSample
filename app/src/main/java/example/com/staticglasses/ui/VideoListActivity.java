package example.com.staticglasses.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

import example.com.staticglasses.R;
import example.com.staticglasses.ui.adapter.VideoAdapter;

/**
 * @author fandong
 * @date 2016/10/11.
 * @description 视频选择列表
 */
public class VideoListActivity extends BaseActivity {


    public static void launch(Activity context) {
        Intent intent = new Intent(context, VideoListActivity.class);
        context.startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar mActionBar = getSupportActionBar();
        if (null != mActionBar) {
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("视频");
        }

        ListView listView = (ListView) findViewById(R.id.lv);
        final VideoAdapter adapter = new VideoAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra("videoPath", ((File) adapter.getItem(position)).getAbsolutePath());
                setResult(0, data);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
