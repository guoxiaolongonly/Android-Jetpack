# Android 架构组件（二）——ViewModel
---
## 一、概述

[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)在2017年的Google I/O大会上，推出的一个适用于MVVM架构开发的官方组件。

它具有如下特性：

### 1. 更规范化的抽象接口

在官方的ViewModel发布之前，ViewModel层的基类多种多样，内部的依赖和公共逻辑更是五花八门。新的ViewModel组件直接对ViewModel层进行了标准化的规范，即使用ViewModel(或者其子类AndroidViewModel)。
同时，Google官方建议ViewModel尽量保证 纯的业务代码，不要持有任何View层(Activity或者Fragment)或Lifecycle的引用，这样保证了ViewModel内部代码的可测试性，避免因为Context等相关的引用导致测试代码的难以编写（比如，MVP中Presenter层代码的测试就需要额外成本，比如依赖注入或者Mock，以保证单元测试的进行）。

### 2.更便于保存数据

由系统响应用户交互或者重建组件，用户无法操控。当组件被销毁并重建后，原来组件相关的数据也会丢失——最简单的例子就是屏幕的旋转，如果数据类型比较简单，同时数据量也不大，可以通过onSaveInstanceState()存储数据，组件重建之后通过onCreate()，从中读取Bundle恢复数据。但如果是大量数据，不方便序列化及反序列化，则上述方法将不适用。
ViewModel的扩展类则会在这种情况下自动保留其数据，如果Activity被重新创建了，它会收到被之前相同ViewModel实例。当所属Activity终止后，框架调用ViewModel的onCleared()方法释放对应资源：

