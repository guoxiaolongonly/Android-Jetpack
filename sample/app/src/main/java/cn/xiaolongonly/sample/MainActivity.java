package cn.xiaolongonly.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/29 11:43
 */
public class MainActivity extends AppCompatActivity {
    private RecyclerView rvContent;
    private ActivityAdapter activityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rvContent=findViewById(R.id.rvContent);
        rvContent.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvContent.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvContent.setAdapter(activityAdapter = new ActivityAdapter());
        activityAdapter.setData(ActivityGenerateUtil.initActivityList());
    }
}