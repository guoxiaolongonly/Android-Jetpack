# Android 架构组件（一）——Lifecycle
---
## 概述

在2017年的Google IO 大会上，Goole官方推出了一套Android架构组件 [Android Architecture Components,](https://developer.android.com/topic/libraries/architecture/index.html)，在Android组件处理生命周期的问题上，新增了一套生命周期管理组件[lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle.html)

可以用 android.arch.lifecycle 包提供的类来控制数据、监听器等的 lifecycle。同时LiveData 与 ViewModel 的 lifecycle 也依赖于 Lifecycle 框架。

目前Android Architecture Components 正式Release， Lifecycle也正式植入进了SupportActivity（AppCompatActivity的基类）和Fragment中，我觉得还是有必要去尝试学习google的这个框架，不管有没有用到，我相信其本身的设计思想也能为给我们提供很多帮助。


## 一、Lifecycle简介&使用

### 为什么要引进Lifecycle？

我们在处理Activity或者Fragment组件生命周期的时候，不可避免地会遇到以下问题：

我们在Activity的onCreate()中初始化某些成员（比如MVP架构中的Presenter），然后在onStop中对这些成员进行对应处理，在onDestroy中释放这些资源，这样导致我们的代码也许会像这样：


```

class MyPresenter{
    public MyPresenter() {
    }

    void create() {
        //do something
    }

    void destroy() {
        //do something
    }
}

class MyActivity extends AppCompatActivity {
    private MyPresenter presenter;

    public void onCreate(...) {
        presenter= new MyPresenter ();
        presenter.create();
    }

    public void onDestroy() {
        super.onDestroy();
        presenter.destory();
    }
}

```

代码没有问题，关键问题是，实际生产环境中 ，这样的代码会非常复杂，你最终会有太多的类似调用并且会导致 onCreate() 和 onDestroy() 方法变的非常臃肿。

### 使用lifecycle

[Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle.html) 是一个类，它持有关于组件（如 Activity 或 Fragment）生命周期状态的信息，并且允许其他对象观察此状态。

我们只需要2步：

#### 1、Prestener继承LifecycleObserver接口

```

public interface IPresenter extends LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate(@NotNull LifecycleOwner owner);

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy(@NotNull LifecycleOwner owner);

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onLifecycleChanged(@NotNull LifecycleOwner owner,
                            @NotNull Lifecycle.Event event);
}

```
```

public class BasePresenter implements IPresenter {
    private static final String TAG = BasePresenter.class.getSimpleName();

    @Override
    public void onCreate(LifecycleOwner owner) {
        Log.d(TAG, "listen create");
    }

    @Override
    public void onDestroy(LifecycleOwner owner) {
        Log.d(TAG, "listen destory");
    }

    @Override
    public void onLifecycleChanged(LifecycleOwner owner, Lifecycle.Event event) {
        Log.d(TAG, "listen lifeChange  " + event.name());
    }
}

```


我直接将我想要观察到Presenter的生命周期事件都列了出来，然后封装到BasePresenter 中，这样每一个BasePresenter 的子类都能感知到Activity容器对应的生命周期事件，并在子类重写的方法中，对应相应行为。


#### 2、在Activity/Fragment容器中添加Observer：


```

public class MainActivity extends AppCompatActivity {
    private IPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("tag", "onCreate" + this.getClass().toString());
        setContentView(R.layout.activity_main);
        mPresenter = new MainPresenter(this);
        getLifecycle().addObserver(mPresenter);//添加LifecycleObserver
    }

    @Override
    protected void onDestroy() {
        Log.d("tag", "onDestroy" + this.getClass().toString());
        super.onDestroy();
    }
}

```


如此，每当Activity发生了对应的生命周期改变，Presenter就会执行对应事件注解的方法：
除onCreate和onDestroy事件之外，Lifecycle一共提供了所有的生命周期事件，只要
通过注解进行声明，就能够使LifecycleObserver观察到对应的生命周期事件：

//以下为logcat日志

	03-27 15:18:07.122 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen create
	03-27 15:18:07.122 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen lifeChange ON_CREATE
	03-27 15:18:07.122 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen lifeChange ON_START
	03-27 15:18:07.152 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen lifeChange ON_RESUME
	03-27 15:18:21.542 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen lifeChange ON_PAUSE
	03-27 15:18:21.872 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen lifeChange ON_STOP
	03-27 15:18:21.872 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen destory
	03-27 15:18:21.872 24525-24525/cn.xiaolongonly.lifecycle D/BasePresenter: listen lifeChange ON_DESTROY
	
	
## 二、原理分析

先通过一张图了解一下Lifecycle的类结构

图片引自：[Android 架构组件（一）——Lifecycle](https://blog.csdn.net/zhuzp_blog/article/details/78871374)
![Lifecycle](https://upload-images.jianshu.io/upload_images/7293029-e8b3a15d2ed0a6ee.png)


我们以V4包中的Fragment（AppCompatActivity类似）为例，看下Fragment和LifecycleOwner、LifecycleObserver、Lifecycle之间的类关系图。

- Lifecycle组件成员Lifecycle被定义成了抽象类，LifecycleOwner、LifecycleObserver被定义成了接口；

- Fragment实现了LifecycleOwner接口，该只有一个返回Lifecycle对象的方法getLifecyle()；

- Fragment中getLifecycle()方法返回的是继承了抽象类Lifecycle的LifecycleRegistry。

- LifecycleRegistry中定义嵌套类ObserverWithState，该类持有GenericLifecycleObserver对象，而GenericLifecycleObserver是继承了LifecycleObserver的接口。


再通过通过时序图，来看看lifecycle运作的流程

图片引自：[Android 架构组件（一）——Lifecycle](https://blog.csdn.net/zhuzp_blog/article/details/78871374)
![Lifecycle](https://upload-images.jianshu.io/upload_images/7293029-a125ace9440970e6.png)


我们在Fragment（AppCompatActivity也一样）中调用getLifecycle()方法得到LifecycleRegistry对象，然后调用addObserver()方法并将实现了LifecycleObserver接口的对象作为参数传进去。这样一个过程就完成了注册监听的过程。

后续就是Fragment生命周期变化时，通知LifecycleObserver的过程：Fragment的performXXX()、onXXX()方法；LifecycleRegistry的handleLifecycleEvent()方法；LifecycleObserver的onXXX()方法。

如果你细心点看上面的时序图，你会发现Fragment中performCreate()、performStart()、performResume()会先调用自身的onXXX()方法，然后再调用LifecycleRegistry的handleLifecycleEvent()方法；而在performPause()、performStop()、performDestroy()中会先LifecycleRegistry的handleLifecycleEvent()方法 ，然后调用自身的onXXX()方法。日志上打印出来也确实如此：


	03-27 15:53:47.732 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__MainActivity: onCreate
	03-27 15:53:47.742 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__BasePresenter: listen lifeChange  ON_CREATE
	03-27 15:53:47.742 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__MainActivity: onStart
	03-27 15:53:47.742 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__BasePresenter: listen lifeChange  ON_START
	03-27 15:53:47.892 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__MainActivity: onResume
	03-27 15:53:47.892 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__BasePresenter: listen lifeChange  ON_RESUME
	03-27 15:53:50.302 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__BasePresenter: listen lifeChange  ON_PAUSE
	03-27 15:53:50.302 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__MainActivity: onPause
	03-27 15:53:50.842 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__BasePresenter: listen lifeChange  ON_STOP
	03-27 15:53:50.842 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__MainActivity: onStop
	03-27 15:53:50.842 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__BasePresenter: listen lifeChange  ON_DESTROY
	03-27 15:53:50.842 27868-27868/cn.xiaolongonly.lifecycle D/Lucky__MainActivity: onDestroy

	看一下Lifecycle中的状态枚举
```
    public enum State {
        /**
         * 在onDestory执行时，切至DESTORYED状态，后onDestory
         */
        DESTROYED,

        /**
         * 对应Activity的onCreate之前的生命周期
         */
        INITIALIZED,

        /**
		 * 对应Activity的onCreate到onStop之间的生命周期
         */
        CREATED,

        /**
         * 对应Activity的onStart到onPause之间的生命周期
         */
        STARTED,

        /**
         * 对应Activity的onResume
         */
        RESUMED;

        /**
         * Compares if this State is greater or equal to the given {@code state}.
         */
        public boolean isAtLeast(@NonNull State state) {
            return compareTo(state) >= 0;
        }
    }
	
```
### 源码分析

这里其实我们就需要搞清楚两点 

1.State如何与生命周期绑定？
2.Event事件是如何分发到LifecycleObserver的？

## 1.State与生命周期的绑定

### 1.从Androidx ComponentActivity开始.

```

public class ComponentActivity extends Activity implements LifecycleOwner {
    ...
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
 
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportFragment.injectIfNeededIn(this);
    }
 
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
	...
```
可以看出：
1.getLifecycle最终返回的是一个LifecycleRegistry 对象，所有的订阅操作由LifecycleRegister完成
2.在onCreate方法中执行了ReportFragment.injectIfNeededIn(this);


### 2.LifecycleRegistry

```
public class LifecycleRegistry extends Lifecycle {
    // LifecycleObserver Map，每一个Observer都有一个State
    private FastSafeIterableMap<LifecycleObserver, ObserverWithState> mObserverMap = new FastSafeIterableMap<>();
    // 当前的状态
    private State mState;
    // 生命周期拥有者，上述的ComponentActivity继承了LifecycleOwner
    private final WeakReference<LifecycleOwner> mLifecycleOwner;
 
    public LifecycleRegistry(@NonNull LifecycleOwner provider) {
        mLifecycleOwner = new WeakReference<>(provider);
        mState = INITIALIZED;
    }
 
    /**
    * 添加LifecycleObserver观察者，并将之前的状态分发给这个Observer,例如我们在onResume之后注册这个Observer，
    * 该Observer依然能收到ON_CREATE事件
    */
    public void addObserver(@NonNull LifecycleObserver observer) {
        State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
        ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
        ObserverWithState previous = mObserverMap.putIfAbsent(observer, statefulObserver);
        ......
        // 例如：Observer初始状态是INITIALIZED，当前状态是RESUMED，需要将INITIALIZED到RESUMED之间的
        // 所有事件分发给Observer
        // 
        while ((statefulObserver.mState.compareTo(targetState) < 0
                && mObserverMap.contains(observer))) {
            pushParentState(statefulObserver.mState);
            statefulObserver.dispatchEvent(lifecycleOwner, upEvent(statefulObserver.mState));
            popParentState();
            targetState = calculateTargetState(observer);
        }
        ......
    }
 
    /**
     * 处理生命周期事件
     */
    public void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
        State next = getStateAfter(event);
        moveToState(next);
    }
 
    /**
     * 改变状态
     */
    private void moveToState(State next) {
        if (mState == next) {
            return;
        }
        mState = next;
        ......
        sync();
        ......
    }
 
    /**
     * 同步Observer状态，并分发事件
     */
    private void sync() {
        LifecycleOwner lfecycleOwner = mLifecycleOwner.get();
        if (lifecycleOwner == null) {
            Log.w(LOG_TAG, "LifecycleOwner is garbage collected, you shouldn't try dispatch "
                    + "new events from it.");
            return;
        }
        while (!isSynced()) {
            mNewEventOccurred = false;
            // State中，状态值是从DESTROYED-INITIALIZED-CREATED-STARTED-RESUMED增大
            // 如果当前状态值 < Observer状态值，需要通知Observer减小状态值，直到等于当前状态值
            if (mState.compareTo(mObserverMap.eldest().getValue().mState) < 0) {
                backwardPass(lifecycleOwner);
            }
            Entry<LifecycleObserver, ObserverWithState> newest = mObserverMap.newest();
            // 如果当前状态值 > Observer状态值，需要通知Observer增大状态值，直到等于当前状态值
            if (!mNewEventOccurred && newest != null
                    && mState.compareTo(newest.getValue().mState) > 0) {
                forwardPass(lifecycleOwner);
            }
        }
        mNewEventOccurred = false;
    }
 
    /**
     * 向前传递事件，对应图中的INITIALIZED -> RESUMED
     * 增加Observer的状态值，直到状态值等于当前状态值
     */
    private void forwardPass(LifecycleOwner lifecycleOwner) {
        Iterator<Entry<LifecycleObserver, ObserverWithState>> ascendingIterator =
                mObserverMap.iteratorWithAdditions();
        while (ascendingIterator.hasNext() && !mNewEventOccurred) {
            Entry<LifecycleObserver, ObserverWithState> entry = ascendingIterator.next();
            ObserverWithState observer = entry.getValue();
            while ((observer.mState.compareTo(mState) < 0 && !mNewEventOccurred
                    && mObserverMap.contains(entry.getKey()))) {
                pushParentState(observer.mState);
                // 分发状态改变事件
                observer.dispatchEvent(lifecycleOwner, upEvent(observer.mState));
                popParentState();
            }
        }
    }
 
    /**
     * 向后传递事件，对应图中的RESUMED -> DESTROYED
     * 减小Observer的状态值，直到状态值等于当前状态值
     */
    private void backwardPass(LifecycleOwner lifecycleOwner) {
        Iterator<Entry<LifecycleObserver, ObserverWithState>> descendingIterator =
                mObserverMap.descendingIterator();
        while (descendingIterator.hasNext() && !mNewEventOccurred) {
            Entry<LifecycleObserver, ObserverWithState> entry = descendingIterator.next();
            ObserverWithState observer = entry.getValue();
            while ((observer.mState.compareTo(mState) > 0 && !mNewEventOccurred
                    && mObserverMap.contains(entry.getKey()))) {
                Event event = downEvent(observer.mState);
                // 分发状态改变事件
                pushParentState(getStateAfter(event));
                observer.dispatchEvent(lifecycleOwner, event);
                popParentState();
            }
        }
    }
}

```
可以看出这个方法主要有以下几个功能：

1.addObserver,将添加进来的观察者存放到一个observerMap中，并向该观察者推送之前的状态。
2.handleLifecycleEvent，作为Event事件处理，这个方法会改变观察者的State和LifecycleRegistry当前的State，并对所有的观察者做一次同步。


### 3.ReportFragment

在ComponentActivity中的订阅很简洁，并没有看到对于Activity生命周期的分发，所以这一块，的处理可能是交给了其他类去代理。
ReportFragment.injectIfNeededIn(this)就充当了这个职责。

```

    public static void injectIfNeededIn(Activity activity) {
        android.app.FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(REPORT_FRAGMENT_TAG) == null) {
            manager.beginTransaction().add(new ReportFragment(), REPORT_FRAGMENT_TAG).commit();
            manager.executePendingTransactions();
        }
    }

    static ReportFragment get(Activity activity) {
        return (ReportFragment) activity.getFragmentManager().findFragmentByTag(
                REPORT_FRAGMENT_TAG);
    }

    private ActivityInitializationListener mProcessListener;

    private void dispatchCreate(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onCreate();
        }
    }

    private void dispatchStart(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onStart();
        }
    }

    private void dispatchResume(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onResume();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dispatchCreate(mProcessListener);
        dispatch(Lifecycle.Event.ON_CREATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        dispatchStart(mProcessListener);
        dispatch(Lifecycle.Event.ON_START);
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatchResume(mProcessListener);
        dispatch(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatch(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        dispatch(Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispatch(Lifecycle.Event.ON_DESTROY);
        // just want to be sure that we won't leak reference to an activity
        mProcessListener = null;
    }

    private void dispatch(Lifecycle.Event event) {
        Activity activity = getActivity();
        if (activity instanceof LifecycleRegistryOwner) {
            ((LifecycleRegistryOwner) activity).getLifecycle().handleLifecycleEvent(event);
            return;
        }

        if (activity instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) activity).getLifecycle();
            if (lifecycle instanceof LifecycleRegistry) {
                ((LifecycleRegistry) lifecycle).handleLifecycleEvent(event);
            }
        }
    }

```

1.通过获取Activity的FM，设置的TAG来增加/获取当前ReportFragment
2.所有的生命周期都调用了dispatch(Lifecycle.Event event)，最终这个方法拿到Activity的LifecycleRegistry 做相应生命周期变化事件的处理。

### 4.Fragment
我们知道生命周期管理同样适用于Fragment，Fragment的生命周期是怎么做的呢？这边可以从ComponentActivity的子类FragmentActivity入手。

```

 private void markFragmentsCreated() {
        boolean reiterate;
        do {
            reiterate = markState(getSupportFragmentManager(), Lifecycle.State.CREATED);
        } while (reiterate);
    }

    private static boolean markState(FragmentManager manager, Lifecycle.State state) {
        boolean hadNotMarked = false;
        Collection<Fragment> fragments = manager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment == null) {
                continue;
            }
            if (fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                fragment.mLifecycleRegistry.markState(state);
                hadNotMarked = true;
            }

            FragmentManager childFragmentManager = fragment.peekChildFragmentManager();
            if (childFragmentManager != null) {
                hadNotMarked |= markState(childFragmentManager, state);
            }
        }
        return hadNotMarked;
    }

```
1.这个类的主要就是把当前Activity的所有Fragment设置一个Create状态

2.在Fragment中也是跟Activity中的方法类似，处理了所有的周期变化

## 总结

回到我们最初的两个问题：

### State如何与生命周期绑定？

在Activity中添加一个ReportFragment(如果你的Activity继承AppCompatActivity，会在父类的onCreate()中添加ReportFragment，否则由LifecycleDispatcher添加)，在ReportFragment生命周期函数中调用LifecycleRegistry.handleLifecycleEvent()方法改变State。

### Event事件是如何分发到LifecycleObserver的？

LifecycleRegistry在收到handleLifecycleEvent()后，内部调用moveToState()方法，改变State值，每一次State值改变，都会调用LifecycleObserver.onStateChanged()方法将Event分发到LifecycleObserver


文章参考：

https://blog.csdn.net/zhuzp_blog/article/details/78871374
https://www.jianshu.com/p/b1208012b268





