package com.practice.todo.storage.database.entity;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

// 这个注解告诉编译器，我要生成一个叫todo_item的表。
@Entity(tableName = "todo_item")
public final class TodoItem implements Parcelable {

    // 这个注解表示这个id是这个表的主键
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private String title = "";
    private boolean isDone = false;
    private String description = "";
    // ignore 的字段不在数据库中存储
    @Ignore
    private long remindTimeMillis = 0;
    @Ignore
    private Location remindLocation;

    public TodoItem(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getRemindTimeMillis() {
        return remindTimeMillis;
    }

    public void setRemindTimeMillis(long remindTimeMillis) {
        this.remindTimeMillis = remindTimeMillis;
    }

    public Location getRemindLocation() {
        return remindLocation;
    }

    public void setRemindLocation(Location remindLocation) {
        this.remindLocation = remindLocation;
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isDone=" + isDone +
                ", description='" + description + '\'' +
                ", remindTimeMillis=" + remindTimeMillis +
                ", remindLocation=" + remindLocation +
                '}';
    }

    public TodoItem copy() {
        TodoItem todoItem = new TodoItem(title);
        todoItem.description = this.description;
        todoItem.title = this.title;
        todoItem.id = this.id;
        todoItem.isDone = this.isDone;
        todoItem.remindTimeMillis = this.remindTimeMillis;
        todoItem.remindLocation = this.remindLocation;
        return todoItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoItem todoItem = (TodoItem) o;
        return id == todoItem.id &&
                isDone == todoItem.isDone &&
                remindTimeMillis == todoItem.remindTimeMillis &&
                Objects.equals(title, todoItem.title) &&
                Objects.equals(description, todoItem.description) &&
                Objects.equals(remindLocation, todoItem.remindLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, isDone, description, remindTimeMillis, remindLocation);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeByte(this.isDone ? (byte) 1 : (byte) 0);
        dest.writeString(this.description);
        dest.writeLong(this.remindTimeMillis);
        dest.writeParcelable(this.remindLocation, flags);
    }

    protected TodoItem(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.isDone = in.readByte() != 0;
        this.description = in.readString();
        this.remindTimeMillis = in.readLong();
        this.remindLocation = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Parcelable.Creator<TodoItem> CREATOR = new Parcelable.Creator<TodoItem>() {
        @Override
        public TodoItem createFromParcel(Parcel source) {
            return new TodoItem(source);
        }

        @Override
        public TodoItem[] newArray(int size) {
            return new TodoItem[size];
        }
    };
}
