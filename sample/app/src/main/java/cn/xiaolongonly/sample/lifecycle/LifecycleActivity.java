package cn.xiaolongonly.sample.lifecycle;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import cn.xiaolongonly.lifecycle.BaseActivity;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/27 14:07
 */
public class LifecycleActivity extends BaseActivity<BasePresenter> {
    private static final String TAG = "Lucky__"+ LifecycleActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate-----------");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected BasePresenter createPresenter() {
        return new BasePresenter();
    }


}