![img](https://upload-images.jianshu.io/upload_images/7293029-0b71443385ac3bdc.png?imageMogr2/auto-orient/)

这样看来，ViewModel是有一定的 作用域 的，它不会在指定的作用域内生成更多的实例，从而节省了更多关于 状态维护（数据的存储、序列化和反序列化）的代码。
ViewModel在对应的 作用域 内保持生命周期内的 局部单例，这就引发一个更好用的特性，那就是Fragment、Activity等UI组件间的通信。


### 3.更方便UI组件之间的通信

一个Activity中的多个Fragment相互通讯是很常见的，如果ViewModel的实例化作用域为Activity的生命周期，则两个Fragment可以持有同一个ViewModel的实例，这也就意味着数据状态的共享:

public class AFragment extends Fragment {
    private CommonViewModel model;
    public void onActivityCreated() {
        model = ViewModelProviders.of(getActivity()).get(CommonViewModel.class);
    }
}

public class BFragment extends Fragment {
    private CommonViewModel model;
    public void onActivityCreated() {
        model = ViewModelProviders.of(getActivity()).get(CommonViewModel.class);
    }
}

	上面两个Fragment getActivity()返回的是同一个宿主Activity，因此两个Fragment之间返回的是同一个ViewModel。

### 4.对状态的持有和维护

ViewModel层的根本职责，就是负责维护UI的状态，追根究底就是维护对应的数据——毕竟，无论是MVP还是MVVM，UI的展示就是对数据的渲染。

1.定义了ViewModel的基类，并建议通过持有LiveData维护保存数据的状态；
2.ViewModel不会随着Activity的屏幕旋转而销毁，减少了维护状态的代码成本（数据的存储和读取、序列化和反序列化）；
3.在对应的作用域内，保正只生产出对应的唯一实例，多个Fragment维护相同的数据状态，极大减少了UI组件之间的数据传递的代码成本。

现在我们对于ViewModel的职责和思想都有了一定的了解，按理说接下来我们应该阐述如何使用ViewModel了，但我想先等等，因为我觉得相比API的使用，掌握其本质的思想会让你在接下来的代码实践中如鱼得水。

## ViewModel的使用

流程很简单
```

public class BooksViewModel extends ViewModel {
    // Create a LiveData with a String
    private MutableLiveData<String> mCurrentBook;
    // Create a LiveData with a String list
    private MutableLiveData<List<String>> mBookListData;

    public MutableLiveData<String> getCurrentName() {
        if (mCurrentBook == null) {
            mCurrentBook = new MutableLiveData<>();
        }
        return mCurrentBook;
    }

    public MutableLiveData<List<String>> getNameList(){
        if (mBookListData == null) {
            mBookListData = new MutableLiveData<>();
        }
        return mBookListData;
    }
}

```

只需要继承ViewModel

```

public class ViewModelActivity extends AppCompatActivity {
    private BooksViewModel mBooksViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedata);
        mBooksViewModel = ViewModelProviders.of(this).get(BooksViewModel.class);
    }
}


```

获取Activity的BooksViewModel对象，

ViewModelProviders.of(FragmentActivity activity).get(Class<T> modelClass);

之后再通过当前Activity对象获取到的BooksViewModel都会是同一个对象。这样相当于Activity持有了这些数据。


## 二、原理分析

先了解一下ViewModel的类结构

![类图](https://img-blog.csdnimg.cn/20190117102246840.png)


- ViewModelProviders是ViewModel工具类，该类提供了通过Fragment和Activity得到ViewModel的方法，而具体实现又是有ViewModelProvider实现的。ViewModelProvider是实现ViewModel创建、获取的工具类。在ViewModelProvider中定义了一个创建ViewModel的接口类——Factory。
- ViewModelProvider中有个ViewModelStore对象，用于存储ViewModel对象。
- ViewModelStore是存储ViewModel的类，具体实现是通过HashMap来保存ViewModle对象。
- ViewModel是个抽象类，里面只定义了一个onCleared()方法，该方法在ViewModel不在被使用时调用。ViewModel有一个子类AndroidViewModel，这个类是便于要在ViewModel中使用Context对象，因为我们前面提到是不能在ViewModel中持有Activity的引用。

ViewModelStores是ViewModelStore的工厂方法类，它会关联HolderFragment，HolderFragment有个嵌套类——HolderFragmentManager。

再通过通过时序图，来看看lifecycle运作的流程

![时序图](https://img-blog.csdnimg.cn/20190117102108270.png)


时序图看起来比较复杂，但是它只描述了两个过程：

得到ViewModel对象。
HolderFragment被销毁时，ViewModel收到onCleared()通知。

## 三、源码分析

这边其实我们就要探究以下几点

1.如何保证数据不随屏幕旋转而销毁
2.作用域内只产出唯一实例

### 1.从ViewModelProviders开始

```
public class ViewModelProviders {

    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity/fragment is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }

    private static Activity checkActivity(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
        return activity;
    }

   
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        return of(fragment, null);
    }

 
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        return of(activity, null);
    }

 
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @Nullable Factory factory) {
        Application application = checkApplication(checkActivity(fragment));
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(fragment.getViewModelStore(), factory);
    }

  
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity,
            @Nullable Factory factory) {
        Application application = checkApplication(activity);
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(activity.getViewModelStore(), factory);
    }

	｝

```

 ViewModelProviders提供了四个of()方法，四个方法功能类似，其中of(FragmentActivity activity, Factory factory)和of(Fragment fragment, Factory factory)提供了自定义创建ViewModel的方法。我们在时序图中使用的是of(Fragment fragment)，这里我们也从该方法开始研究，在该方法中主要有两个步骤。

#### 1，判断是否需要初始化默认的Factory

 这一过程很简单：通过调用initializeFactoryIfNeeded()方法判断是否需要初始化mDefaultFactory变量。在此之前又会判断Fragment的是否Attached to Activity，Activity的Application对象是否为空。

#### 2，创建一个ViewModelProvider对象。

 创建ViewModel对象看似很简单，一行代码搞定new ViewModelProvider(ViewModelStores.of(fragment), sDefaultFactory)。但是里面的里面的逻辑比较深，我们慢慢把它抽丝剥茧，看看有多深。
