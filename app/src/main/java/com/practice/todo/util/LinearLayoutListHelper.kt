package com.practice.todo.util

import android.view.View
import android.widget.LinearLayout


/**
 * Created by suikajy on 2019.10.2
 */

class LinearLayoutListHelper {

    companion object {
        fun <VH : ViewHolder> setAdapter(layout: LinearLayout, adapter: LinearAdapter<VH>) {
            layout.removeAllViews()
            for (i in 0 until adapter.itemCount) {
                val viewHolder = adapter.onCreateViewHolder(layout, adapter.getItemType(i))
                adapter.onBindViewHolder(viewHolder, i)
                layout.addView(viewHolder.itemView)
            }
        }
    }

    abstract class LinearAdapter<VH : ViewHolder> {
        abstract val itemCount: Int

        abstract fun onCreateViewHolder(parent: LinearLayout, type: Int): VH

        abstract fun onBindViewHolder(holder: VH, position: Int)

        fun getItemType(position: Int) = -1
    }

    abstract class SimpleLinearAdapter<VH : ViewHolder, T>(
        private val mParent: LinearLayout
    ) : LinearAdapter<VH>() {
        protected var mDataSet: MutableList<T> = ArrayList()

        override val itemCount: Int
            get() = mDataSet.size

        abstract override fun onCreateViewHolder(parent: LinearLayout, type: Int): VH

        override fun onBindViewHolder(holder: VH, position: Int) {
            if (mDataSet.isNotEmpty()) {
                onSimpleBindView(holder, mDataSet[position], position)
            }
        }

        protected abstract fun onSimpleBindView(holder: VH, item: T, position: Int)

        fun refreshData(list: List<T>) {
            mDataSet.clear()
            mDataSet.addAll(list)
            setAdapter(mParent, this)
        }

        fun clear() {
            mDataSet.clear()
            setAdapter(mParent, this)
        }

        fun addItem(element: T) {
            val elePosition = mDataSet.size
            mDataSet.add(element)
            val viewHolder = onCreateViewHolder(mParent, getItemType(elePosition))
            onBindViewHolder(viewHolder, elePosition)
            mParent.addView(viewHolder.itemView)
        }
    }

    open class ViewHolder(val itemView: View)

}