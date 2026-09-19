package com.app.rualingoapplication.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "lessons",
        foreignKeys = {
                @ForeignKey(entity = Course.class,
                        parentColumns = "id",
                        childColumns = "course_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("course_id")})
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    
    @ColumnInfo(name = "course_id")
    public long courseId;
    
    public String description;
    public String topic;

    public Lesson() {}

    @Ignore
    public Lesson(String title, long courseId) {
        this.title = title;
        this.courseId = courseId;
    }
}
