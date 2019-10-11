package com.practice.todo

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.practice.todo.storage.database.TodoDatabase
import com.practice.todo.storage.database.dao.TodoSubItemDao
import com.practice.todo.storage.database.dao.TodoItemDao
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Created by suikajy on 2019.10.2
 */
@RunWith(AndroidJUnit4::class)
class EntityReadWriteTest {

    private lateinit var todoItemDao: TodoItemDao
    private lateinit var todoSubItemDao: TodoSubItemDao
    private lateinit var db: TodoDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java).build()
        todoItemDao = db.todoItemDao()
        todoSubItemDao = db.todoSubItemDao()
    }

    @Test
    @Throws(Exception::class)
    fun loadAllTest() {
        val loadAll = todoItemDao.loadAll()
        Log.e("ss", loadAll.size.toString())
        assertTrue(loadAll.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun insertTest() {
        val todoListItem = TodoItem(title = "testTitle", isDone = false)
        val todoListItem2 = TodoItem(title = "testTitle2", isDone = false)
        todoItemDao.insert(todoListItem)
        todoItemDao.insert(todoListItem2)
        val loadAll = todoItemDao.loadAll()
        loadAll.forEach {
            println(it)
        }
        assertTrue(loadAll.size == 2)
    }

    @Test
    @Throws(Exception::class)
    fun queryById() {
        val todoListItem = TodoItem(title = "testTitle", isDone = false)
        val todoListItem2 = TodoItem(title = "testTitle2", isDone = false)
        todoItemDao.insert(todoListItem)
        todoItemDao.insert(todoListItem2)
        val item = todoItemDao.loadById(2)
        assertTrue(item.title == todoListItem2.title)
    }

    @Test
    @Throws(Exception::class)
    fun insertSubItem() {
        val todoListItem = TodoItem(title = "testTitle", isDone = false)
        val todoListItem2 = TodoItem(title = "testTitle2", isDone = false)
        todoItemDao.insert(todoListItem)
        todoItemDao.insert(todoListItem2)
        val listItem = todoItemDao.loadById(2)
        val todoItem = TodoSubItem(parentId = listItem.id, title = "sub item title1")
        todoSubItemDao.insert(todoItem)

        val subItems = todoSubItemDao.loadByParentId(2)
        assertTrue(subItems[0].title == todoItem.title)
    }

    @Test
    @Throws(Exception::class)
    fun deleteTodoCascade() {
        val todoListItem = TodoItem(title = "testTitle", isDone = false)
        val todoListItem2 = TodoItem(title = "testTitle2", isDone = false)
        todoItemDao.insert(todoListItem)
        todoItemDao.insert(todoListItem2)
        val listItem2 = todoItemDao.loadById(2)
        val subItem1 = TodoSubItem(parentId = listItem2.id, title = "sub item title1")
        todoSubItemDao.insert(subItem1)
        val subItem2 = TodoSubItem(parentId = listItem2.id, title = "sub item title1")
        todoSubItemDao.insert(subItem1)

        val subItems = todoSubItemDao.loadByParentId(2)
        assertTrue(subItems.size == 2)

        todoItemDao.delete(TodoItem(id = 2))
        val subItems2 = todoSubItemDao.loadByParentId(2)
        assertTrue(subItems2.isEmpty())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}