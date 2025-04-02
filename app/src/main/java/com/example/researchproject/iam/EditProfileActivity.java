package com.example.researchproject.iam;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import android.Manifest;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile, btnUpload;
    private EditText etNickname, etBirthdate;
    private Spinner spinnerGender;
    private Button btnSave;
    private Uri imageUri;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752";
    private ProgressDialog progressDialog;

    private String[] genderOptions = {"Nam", "Nữ", "Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages").child(auth.getCurrentUser().getUid() + ".jpg");

        // Ánh xạ view
        imgProfile = findViewById(R.id.img_profile);
        btnUpload = findViewById(R.id.btn_upload);
        etNickname = findViewById(R.id.et_nickname);
        etBirthdate = findViewById(R.id.et_birthdate);
        spinnerGender = findViewById(R.id.spinner_gender);
        btnSave = findViewById(R.id.btn_save);
        // Thiết lập Spinner giới tính
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genderOptions);
        spinnerGender.setAdapter(genderAdapter);


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        imgProfile.setImageURI(imageUri); // Hiển thị ảnh đã chọn
                    }
                }
        );
        // Nút chọn ảnh đại diện
        btnUpload.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openFileChooser();
            }
        });

        // Nút lưu thông tin
        btnSave.setOnClickListener(v -> saveUserData());



    // Chọn ngày sinh
        etBirthdate.setOnClickListener(v -> showDatePicker());

        btnUpload.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveUserData());

        // Load dữ liệu người dùng từ Firebase
        loadUserData();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        userRef.get().addOnSuccessListener(snapshot -> {
            etNickname.setText(snapshot.child("nickname").getValue(String.class));
            etBirthdate.setText(snapshot.child("birthdate").getValue(String.class));
            // Đặt giới tính đã lưu vào Spinner
            String gender = snapshot.child("gender").getValue(String.class);
            if (gender != null) {
                int index = java.util.Arrays.asList(genderOptions).indexOf(gender);
                if (index >= 0) spinnerGender.setSelection(index);
            }

            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(imgProfile);
            }

        });
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Lọc chỉ chọn file ảnh
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Chỉ hiện các file có thể mở được
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }

    // Xử lý khi người dùng cấp quyền truy cập bộ nhớ
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền truy cập bộ nhớ để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openFileChooser();
        }
    }


    private void saveUserData() {
        progressDialog = ProgressDialog.show(this, "Uploading", "Please wait...", true);

        if (imageUri != null) {
            uploadImageToImgur(imageUri, this::updateUserProfile, errorMessage -> {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            updateUserProfile(null);
        }
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etBirthdate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    private void updateUserProfile(String imageUrl) {
        String nickname = etNickname.getText().toString();
        String birthdate = etBirthdate.getText().toString();
        String selectedGender = spinnerGender.getSelectedItem().toString();

        userRef.child("nickname").setValue(nickname);
        userRef.child("birthdate").setValue(birthdate);
        userRef.child("gender").setValue(selectedGender);
        if (imageUrl != null) {
            userRef.child("profileImageUrl").setValue(imageUrl);
        }

        progressDialog.dismiss();
        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent().putExtra("updated", true));
        finish();
    }

    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                OkHttpClient client = new okhttp3.OkHttpClient.Builder().build(); // Sửa lỗi tại đây
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", encodedImage)
                        .build();
                Request request = new okhttp3.Request.Builder() // Sửa lỗi tại đây
                        .url("https://api.imgur.com/3/image")
                        .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    String imageUrl = jsonObject.getJSONObject("data").getString("link");
                    runOnUiThread(() -> successListener.onSuccess(imageUrl));
                } else {
                    runOnUiThread(() -> failureListener.onFailure("Upload failed"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
            }
        }).start();
    }

    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }

    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }



}
