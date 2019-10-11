package com.practice.todo.util;

import android.location.Location;

public final class InMemoryCache {

    public static final class RemindTimeCache {
        public static long remindTimeMills;
        public static int itemDbId;
    }

    public static final class RemindLocationCache {
        public static Location remindLocation;
        public static int itemDbId;
    }
}
