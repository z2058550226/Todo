package com.practice.todo.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practice.todo.storage.database.dao.TodoItemDao
import com.practice.todo.storage.database.dao.TodoSubItemDao
import com.practice.todo.storage.database.entity.TodoItem
import com.practice.todo.storage.database.entity.TodoSubItem

@Database(
    entities = [
        TodoItem::class,
        TodoSubItem::class
    ], version = 1
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao
    abstract fun todoSubItemDao(): TodoSubItemDao
}