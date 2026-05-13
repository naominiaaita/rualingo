package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "lessons",
        foreignKeys = {
                @ForeignKey(entity = Course.class,
                        parentColumns = "id",
                        childColumns = "course_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Language.class,
                        parentColumns = "id",
                        childColumns = "language_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("course_id"), @Index("language_id")})
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public long course_id;
    public long language_id;

    public Lesson(String title, long course_id, long language_id) {
        this.title = title;
        this.course_id = course_id;
        this.language_id = language_id;
    }
}
