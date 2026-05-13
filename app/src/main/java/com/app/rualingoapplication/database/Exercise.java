package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "exercises",
        foreignKeys = {
                @ForeignKey(entity = Lesson.class,
                        parentColumns = "id",
                        childColumns = "lesson_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Language.class,
                        parentColumns = "id",
                        childColumns = "language_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("lesson_id"), @Index("language_id")})
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String prompt;
    public List<String> options;
    public String correct_answer;
    public long lesson_id;
    public long language_id;

    public Exercise(String prompt, List<String> options, String correct_answer, long lesson_id, long language_id) {
        this.prompt = prompt;
        this.options = options;
        this.correct_answer = correct_answer;
        this.lesson_id = lesson_id;
        this.language_id = language_id;
    }
}
