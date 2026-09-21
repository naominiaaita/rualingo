package com.app.rualingoapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.app.rualingoapplication.database.AppDatabase;
import com.app.rualingoapplication.database.PendingLog;

public class SessionManager {
    private static final String PREF_NAME = "RualingoPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_SECOND_NAME = "secondName";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DOB = "dateOfBirth";
    private static final String KEY_PROVINCE = "provinceOfOrigin";
    private static final String KEY_ROLE = "role";
    private static final String KEY_PROFILE_PIC = "profilePic";
    private static final String KEY_SELECTED_LANGUAGE = "selectedLanguage";
    private static final String KEY_XP = "xp";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_LAST_LOGIN_DATE = "lastLoginDate";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_ACTIVE = "isActive";
    private static final String KEY_TOTAL_CORRECT = "totalCorrect";
    private static final String KEY_TOTAL_ATTEMPTED = "totalAttempted";
    private static final String KEY_JWT_TOKEN = "jwtToken";
    private static final String KEY_CURRENT_LESSON_ID = "currentLessonId";
    private static final String KEY_SELECTED_COURSE_ID = "selectedCourseId";
    private static final String KEY_IS_NEW_USER = "isNewUser";

    private final SharedPreferences pref;
    private final Context context;

    public SessionManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createLoginSession(User user, String token) {
        Log.d("SessionManager", "Creating login session for: " + user.getUsername() + " with role: " + user.getRole());
        
        // Explicitly wipe previous session data
        pref.edit().clear().apply();
        
        SharedPreferences.Editor editor = pref.edit();
        
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getId() != null ? user.getId() : -1);
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_FIRST_NAME, user.getFirstName());
        editor.putString(KEY_SECOND_NAME, user.getSecondName());
        editor.putString(KEY_GENDER, user.getGender());
        editor.putString(KEY_DOB, user.getDateOfBirth());
        editor.putString(KEY_PROVINCE, user.getProvinceOfOrigin());
        editor.putString(KEY_SELECTED_LANGUAGE, user.getCurrentCourse());
        editor.putInt(KEY_STREAK, user.getStreak());
        
        // Set new user flag - if they don't have a language selected, they are likely new
        boolean isNew = user.getCurrentCourse() == null || user.getCurrentCourse().isEmpty();
        editor.putBoolean(KEY_IS_NEW_USER, isNew);
        
        if (token != null) {
            editor.putString(KEY_JWT_TOKEN, token);
        }
        
        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            role = "USER";
        }
        editor.putString(KEY_ROLE, role.toUpperCase());
        editor.putString(KEY_PROFILE_PIC, user.getProfilePicture());
        editor.putBoolean(KEY_IS_ACTIVE, user.getIsActive() != null ? user.getIsActive() : true);
        
        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        editor.putLong(KEY_LAST_LOGIN_DATE, today);
        
        editor.apply();
        Log.d("SessionManager", "Session saved. Saved Role: " + getRole());
    }

    public void setJwtToken(String token) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_JWT_TOKEN, token);
        editor.apply();
        Log.d("SessionManager", "JWT token stored");
    }

    public String getJwtToken() {
        return pref.getString(KEY_JWT_TOKEN, null);
    }

    public Long getUserId() { return pref.getLong(KEY_USER_ID, -1); }

    public boolean isLoggedIn() { return pref.getBoolean(KEY_IS_LOGGED_IN, false); }
    
    public boolean isNewUser() { return pref.getBoolean(KEY_IS_NEW_USER, true); }

    public void setNewUser(boolean isNew) {
        pref.edit().putBoolean(KEY_IS_NEW_USER, isNew).apply();
    }
    
    public void logout() { 
        Log.d("SessionManager", "Logging out user...");
        SharedPreferences.Editor editor = pref.edit();
        editor.clear(); 
        editor.apply(); 
        Log.d("SessionManager", "Logout successful");
    }

    public String getUsername() { return pref.getString(KEY_USERNAME, "Learner"); }
    public String getEmail() { return pref.getString(KEY_EMAIL, ""); }
    public String getFirstName() { return pref.getString(KEY_FIRST_NAME, ""); }
    public String getSecondName() { return pref.getString(KEY_SECOND_NAME, ""); }
    public String getGender() { return pref.getString(KEY_GENDER, ""); }
    public String getDateOfBirth() { return pref.getString(KEY_DOB, ""); }
    public String getProvinceOfOrigin() { return pref.getString(KEY_PROVINCE, ""); }
    
    public String getRole() { 
        // Read directly from pref to be sure
        String role = pref.getString(KEY_ROLE, "USER");
        Log.d("SessionManager", "Fetched role from Prefs: " + role);
        return role != null ? role.toUpperCase() : "USER";
    }

    public void updateProfile(User user) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_FIRST_NAME, user.getFirstName());
        editor.putString(KEY_SECOND_NAME, user.getSecondName());
        editor.putString(KEY_GENDER, user.getGender());
        editor.putString(KEY_DOB, user.getDateOfBirth());
        editor.putString(KEY_PROVINCE, user.getProvinceOfOrigin());
        editor.putString(KEY_PROFILE_PIC, user.getProfilePicture());
        if (user.getCurrentCourse() != null) {
            editor.putString(KEY_SELECTED_LANGUAGE, user.getCurrentCourse());
        }
        if (user.getIsActive() != null) {
            editor.putBoolean(KEY_IS_ACTIVE, user.getIsActive());
        }
        if (user.getStreak() > 0) {
            editor.putInt(KEY_STREAK, user.getStreak());
        }
        editor.apply();
    }

    public String getProfilePicture() { return pref.getString(KEY_PROFILE_PIC, ""); }
    public String getSelectedLanguage() { return pref.getString(KEY_SELECTED_LANGUAGE, ""); }
    
    public void setSelectedLanguage(String language) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_SELECTED_LANGUAGE, language);
        editor.apply();
    }

    public int getXP() { return pref.getInt(KEY_XP, 0); }
    public int getStreak() { return pref.getInt(KEY_STREAK, 0); }
    public int getLevel() { return pref.getInt(KEY_LEVEL, 1); }

    public void addXP(int xp) {
        int currentXp = getXP();
        int newXp = currentXp + xp;
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_XP, newXp);
        int newLevel = (newXp / 1000) + 1;
        editor.putInt(KEY_LEVEL, newLevel);
        editor.apply();
    }

    public void recordPerformance(int correct, int attempted, Long lessonId) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_TOTAL_CORRECT, pref.getInt(KEY_TOTAL_CORRECT, 0) + correct);
        editor.putInt(KEY_TOTAL_ATTEMPTED, pref.getInt(KEY_TOTAL_ATTEMPTED, 0) + attempted);
        editor.putLong(KEY_CURRENT_LESSON_ID, lessonId != null ? lessonId : -1L);
        editor.apply();

        Long userId = getUserId();
        if (userId != -1) {
            String action = "LESSON_COMPLETED";
            long timestamp = System.currentTimeMillis();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", action);
            payload.put("lessonId", lessonId);
            payload.put("exerciseId", null);
            payload.put("clientTimestamp", timestamp);

            RetrofitClient.getApiService().logUserActivity(userId, payload)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateProfile(response.body());
                            Log.d("ActivitySync", "Metrics pushed and streak synced: " + response.body().getStreak());
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("ActivitySync", "Failed to sync due to network state. Saving to local queue.");
                        saveLogLocally(userId, action, lessonId, null, timestamp);
                    }
                });
        }
    }

    private void saveLogLocally(Long userId, String action, Long lessonId, Long exerciseId, long timestamp) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            db.appDao().insertPendingLog(new PendingLog(userId, action, lessonId, exerciseId, timestamp));
            Log.d("SessionManager", "Log saved locally for later sync");
        }).start();
    }

    public int getAccuracy() {
        int total = pref.getInt(KEY_TOTAL_ATTEMPTED, 0);
        return total == 0 ? 0 : (pref.getInt(KEY_TOTAL_CORRECT, 0) * 100) / total;
    }

    public long getCurrentLessonId() {
        return pref.getLong(KEY_CURRENT_LESSON_ID, -1L);
    }

    public void setCurrentLessonId(long lessonId) {
        pref.edit().putLong(KEY_CURRENT_LESSON_ID, lessonId).apply();
    }

    public long getSelectedCourseId() {
        return pref.getLong(KEY_SELECTED_COURSE_ID, -1L);
    }

    public void setSelectedCourseId(long courseId) {
        pref.edit().putLong(KEY_SELECTED_COURSE_ID, courseId).apply();
    }
}
