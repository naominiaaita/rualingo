package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary",
        foreignKeys = @ForeignKey(entity = Course.class,
                parentColumns = "id",
                childColumns = "course_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("course_id")})
public class Vocabulary {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String word;
    public String translation;
    public long course_id;

    public Vocabulary(String word, String translation, long course_id) {
        this.word = word;
        this.translation = translation;
        this.course_id = course_id;
    }
}
