package com.app.rualingoapplication;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.app.rualingoapplication.database.AppDatabase;
import com.app.rualingoapplication.database.PendingLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting sync work...");
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<PendingLog> pendingLogs = db.appDao().getAllPendingLogs();

        if (pendingLogs.isEmpty()) {
            return Result.success();
        }

        ApiService apiService = RetrofitClient.getApiService();
        boolean allSuccess = true;

        for (PendingLog log : pendingLogs) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("action", log.getAction());
                payload.put("lessonId", log.getLessonId());
                payload.put("exerciseId", log.getExerciseId());
                payload.put("clientTimestamp", log.getClientTimestamp());

                Response<User> response = apiService.logUserActivity(log.getUserId(), payload).execute();
                if (response.isSuccessful()) {
                    db.appDao().deletePendingLog(log.getId());
                } else {
                    allSuccess = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to sync log: " + log.getId(), e);
                allSuccess = false;
            }
        }

        return allSuccess ? Result.success() : Result.retry();
    }
}
