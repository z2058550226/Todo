package com.practice.todo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.practice.todo.R;
import com.practice.todo.TodoEditActivity;
import com.practice.todo.storage.database.entity.TodoItem;

import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView的适配器
 */
public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder> {

    private List<TodoItem> mDataList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final TodoItem item = mDataList.get(position);
        holder.mCbIsDone.setChecked(item.isDone());
        holder.mTvTitle.setText(item.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TodoEditActivity.start((Activity) holder.itemView.getContext(), item.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void updateAll(List<TodoItem> items) {
        mDataList.clear();
        mDataList.addAll(items);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final CheckBox mCbIsDone;
        final TextView mTvTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCbIsDone = itemView.findViewById(R.id.mCbIsDone);
            mTvTitle = itemView.findViewById(R.id.mTvTitle);
        }
    }
}
