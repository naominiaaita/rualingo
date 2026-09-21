package com.app.rualingoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, emailEditText, passwordEditText;
    private MaterialButton signupSubmitButton;
    private ApiService apiService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        RetrofitClient.setContext(this);
        mAuth = FirebaseAuth.getInstance();
        apiService = RetrofitClient.getApiService();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupSubmitButton = findViewById(R.id.signupSubmitButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        signupSubmitButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
            String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            setLoading(true);
            performRegistration(username, email, password);
        });
    }

    private void setLoading(boolean isLoading) {
        signupSubmitButton.setEnabled(!isLoading);
        signupSubmitButton.setText(isLoading ? "PROCESSING..." : "CREATE ACCOUNT");
        signupSubmitButton.setAlpha(isLoading ? 0.7f : 1.0f);
    }

    private void performRegistration(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        sendVerificationEmail(username, email, password, user);
                    }
                } else {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        // Firebase account exists. If backend doesn't have it, we should re-sync.
                        Log.d("SignupActivity", "Firebase user already exists, attempting to sign in and sync.");
                        mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(signInTask -> {
                                if (signInTask.isSuccessful() && mAuth.getCurrentUser() != null) {
                                    FirebaseUser existingUser = mAuth.getCurrentUser();
                                    existingUser.reload().addOnCompleteListener(reloadTask -> {
                                        FirebaseUser refreshedUser = mAuth.getCurrentUser();
                                        if (reloadTask.isSuccessful() && refreshedUser != null && refreshedUser.isEmailVerified()) {
                                            syncWithBackend(username, email, password, refreshedUser);
                                        } else if (refreshedUser != null) {
                                            sendVerificationEmail(username, email, password, refreshedUser);
                                        } else {
                                            setLoading(false);
                                            showCollisionDialog(email);
                                        }
                                    });
                                } else {
                                    setLoading(false);
                                    showCollisionDialog(email);
                                }
                            });
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Firebase Error: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void sendVerificationEmail(String username, String email, String password, FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setLoading(false);
                Exception e = task.getException();
                Log.e("SignupActivity", "sendEmailVerification failed", e);
                Toast.makeText(this, "Could not send verification email: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                return;
            }

            setLoading(false);
            showVerificationDialog(username, email, password, user);
        });
    }

    private void showVerificationDialog(String username, String email, String password, FirebaseUser user) {
        new AlertDialog.Builder(this)
            .setTitle("Verify your email")
            .setMessage("We sent a verification link to " + email + ". Verify it before continuing.")
            .setPositiveButton("I verified it", (dialog, which) -> {
                user.reload().addOnCompleteListener(reloadTask -> {
                    FirebaseUser refreshedUser = mAuth.getCurrentUser();
                    if (reloadTask.isSuccessful() && refreshedUser != null && refreshedUser.isEmailVerified()) {
                        setLoading(true);
                        syncWithBackend(username, email, password, refreshedUser);
                    } else {
                        Toast.makeText(this, "Your email is not verified yet.", Toast.LENGTH_LONG).show();
                        showVerificationDialog(username, email, password, user);
                    }
                });
            })
            .setNeutralButton("Resend email", (dialog, which) -> {
                user.sendEmailVerification().addOnCompleteListener(resendTask -> {
                    if (!resendTask.isSuccessful()) {
                        Log.e("SignupActivity", "resend verification failed", resendTask.getException());
                    }
                    Toast.makeText(this,
                        resendTask.isSuccessful() ? "Verification email resent." : "Could not resend: " + (resendTask.getException() != null ? resendTask.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_LONG).show();
                });
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                mAuth.signOut();
                finish();
            })
            .setCancelable(false)
            .show();
    }

    private void showCollisionDialog(String email) {
        new AlertDialog.Builder(this)
            .setTitle("Account Already Exists")
            .setMessage("The email " + email + " is already registered. If you forgot your password, please use the login screen.")
            .setPositiveButton("Go to Login", (dialog, which) -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void syncWithBackend(String username, String email, String password, FirebaseUser firebaseUser) {
        firebaseUser.getIdToken(true).addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful() || tokenTask.getResult() == null) {
                setLoading(false);
                Toast.makeText(this, "Could not authenticate with Firebase.", Toast.LENGTH_LONG).show();
                return;
            }

            String role = BuildConfig.FLAVOR_TYPE;
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setFirebaseIdToken(tokenTask.getResult().getToken());
            user.setRole(role);
            user.setFirstName(username);
            user.setSecondName("User");
            user.setGender("Other");
            user.setDateOfBirth("2000-01-01");
            user.setProvinceOfOrigin("NCD");

            apiService.signup(user).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    SessionManager sessionManager = new SessionManager(SignupActivity.this);
                    
                    User loggedInUser = new User();
                    loggedInUser.setId(authResponse.getUserId());
                    loggedInUser.setUsername(authResponse.getUsername());
                    loggedInUser.setEmail(authResponse.getEmail());
                    loggedInUser.setRole(authResponse.getRole());
                    loggedInUser.setStreak(authResponse.getStreak());
                    
                    sessionManager.createLoginSession(loggedInUser, authResponse.getToken());
                    
                    // Proceed to language selection
                    Intent intent = new Intent(SignupActivity.this, LanguageSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 409) {
                    // User already exists in backend, so just log them in automatically
                    Log.d("SignupActivity", "Conflict 409: User exists in backend. Attempting auto-login...");
                    autoLoginAfterSync(email, password);
                } else {
                    String error = "Backend Sync Failed (" + response.code() + ")";
                    try { if (response.errorBody() != null) error += ": " + response.errorBody().string(); } catch (Exception ignored) {}
                    Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show();
                    Log.e("SignupActivity", "Sync failed: " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, "Network Error during sync: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
            });
        });
    }

    private void autoLoginAfterSync(String identifier, String password) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            setLoading(false);
            showCollisionDialog(identifier);
            return;
        }

        firebaseUser.getIdToken(true).addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful() || tokenTask.getResult() == null) {
                setLoading(false);
                showCollisionDialog(identifier);
                return;
            }

            String role = BuildConfig.FLAVOR_TYPE;
            User user = new User(identifier, identifier, password, role);
            user.setFirebaseIdToken(tokenTask.getResult().getToken());

            apiService.login(user).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    SessionManager sessionManager = new SessionManager(SignupActivity.this);
                    
                    User loggedInUser = new User();
                    loggedInUser.setId(authResponse.getUserId());
                    loggedInUser.setUsername(authResponse.getUsername());
                    loggedInUser.setEmail(authResponse.getEmail());
                    loggedInUser.setRole(authResponse.getRole());
                    loggedInUser.setStreak(authResponse.getStreak());
                    
                    sessionManager.createLoginSession(loggedInUser, authResponse.getToken());
                    
                    // If they have a course, go home, otherwise selection
                    Intent intent;
                    if (authResponse.isNewUser()) {
                        intent = new Intent(SignupActivity.this, LanguageSelectionActivity.class);
                    } else {
                        intent = new Intent(SignupActivity.this, HomeActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showCollisionDialog(identifier);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                showCollisionDialog(identifier);
            }
            });
        });
    }
}
