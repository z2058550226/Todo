package com.practice.todo.storage.database;

import androidx.room.Room;

import com.practice.todo.App;

public class DB {
    // 全局的Database对象，方便到处获取
    public static TodoDatabase sDb = Room.databaseBuilder(App.instance, TodoDatabase.class, "todo.db").build();
}
