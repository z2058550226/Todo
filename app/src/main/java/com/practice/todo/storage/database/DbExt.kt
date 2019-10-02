package com.practice.todo.storage.database

import androidx.room.Room
import com.practice.todo.App

val db by lazy {
    Room.databaseBuilder(App.instance, TodoDatabase::class.java,"todo.db").build()
}