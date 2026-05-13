package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses",
        foreignKeys = @ForeignKey(entity = Language.class,
                parentColumns = "id",
                childColumns = "language_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("language_id")})
public class Course {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public long language_id;

    public Course(String title, long language_id) {
        this.title = title;
        this.language_id = language_id;
    }
}