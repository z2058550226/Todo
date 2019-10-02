package com.practice.todo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.practice.todo.adapter.TodoListAdapter
import com.practice.todo.base.BaseActivity
import com.practice.todo.storage.database.db
import com.practice.todo.storage.database.entity.TodoItem
import com.practice.todo.util.SpacesItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.dip

class MainActivity : BaseActivity() {

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val mTodoItemDao = db.todoItemDao()
    private val mAdapter = TodoListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRvTodoList.layoutManager = LinearLayoutManager(this)
        mRvTodoList.addItemDecoration(SpacesItemDecoration(dip(8)))
        mRvTodoList.adapter = mAdapter
        refreshList()
    }

    private fun refreshList() {
        launch {
            val todoItems = withContext(Dispatchers.IO) {
                mTodoItemDao.loadAll()
            }
            mAdapter.updateAll(todoItems.toList())
        }
    }

    fun addTodo(view: View) {
        InputDialog.show(this) {
            launch {
                withContext(Dispatchers.IO) {
                    mTodoItemDao.insert(TodoItem(title = it))
                }
                refreshList()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TodoEditActivity.REQ_CODE && resultCode == TodoEditActivity.RES_CODE) {
            refreshList()
        }
    }
}
