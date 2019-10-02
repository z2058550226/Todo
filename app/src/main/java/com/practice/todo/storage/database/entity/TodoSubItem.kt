package com.practice.todo.storage.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo_sub_item",
    foreignKeys = [ForeignKey(
        entity = TodoItem::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("parent_id"),
        onDelete = CASCADE
    )]
)
data class TodoSubItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "parent_id") var parentId: Int = 0,
    var title: String = "",
    var isDone: Boolean = false
)