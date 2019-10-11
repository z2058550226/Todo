package com.practice.todo.storage.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "todo_sub_item",
        foreignKeys = @ForeignKey(
                entity = TodoItem.class,
                parentColumns = {"id"},
                childColumns = {"parent_id"},
                onDelete = CASCADE
        )
)
public final class TodoSubItem implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "parent_id")
    private int parentId;
    private String title = "";
    private boolean isDone = false;

    public TodoSubItem(int parentId, String title) {
        this.parentId = parentId;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.parentId);
        dest.writeString(this.title);
        dest.writeByte(this.isDone ? (byte) 1 : (byte) 0);
    }

    protected TodoSubItem(Parcel in) {
        this.id = in.readInt();
        this.parentId = in.readInt();
        this.title = in.readString();
        this.isDone = in.readByte() != 0;
    }

    public static final Parcelable.Creator<TodoSubItem> CREATOR = new Parcelable.Creator<TodoSubItem>() {
        @Override
        public TodoSubItem createFromParcel(Parcel source) {
            return new TodoSubItem(source);
        }

        @Override
        public TodoSubItem[] newArray(int size) {
            return new TodoSubItem[size];
        }
    };
}
