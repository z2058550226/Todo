package com.practice.todo.storage.database;

import androidx.room.Room;

import com.practice.todo.App;

public class DB {

    public static TodoDatabase sDb = Room.databaseBuilder(App.instance, TodoDatabase.class, "todo.db").build();
}
