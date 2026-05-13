package com.app.rualingoapplication;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Auth endpoints
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body User user);

    @GET("api/exercises")
    Call<List<Question>> getExercises();

    @POST("api/exercises")
    Call<Question> addQuestion(@Body Question question);

    @PUT("api/exercises/{id}")
    Call<Question> editExercise(@Path("id") Long id, @Body Question question);

    @DELETE("api/exercises/{id}")
    Call<Void> deleteExercise(@Path("id") Long id);

    @PATCH("api/exercises/{exerciseId}/lesson/{lessonId}")
    Call<Void> assignExerciseToLesson(@Path("exerciseId") Long exerciseId, @Path("lessonId") Long lessonId);

    // Lessons (CRUD)
    @GET("api/lessons")
    Call<List<Lesson>> getLessons();

    @POST("api/lessons")
    Call<Lesson> createLesson(@Body Lesson lesson);

    @PUT("api/lessons/{id}")
    Call<Lesson> updateLesson(@Path("id") Long id, @Body Lesson lesson);

    @DELETE("api/lessons/{id}")
    Call<Void> deleteLesson(@Path("id") Long id);

    // Languages (CRUD)
    @GET("api/languages")
    Call<List<LanguageModel>> getLanguages();

    @POST("api/languages")
    Call<LanguageModel> createLanguage(@Body LanguageModel language);

    @PUT("api/languages/{id}")
    Call<LanguageModel> updateLanguage(@Path("id") Long id, @Body LanguageModel language);

    @DELETE("api/languages/{id}")
    Call<Void> deleteLanguage(@Path("id") Long id);

    // Courses (CRUD)
    @GET("api/courses")
    Call<List<Course>> getCourses();

    @POST("api/courses")
    Call<Course> createCourse(@Body Course course);

    @PUT("api/courses/{id}")
    Call<Course> updateCourse(@Path("id") Long id, @Body Course course);

    @DELETE("api/courses/{id}")
    Call<Void> deleteCourse(@Path("id") Long id);

    @POST("api/auth/register")
    Call<AuthResponse> signup(@Body User user);

    // Users (CRUD)
    @GET("api/users")
    Call<List<User>> fetchAllUsers();

    @PUT("api/users/{id}")
    Call<User> editUser(@Path("id") Long id, @Body User user);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") Long id);

    @GET("api/activity-logs")
    Call<List<Map<String, Object>>> viewUserActivity(@Query("userId") Long userId);

    // Reports
    @GET("api/reports")
    Call<Map<String, Object>> generateReport();

    @GET("api/languages/health")
    Call<List<LanguageModel>> getLanguageHealth();

    // Vocabulary
    @GET("api/vocabulary")
    Call<List<VocabularyItem>> getVocabulary(@Query("courseId") Long courseId, @Query("topic") String topic);

    @POST("api/vocabulary")
    Call<VocabularyItem> createVocabulary(@Body VocabularyItem vocabulary);

    @PUT("api/vocabulary/{id}")
    Call<VocabularyItem> updateVocabulary(@Path("id") Long id, @Body VocabularyItem vocabulary);

    @DELETE("api/vocabulary/{id}")
    Call<Void> deleteVocabulary(@Path("id") Long id);

    // Quizzes
    @GET("api/quizzes")
    Call<List<Question>> getQuizzes();

    @POST("api/chat/ask")
    Call<ChatMessage> askRua(@Body ChatMessage message);
}
