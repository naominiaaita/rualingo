package com.app.rualingoapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText firstNameET, secondNameET, dobET;
    private AutoCompleteTextView genderSpinner, provinceSpinner;
    private ImageView profileImage;
    private SessionManager sessionManager;
    private ApiService apiService;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    profileImage.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        RetrofitClient.setContext(this);
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        firstNameET = findViewById(R.id.firstNameEditText);
        secondNameET = findViewById(R.id.secondNameEditText);
        genderSpinner = findViewById(R.id.genderAutoComplete);
        dobET = findViewById(R.id.dobEditText);
        provinceSpinner = findViewById(R.id.provinceAutoComplete);
        profileImage = findViewById(R.id.editProfileImage);

        setupDropdowns();

        // Pre-fill
        firstNameET.setText(sessionManager.getFirstName());
        secondNameET.setText(sessionManager.getSecondName());
        genderSpinner.setText(sessionManager.getGender(), false);
        dobET.setText(sessionManager.getDateOfBirth());
        provinceSpinner.setText(sessionManager.getProvinceOfOrigin(), false);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveButton).setOnClickListener(v -> saveProfile());
        findViewById(R.id.changePhotoButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        dobET.setOnClickListener(v -> showDatePicker());
    }

    private void setupDropdowns() {
        String[] genders = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        genderSpinner.setAdapter(genderAdapter);

        String[] provinces = getResources().getStringArray(R.array.png_provinces);
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provinces);
        provinceSpinner.setAdapter(provinceAdapter);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    dobET.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveProfile() {
        User updatedUser = new User();
        updatedUser.setFirstName(Objects.requireNonNull(firstNameET.getText()).toString().trim());
        updatedUser.setSecondName(Objects.requireNonNull(secondNameET.getText()).toString().trim());
        updatedUser.setGender(genderSpinner.getText().toString().trim());
        updatedUser.setDateOfBirth(Objects.requireNonNull(dobET.getText()).toString().trim());
        updatedUser.setProvinceOfOrigin(provinceSpinner.getText().toString().trim());
        
        if (selectedImageUri != null) {
            updatedUser.setProfilePicture(selectedImageUri.toString());
        } else {
            updatedUser.setProfilePicture(sessionManager.getProfilePicture());
        }
        
        Long userId = sessionManager.getUserId();
        Log.d("EditProfile", "Updating profile for user ID: " + userId);
        
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found in session", Toast.LENGTH_SHORT).show();
            return;
        }
        
        apiService.editUser(userId, updatedUser).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.updateProfile(response.body());
                    Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = "Update Failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error += " " + response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Log.e("EditProfile", error);
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("EditProfile", "Network Error", t);
                Toast.makeText(EditProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
