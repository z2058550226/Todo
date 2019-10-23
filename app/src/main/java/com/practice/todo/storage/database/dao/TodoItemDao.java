package com.practice.todo.storage.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.practice.todo.storage.database.entity.TodoItem;

/**
 * TodoItem数据库操作类
 */
@Dao
public interface TodoItemDao {
    @Query("SELECT * FROM todo_item")
    TodoItem[] loadAll();
    @Query("SELECT * FROM todo_item WHERE id = :id")
    TodoItem loadById(int id);
    @Insert
    void insert(TodoItem item);
    @Update
    void update(TodoItem... items);
    @Delete
    void delete(TodoItem... items);
}
