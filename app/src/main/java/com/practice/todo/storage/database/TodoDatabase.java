package com.practice.todo.storage.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.practice.todo.storage.database.dao.TodoItemDao;
import com.practice.todo.storage.database.dao.TodoSubItemDao;
import com.practice.todo.storage.database.entity.TodoItem;
import com.practice.todo.storage.database.entity.TodoSubItem;

/**
 * 数据库类，定义有哪些实体类，和它对应的数据库操作类的获取方法
 *
 * 原理：
 * 这里面都是抽象方法，不需要自己去实现一个数据库，编译器会根据这里的注解自己去写一个数据库的实现。
 * 这种技术叫做动态代理，这里不需要深究，只需要知道它是用来简化代码的，我们只要根据Room的用法写出
 * 一个数据库的定义即可
 */
@Database(
        entities = {
                TodoItem.class,
                TodoSubItem.class
        }, version = 1
)
public abstract class TodoDatabase extends RoomDatabase {
    public abstract TodoItemDao todoItemDao();

    public abstract TodoSubItemDao todoSubItemDao();
}
