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
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8, getResources().getDisplayMetrics());
        mRvTodoList.setLayoutManager(new LinearLayoutManager(this));
        mRvTodoList.addItemDecoration(new SpacesItemDecoration(space));
        mRvTodoList.setAdapter(mAdapter);
        refreshList();
    }

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TodoEditActivity.REQ_CODE && resultCode == TodoEditActivity.RES_CODE) {
            refreshList();
        }
    }
}
