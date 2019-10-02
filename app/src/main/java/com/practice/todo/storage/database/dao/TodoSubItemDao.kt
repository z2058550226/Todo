package com.practice.todo.storage.database.dao

import androidx.room.*
import com.practice.todo.storage.database.entity.TodoSubItem

@Dao
interface TodoSubItemDao {
    @Query("SELECT * FROM todo_sub_item WHERE parent_id = :parentId")
    fun loadByParentId(parentId: Int): Array<TodoSubItem>

    @Insert
    fun insert(todoSubItem: TodoSubItem)

    @Update
    fun update(vararg todoItems: TodoSubItem)

    @Delete
    fun delete(vararg todoSubItems: TodoSubItem)
}