package com.example.kcaloryfer.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posture_days")
public class PostureDay {

    @PrimaryKey
    @NonNull
    public String date;

    public int count;

    public long lastAddedTime;

    public PostureDay(@NonNull String date, int count, long lastAddedTime) {
        this.date = date;
        this.count = count;
        this.lastAddedTime = lastAddedTime;
    }
}