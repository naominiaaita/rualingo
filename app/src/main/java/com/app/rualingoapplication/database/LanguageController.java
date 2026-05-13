package com.app.rualingoapplication.database;

import android.content.Context;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sample Controller class for fetching data based on language.
 * Demonstrates both Database access and ResourceBundle localization.
 */
public class LanguageController {

    private final AppDao appDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LanguageController(Context context) {
        this.appDao = AppDatabase.getDatabase(context).appDao();
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }

    /**
     * Fetches courses for a specific language ID from the Room database.
     */
    public void fetchCoursesForLanguage(long langId, DataCallback<List<Course>> callback) {
        executor.execute(() -> {
            List<Course> courses = appDao.getCoursesByLanguage(langId);
            callback.onDataLoaded(courses);
        });
    }

    /**
     * Sample of how to load a ResourceBundle for localization in Java.
     * Note: On Android, native 'strings.xml' is preferred, but ResourceBundle 
     * is used here as per requirements.
     * 
     * @param languageCode "tp" for Tok Pisin, "ho" for Motu, etc.
     */
    public String getLocalizedString(String key, String languageCode) {
        try {
            Locale locale = new Locale(languageCode);
            // Expects strings_tp.properties, strings_ho.properties etc. in resources
            ResourceBundle bundle = ResourceBundle.getBundle("strings", locale);
            return bundle.getString(key);
        } catch (Exception e) {
            return "Key not found";
        }
    }
}
