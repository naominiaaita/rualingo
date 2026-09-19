package com.app.rualingoapplication.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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
    
    @ColumnInfo(name = "language_id")
    public long languageId;
    
    public String languageName;
    public String flag;

    public Course() {}

    @Ignore
    public Course(String title, long languageId) {
        this.title = title;
        this.languageId = languageId;
    }
}
