package cn.xiaolongonly.sample.lifecycle;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import cn.xiaolongonly.lifecycle.IPresenter;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/27 11:30
 */
public class BasePresenter implements IPresenter {
    private static final String TAG = "Lucky__" + BasePresenter.class.getSimpleName();


    @Override
    public void onCreate(LifecycleOwner owner) {
        Log.d(TAG, "listen create---------");
    }

    @Override
    public void onDestroy(LifecycleOwner owner) {
        Log.d(TAG, "listen destory");
    }

    @Override
    public void onStart(LifecycleOwner owner) {
        Log.d(TAG, "listen start");
    }

    @Override
    public void onResume(LifecycleOwner owner) {
        Log.d(TAG, "listen resume");
    }

    @Override
    public void onPause(LifecycleOwner owner) {
        Log.d(TAG, "listen pause");
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        Log.d(TAG, "listen stop");
    }
//
//    @Override
//    public void onLifecycleChanged(LifecycleOwner owner, Lifecycle.Event event) {
//        Log.d(TAG, "listen lifeChange  " + event.name());
//    }
}
