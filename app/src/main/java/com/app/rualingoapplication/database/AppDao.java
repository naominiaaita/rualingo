package com.app.rualingoapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AppDao {
    @Insert
    long insertLanguage(Language language);

    @Insert
    long insertCourse(Course course);

    @Insert
    long insertLesson(Lesson lesson);

    @Insert
    long insertExercise(Exercise exercise);

    @Insert
    long insertVocabulary(Vocabulary vocabulary);

    @Query("SELECT * FROM languages")
    List<Language> getAllLanguages();

    @Query("SELECT * FROM courses WHERE language_id = :langId")
    List<Course> getCoursesByLanguage(long langId);

    @Query("SELECT * FROM vocabulary WHERE course_id = :courseId")
    List<Vocabulary> getVocabularyByCourse(long courseId);

    @Query("SELECT * FROM lessons WHERE course_id = :courseId")
    List<Lesson> getLessonsByCourse(long courseId);

    @Query("SELECT * FROM exercises WHERE lesson_id = :lessonId")
    List<Exercise> getExercisesByLesson(long lessonId);

    @Query("SELECT * FROM exercises")
    List<Exercise> getAllExercises();
}
