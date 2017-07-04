package example.com.staticglasses;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

import example.com.staticglasses.manager.AppInitManager;

/**
 * @author fandong
 * @date 2016/9/27
 * @description 代表应用的application
 */

public class StaticApplication extends Application {

    public static List<AppCompatActivity> PAGES;

    public static StaticApplication gContext;

    static {
        StaticApplication.PAGES = new LinkedList<AppCompatActivity>();
    }

    //添加到集合当中
    public static void addPage(AppCompatActivity activity) {
        PAGES.add(activity);
    }

    //从集合当中删除
    public static void removePage(AppCompatActivity activity) {
        if (!PAGES.isEmpty() && PAGES.contains(activity)) {
            PAGES.remove(activity);
        }
    }

    public static AppCompatActivity getCurrentActivity() {
        if (!PAGES.isEmpty()) {
            return PAGES.get(PAGES.size() - 1);
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gContext = this;
        //初始化
        AppInitManager.getInstance().init();
    }

    //退出
    public void destroy() {
        while (!PAGES.isEmpty()) {
            AppCompatActivity activity = PAGES.remove(PAGES.size() - 1);
            activity.finish();
        }
    }


}
