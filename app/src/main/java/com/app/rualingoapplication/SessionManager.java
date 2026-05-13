package com.app.rualingoapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

    private final SharedPreferences pref;

    public SessionManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
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
        
        boolean success = editor.commit(); // Synchronous write
        Log.d("SessionManager", "Session saved success: " + success + ". Saved Role: " + getRole());
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
    
    public void logout() { 
        Log.d("SessionManager", "Logging out user...");
        SharedPreferences.Editor editor = pref.edit();
        editor.clear(); 
        boolean success = editor.commit(); 
        Log.d("SessionManager", "Logout successful: " + success);
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
        editor.apply();
    }

    public String getProfilePicture() { return pref.getString(KEY_PROFILE_PIC, ""); }
    public String getSelectedLanguage() { return pref.getString(KEY_SELECTED_LANGUAGE, "Tok Pisin"); }
    
    public void setSelectedLanguage(String language) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_SELECTED_LANGUAGE, language);
        editor.commit(); // Use commit for critical settings
    }

    public int getXP() { return pref.getInt(KEY_XP, 0); }
    public int getStreak() { return pref.getInt(KEY_STREAK, 1); }
    public int getLevel() { return pref.getInt(KEY_LEVEL, 1); }

    public void addXP(int xp) {
        int currentXp = getXP();
        int newXp = currentXp + xp;
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_XP, newXp);
        int newLevel = (newXp / 1000) + 1;
        editor.putInt(KEY_LEVEL, newLevel);
        editor.commit();
    }

    public void recordPerformance(int correct, int attempted) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_TOTAL_CORRECT, pref.getInt(KEY_TOTAL_CORRECT, 0) + correct);
        editor.putInt(KEY_TOTAL_ATTEMPTED, pref.getInt(KEY_TOTAL_ATTEMPTED, 0) + attempted);
        editor.commit();
    }

    public int getAccuracy() {
        int total = pref.getInt(KEY_TOTAL_ATTEMPTED, 0);
        return total == 0 ? 0 : (pref.getInt(KEY_TOTAL_CORRECT, 0) * 100) / total;
    }
}
