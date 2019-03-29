package cn.xiaolongonly.sample;

import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <描述功能>
 *
 * @author xiaolong 719243738@qq.com
 * @version v1.0
 * @since 2019/3/20 14:15
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
    private List<Pair<String, Class>> activityList = new ArrayList<>();

    protected ActivityAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(activityList.get(position));
    }

    public void setData(List<Pair<String, Class>> data) {
        activityList.clear();
        activityList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvActivityName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvActivityName = itemView.findViewById(R.id.tvActivityName);
        }

        public void setData(final Pair<String, Class> item) {
            tvActivityName.setText(item.first);
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setClass(itemView.getContext(), item.second);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
