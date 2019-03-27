# Android 架构组件（一）——Lifecycle
---
## 概述

在2018年5月9日的Google IO 大会上，Goole官方推出了一套Android架构组件 [Android Architecture Components,](https://developer.android.com/topic/libraries/architecture/index.html)，在Android组件处理生命周期的问题上，新增了一套生命周期管理组件[lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle.html)

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




