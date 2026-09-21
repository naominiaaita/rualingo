package com.app.rualingoapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText firstNameET, secondNameET, dobET;
    private MaterialAutoCompleteTextView genderSpinner, provinceSpinner;
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
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_dark, genders);
        genderSpinner.setAdapter(genderAdapter);

        java.util.List<Province> provinces = new java.util.ArrayList<>();
        String[] provinceNames = getResources().getStringArray(R.array.png_provinces);
        
        for (String name : provinceNames) {
            int flagResId = R.drawable.png_flag; // Default global fallback
            
            // 1. Clean the name: lowercase, remove (brackets), replace spaces/hyphens with underscores
            String fileName = name.toLowerCase()
                    .replaceAll("\\s*\\([^)]*\\)", "") // Removes "(Simbu)", "(NCD)", etc.
                    .trim()
                    .replace(" ", "_")
                    .replace("-", "_")
                    + "_flag";
            
            // 2. Try to find the file dynamically by name
            int resId = getResources().getIdentifier(fileName, "drawable", getPackageName());
            
            if (resId != 0) {
                flagResId = resId;
            } else {
                // 3. Fallbacks or special handles
                String lower = name.toLowerCase();
                if (lower.contains("bougainville")) flagResId = R.drawable.flag_of_autonomous_region_of_bougainville;
                else if (lower.contains("chimbu")) flagResId = R.drawable.flag_of_chimbu;
                else if (lower.contains("eastern highlands")) flagResId = R.drawable.flag_of_eastern_highlands;
                else if (lower.contains("jiwaka")) flagResId = R.drawable.flag_of_jiwaka;
                else if (lower.contains("southern highlands")) flagResId = R.drawable.flag_of_southern_highlands_province;
                else if (lower.contains("hela")) flagResId = R.drawable.flag_of_hela;
                else if (lower.contains("new ireland")) flagResId = R.drawable.flag_of_new_ireland;
                else if (lower.contains("east sepik")) flagResId = R.drawable.flag_of_east_sepik;
                else if (lower.contains("enga")) flagResId = R.drawable.flag_of_enga;
                else if (lower.contains("gulf")) flagResId = R.drawable.flag_of_gulf_province;
                else if (lower.contains("morobe")) flagResId = R.drawable.flag_of_morobe;
                else if (lower.contains("oro")) flagResId = R.drawable.flag_of_flag_oro;
                else if (lower.contains("ncd")) flagResId = R.drawable.flag_of_ncd;
            }
            
            provinces.add(new Province(name, flagResId));
        }

        ProvinceAdapter provinceAdapter = new ProvinceAdapter(this, provinces);
        provinceSpinner.setAdapter(provinceAdapter);
        provinceSpinner.setThreshold(0); 
        provinceSpinner.setOnClickListener(v -> provinceSpinner.showDropDown());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Using Theme_Holo_Dialog to force the wheel picker style and match the dark theme
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Dialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    dobET.setText(date);
                }, year, month, day);

        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

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
