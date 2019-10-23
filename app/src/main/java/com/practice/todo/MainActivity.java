package com.practice.todo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.practice.todo.adapter.TodoListAdapter;
import com.practice.todo.storage.database.DB;
import com.practice.todo.storage.database.dao.TodoItemDao;
import com.practice.todo.storage.database.entity.TodoItem;
import com.practice.todo.util.SpacesItemDecoration;

import java.util.Arrays;

/**
 * 主页面，主要是一个RecyclerView，持有Room的sqlite数据库DAO对象
 *
 * Room的一切操作都要求在子线程执行，所以这里频繁的进行切换线程(其实可以用线程池，但可能又复杂了点)
 */
public class MainActivity extends AppCompatActivity {

    private TodoListAdapter mAdapter = new TodoListAdapter();
    private TodoItemDao mTodoItemDao = DB.sDb.todoItemDao();

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView mRvTodoList = findViewById(R.id.mRvTodoList);
        // 设置8dp的item间隔
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8, getResources().getDisplayMetrics());
        mRvTodoList.setLayoutManager(new LinearLayoutManager(this));
        mRvTodoList.addItemDecoration(new SpacesItemDecoration(space));
        mRvTodoList.setAdapter(mAdapter);
        refreshList();
    }

    /**
     * 刷新RecyclerView
     *
     * 读取数据库中的TodoItem，然后放到RecyclerView的Adapter中
     */
    private void refreshList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final TodoItem[] todoItems = mTodoItemDao.loadAll();
                Log.e("sss", Arrays.toString(todoItems));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateAll(Arrays.asList(todoItems));
                    }
                });
            }
        }).start();
    }

    /**
     * 添加todo事项，这个是FloatActionButton的点击事件。
     *
     * 代码是弹出一个用来输入的Dialog，然后在submit的点击事件(onSend)中新建一个TodoItem模型类插入数据库中
     * 插入完毕之后刷新列表
     */
    public void addTodo(View view) {
        new InputDialog(this, new InputDialog.OnSendListener() {
            @Override
            public void onSend(final String content) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTodoItemDao.insert(new TodoItem(content));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshList();
                            }
                        });
                    }
                }).start();
            }
        }).show();
    }

    /**
     * 当从编辑页返回主页面时刷新一下列表，因为可能删除了一个Todo
     *
     * 这里也可以写在onResume中。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TodoEditActivity.REQ_CODE && resultCode == TodoEditActivity.RES_CODE) {
            refreshList();
        }
    }
}
