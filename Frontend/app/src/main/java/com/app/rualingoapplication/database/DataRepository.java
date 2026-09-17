package com.app.rualingoapplication.database;

import android.content.Context;
import android.util.Log;
import com.app.rualingoapplication.ApiService;
import com.app.rualingoapplication.LanguageModel;
import com.app.rualingoapplication.RetrofitClient;
import com.app.rualingoapplication.Question;
import com.app.rualingoapplication.VocabularyItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataRepository {
    private static final String TAG = "DataRepository";
    private final AppDao appDao;
    private final ApiService apiService;

    public DataRepository(Context context) {
        this.appDao = AppDatabase.getDatabase(context).appDao();
        this.apiService = RetrofitClient.getApiService();
    }

    public List<com.app.rualingoapplication.Course> getCoursesForLanguage(String targetLanguage) {
        List<com.app.rualingoapplication.Course> result = new ArrayList<>();
        try {
            retrofit2.Response<List<com.app.rualingoapplication.Course>> response = apiService.getCourses().execute();
            if (response.isSuccessful() && response.body() != null) {
                for (com.app.rualingoapplication.Course c : response.body()) {
                    Course localCourse = new Course();
                    localCourse.id = c.getId();
                    localCourse.title = c.getTitle();
                    localCourse.languageName = c.getLanguageName();
                    localCourse.languageId = c.getLanguageId() != null ? c.getLanguageId() : 0L;
                    localCourse.flag = c.getFlag();
                    try {
                        appDao.insertCourse(localCourse);
                    } catch (Exception ignored) {}
                }
                return response.body();
            }
        } catch (IOException e) {
            Log.e(TAG, "Offline mode for courses", e);
        }

        // Fallback to local DB
        for (Course local : appDao.getAllLanguages().isEmpty() ? new ArrayList<Course>() : appDao.getCoursesByLanguage(0L)) {
            // Mapping fallback structural properties
        }
        return result;
    }

    public List<com.app.rualingoapplication.Lesson> getLessons(long courseId) {
        try {
            retrofit2.Response<List<com.app.rualingoapplication.Lesson>> response = apiService.getLessons(null).execute();
            if (response.isSuccessful() && response.body() != null) {
                List<com.app.rualingoapplication.Lesson> courseLessons = new ArrayList<>();
                for (com.app.rualingoapplication.Lesson l : response.body()) {
                    Lesson local = new Lesson();
                    local.id = l.getId();
                    local.courseId = l.getCourseId();
                    local.title = l.getTitle();
                    local.description = l.getDescription();
                    local.topic = l.getTopic();
                    try {
                        appDao.insertLesson(local);
                    } catch (Exception ignored) {}
                    
                    if (Objects.equals(l.getCourseId(), courseId)) {
                        courseLessons.add(l);
                    }
                }
                return courseLessons;
            }
        } catch (IOException e) {
            Log.e(TAG, "Offline mode for lessons", e);
        }

        List<com.app.rualingoapplication.Lesson> localLessons = new ArrayList<>();
        for (Lesson l : appDao.getLessonsByCourse(courseId)) {
            com.app.rualingoapplication.Lesson remote = new com.app.rualingoapplication.Lesson();
            remote.setId(l.id);
            remote.setCourseId(l.courseId);
            remote.setTitle(l.title);
            remote.setDescription(l.description);
            remote.setTopic(l.topic);
            localLessons.add(remote);
        }
        return localLessons;
    }
}
