package cn.xiaolongonly.sample;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import cn.xiaolongonly.sample.lifecycle.LifecycleActivity;
import cn.xiaolongonly.sample.viewmodel.ViewModelActivity;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/29 14:10
 */
public class ActivityGenerateUtil {

    public static List<Pair<String, Class>> initActivityList() {
        List<Pair<String, Class>> activityList = new ArrayList<>();
        activityList.add(new Pair<>("lifecycle", LifecycleActivity.class));
        activityList.add(new Pair<>("viewModel", ViewModelActivity.class));
        return activityList;
    }


}
