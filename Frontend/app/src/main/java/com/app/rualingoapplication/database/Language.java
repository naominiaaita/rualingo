package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "languages")
public class Language {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;

    public Language() {}

    @Ignore
    public Language(String name) {
        this.name = name;
    }
}
