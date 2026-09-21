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
            provinces.add(new Province(name, resolveProvinceFlag(name)));
        }

        ProvinceAdapter provinceAdapter = new ProvinceAdapter(this, provinces);
        provinceSpinner.setAdapter(provinceAdapter);
        provinceSpinner.setThreshold(0); 
        provinceSpinner.setOnClickListener(v -> provinceSpinner.showDropDown());
    }

    private int resolveProvinceFlag(String provinceName) {
        String normalized = provinceName.toLowerCase(java.util.Locale.ROOT).trim();
        String resourceName;

        if (normalized.contains("bougainville")) {
            resourceName = "flag_of_autonomous_region_of_bougainville";
        } else if (normalized.equals("central")) {
            resourceName = "central_flag";
        } else if (normalized.contains("chimbu")) {
            resourceName = "flag_of_chimbu";
        } else if (normalized.contains("eastern highlands")) {
            resourceName = "flag_of_eastern_highlands";
        } else if (normalized.contains("east new britain")) {
            resourceName = "flag_of_east_new_britain";
        } else if (normalized.contains("east sepik")) {
            resourceName = "flag_of_east_sepik";
        } else if (normalized.equals("enga")) {
            resourceName = "flag_of_enga";
        } else if (normalized.equals("gulf")) {
            resourceName = "flag_of_gulf_province";
        } else if (normalized.equals("hela")) {
            resourceName = "flag_of_hela";
        } else if (normalized.equals("jiwaka")) {
            resourceName = "flag_of_jiwaka";
        } else if (normalized.equals("madang")) {
            resourceName = "flag_of_madang";
        } else if (normalized.equals("manus")) {
            resourceName = "flag_of_manus";
        } else if (normalized.equals("milne bay")) {
            resourceName = "flag_of_milne_bay";
        } else if (normalized.equals("morobe")) {
            resourceName = "flag_of_morobe";
        } else if (normalized.equals("new ireland")) {
            resourceName = "flag_of_new_ireland";
        } else if (normalized.contains("oro")) {
            resourceName = "flag_of_flag_oro";
        } else if (normalized.contains("ncd")) {
            resourceName = "flag_of_ncd";
        } else if (normalized.contains("southern highlands")) {
            resourceName = "flag_of_southern_highlands_province";
        } else if (normalized.equals("western (fly)")) {
            resourceName = "western";
        } else if (normalized.equals("western highlands")) {
            resourceName = "western_higlands";
        } else if (normalized.contains("west new britain")) {
            resourceName = "flag_of_west_new_britain";
        } else if (normalized.contains("west sepik") || normalized.contains("sandaun")) {
            resourceName = "flag_of_sandaun";
        } else {
            return R.drawable.png_flag;
        }

        int flagResId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        return flagResId != 0 ? flagResId : R.drawable.png_flag;
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
