package com.practice.todo.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class LinearLayoutListHelper {
    public static void setAdapter(LinearLayout layout, LinearAdapter adapter) {
        layout.removeAllViews();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ViewHolder viewHolder = adapter.onCreateViewHolder(layout, adapter.getItemType(i));
            adapter.onBindViewHolder(viewHolder, i);
            layout.addView(viewHolder.itemView);
        }
    }

    public static abstract class LinearAdapter<VH extends ViewHolder> {

        protected Context mContext;
        protected LayoutInflater mInflater;

        public LinearAdapter(Context mContext) {
            this.mContext = mContext;
            mInflater = LayoutInflater.from(mContext);
        }

        public abstract VH onCreateViewHolder(LinearLayout parent, int type);

        public abstract void onBindViewHolder(VH holder, int position);

        public abstract int getItemCount();

        public int getItemType(int position) {
            return -1;
        }
    }

    public static abstract class SimpleLinearAdapter<VH extends ViewHolder, T> extends LinearAdapter<VH> {
        private final LinearLayout mParent;
        protected List<T> mDataSet = new ArrayList<>();

        public SimpleLinearAdapter(Context context, LinearLayout parent) {
            super(context);
            mParent = parent;
        }

        @Override
        public abstract VH onCreateViewHolder(LinearLayout parent, int type);

        @Override
        public void onBindViewHolder(VH holder, int position) {
            if (!mDataSet.isEmpty()) {
                onSimpleBindView(holder, mDataSet.get(position), position);
            }
        }

        protected abstract void onSimpleBindView(VH holder, T t, int position);

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        public void refreshData(List<T> list) {
            mDataSet.clear();
            mDataSet.addAll(list);
            setAdapter(mParent, this);
        }
    }

    public static class ViewHolder {
        public View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
