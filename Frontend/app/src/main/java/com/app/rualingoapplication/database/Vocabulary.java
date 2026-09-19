package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary",
        foreignKeys = @ForeignKey(entity = Lesson.class,
                parentColumns = "id",
                childColumns = "lesson_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("lesson_id")})
public class Vocabulary {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String word;
    public String translation;
    public long lesson_id;

    public Vocabulary() {}

    @Ignore
    public Vocabulary(String word, String translation, long lesson_id) {
        this.word = word;
        this.translation = translation;
        this.lesson_id = lesson_id;
    }
}
