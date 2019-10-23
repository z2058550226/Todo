package com.practice.todo.storage.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.practice.todo.storage.database.entity.TodoSubItem;

/**
 * TodoSubItem操作类，这里都只需写出定义即可，真正的实现由编译器帮你实现
 */
@Dao
public interface TodoSubItemDao {
    @Query("SELECT * FROM todo_sub_item WHERE parent_id = :parentId")
    TodoSubItem[] loadByParentId(int parentId);

    @Insert
    void insert(TodoSubItem todoSubItem);

    @Update
    void update(TodoSubItem... todoSubItems);

    @Delete
    void delete(TodoSubItem... todoSubItems);
}
