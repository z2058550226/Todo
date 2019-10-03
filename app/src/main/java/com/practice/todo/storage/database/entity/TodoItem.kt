@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.practice.todo.storage.database.entity

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "todo_item")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var isDone: Boolean = false,
    @Ignore var remindTimeMillis: Long = 0,
    @Ignore var remindLocation: Location? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readParcelable(Location::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeByte(if (isDone) 1 else 0)
        parcel.writeLong(remindTimeMillis)
        parcel.writeParcelable(remindLocation, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TodoItem> {
        override fun createFromParcel(parcel: Parcel): TodoItem {
            return TodoItem(parcel)
        }

        override fun newArray(size: Int): Array<TodoItem?> {
            return arrayOfNulls(size)
        }
    }
}