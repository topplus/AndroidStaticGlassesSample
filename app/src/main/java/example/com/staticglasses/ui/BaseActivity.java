package example.com.staticglasses.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import example.com.staticglasses.StaticApplication;

/**
 * @author fandong
 * @date 2016/9/27
 * @description
 */

public abstract class BaseActivity extends AppCompatActivity {

    private boolean mIsResumed;

    private boolean mIsPostResumed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StaticApplication.addPage(this);
    }

    @Override
    protected void onDestroy() {
        StaticApplication.removePage(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsResumed) {
            mIsResumed = true;
            onLazyLoad();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!mIsPostResumed) {
            mIsPostResumed = true;
        }
    }

    public boolean hasResumed() {
        return mIsResumed;
    }

    public boolean hasPostResumed() {
        return mIsPostResumed;
    }


    protected void onLazyLoad() {
    }

}
