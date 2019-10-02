package com.practice.todo.storage.database.dao

import androidx.room.*
import com.practice.todo.storage.database.entity.TodoItem

@Dao
interface TodoItemDao {

    @Query("SELECT * FROM todo_item")
    fun loadAll(): Array<TodoItem>

    @Query("SELECT * FROM todo_item WHERE id = :id")
    fun loadById(id: Int): TodoItem

    @Insert
    fun insert(todoItem: TodoItem)

    @Update
    fun update(vararg todoItems: TodoItem)

    @Delete
    fun delete(vararg todoItems: TodoItem)
}