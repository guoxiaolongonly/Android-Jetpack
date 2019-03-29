package cn.xiaolongonly.sample.viewmodel;

import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import cn.xiaolongonly.sample.R;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/29 11:16
 */
public class ViewModelActivity extends AppCompatActivity {
    private BooksViewModel mBooksViewModel;
    private TextView tvBookName;
    private TextView tvBookList;
    private Random random;

    private static final String[] randomBook = {"马克思主义基本原理上", "马克思主义基本原理下", "邓小平理论", "毛泽东思想", "三个代表重要思想", "编不下去了"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedata);
        random = new Random();
        tvBookName = findViewById(R.id.tvBookName);
        tvBookList = findViewById(R.id.tvBookList);
        mBooksViewModel = ViewModelProviders.of(this).get(BooksViewModel.class);
        mBooksViewModel.getCurrentName().observe(this, (String name) -> {
            tvBookName.setText("currentBook: " + name);
        }); // 订阅LiveData中当前Name数据变化，以lambda形式定义Observer
        mBooksViewModel.getNameList().observe(this, (List<String> nameList) -> {
            tvBookList.setText("");
            for (String item : nameList) {
                tvBookList.append("book: " + item + "\n");
            }
        }); // 订阅LiveData中Name列表数据变化，以lambda形式定义Observer


        tvBookList.setOnClickListener(v -> {
            List<String> nameList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                nameList.add(randomBook[random.nextInt(randomBook.length)]);
            }
            mBooksViewModel.getNameList().setValue(nameList);
        });

        tvBookName.setOnClickListener(v -> {
            mBooksViewModel.getCurrentName().setValue(randomBook[random.nextInt(randomBook.length)]);
        });
    }
}
