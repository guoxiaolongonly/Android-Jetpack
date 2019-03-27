package cn.xiaolongonly.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/27 11:31
 */
public interface IPresenter extends LifecycleObserver {

//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    void onCreate(LifecycleOwner owner);
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    void onDestroy(LifecycleOwner owner);
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    void onStart(LifecycleOwner owner);
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    void onResume(LifecycleOwner owner);
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    void onPause(LifecycleOwner owner);
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    void onStop(LifecycleOwner owner);

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onLifecycleChanged(LifecycleOwner owner,
                            Lifecycle.Event event);
}
