package cn.xiaolongonly.sample.viewmodel;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/29 11:20
 */
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
