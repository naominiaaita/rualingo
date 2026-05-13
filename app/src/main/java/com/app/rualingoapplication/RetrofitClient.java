package com.app.rualingoapplication;

import android.content.Context;
import android.util.Log;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import androidx.annotation.NonNull;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;
    private static Context context = null;

    public static void setContext(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new TokenInterceptor())
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        Log.d("RetrofitClient", "--> Sending " + request.method() + " to " + request.url());
                        if (request.body() != null) {
                            okio.Buffer buffer = new okio.Buffer();
                            request.body().writeTo(buffer);
                            Log.d("RetrofitClient", "Request Body: " + buffer.readUtf8());
                        }
                        
                        Response response = chain.proceed(request);
                        
                        if (response.code() == 401) {
                            Log.e("RetrofitClient", "!!! AUTH ERROR 401 !!! Token invalid or expired.");
                            if (context != null) {
                                // Logout and force re-login
                                SessionManager sm = new SessionManager(context);
                                sm.logout();
                                android.content.Intent intent = new android.content.Intent(context, MainActivity.class);
                                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        }

                        if (!response.isSuccessful()) {
                            Log.e("RetrofitClient", "<-- Error " + response.code() + " from " + request.url());
                            if (response.body() != null) {
                                String errorBody = response.peekBody(Long.MAX_VALUE).string();
                                Log.e("RetrofitClient", "Error Body: " + errorBody);
                            }
                        } else {
                            Log.d("RetrofitClient", "<-- Success " + response.code());
                        }
                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    static class TokenInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws java.io.IOException {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");

            // Only add Token if it exists and we aren't hitting the auth endpoints
            String path = originalRequest.url().encodedPath();
            if (context != null && !path.contains("/api/auth/")) {
                SessionManager sessionManager = new SessionManager(context);
                String token = sessionManager.getJwtToken();
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                    Log.d("RetrofitClient", "Added Auth token to request");
                } else {
                    Log.w("RetrofitClient", "No token found in SessionManager for authenticated request");
                }
            } else if (context == null) {
                Log.e("RetrofitClient", "Context is null! Cannot add auth token.");
            }

            return chain.proceed(requestBuilder.build());
        }
    }
}
