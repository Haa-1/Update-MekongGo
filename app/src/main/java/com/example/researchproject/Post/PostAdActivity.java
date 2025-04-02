package com.example.researchproject.Post;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import com.example.researchproject.R;
import com.example.researchproject.fragment.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import androidx.annotation.NonNull;
public class PostAdActivity extends AppCompatActivity {
    private EditText edtAdTitle;
    private ImageView imgAd;
    private Button btnSelectImage, btnPostAd;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752"; // Thay bằng Client ID của bạn
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);
        // 🟢 Ánh xạ View
        edtAdTitle = findViewById(R.id.edtAdTitle);
        imgAd = findViewById(R.id.imgAd);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnPostAd = findViewById(R.id.btnPostAd);
        Button btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(v -> {
            Intent intent = new Intent(PostAdActivity.this, PostActivity.class);
            startActivity(intent);
        });

        // 🟡 Kết nối Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Ads");
        // 📁 Chọn hình ảnh
        btnSelectImage.setOnClickListener(v -> openFileChooser());
        // 🚀 Đăng quảng cáo
        btnPostAd.setOnClickListener(v -> uploadAd());
    }
    // ✅ Hàm mở bộ nhớ để chọn ảnh
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    // ✅ Nhận kết quả sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgAd); // Hiển thị ảnh đã chọn
        }
    }
    // Xử lý đăng bài
    private void uploadAd() {
        String adTitle = edtAdTitle.getText().toString().trim();

        if (adTitle.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và chọn hình ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng tin...");
        progressDialog.show();

        // Giả sử uid đã có sẵn từ quá trình đăng nhập
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userEmail = snapshot.child("email").getValue(String.class); // ✅ Lấy userEmail

                if (imageUri != null) {
                    uploadImageToImgur(imageUri, (imageUrl) -> {
                        saveAdToFirebase(adTitle, imageUrl, uid, userEmail); // Cập nhật URL ảnh vào TextView
                        progressDialog.dismiss();
                        Toast.makeText(PostAdActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }, (errorMessage) -> {
                        progressDialog.dismiss();
                        Toast.makeText(PostAdActivity.this, "Lỗi khi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    saveAdToFirebase(adTitle, "", uid, userEmail);
                    progressDialog.dismiss();
                    Toast.makeText(PostAdActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(PostAdActivity.this, "Lỗi khi lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Lưu thông tin bài đăng vào Firebase
    private void saveAdToFirebase(String title,  String imageUrl, String uid, String userEmail) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Ads");
        String adId = databaseReference.push().getKey(); // ✅ Tạo ID duy nhất cho bài đăng
        long timestamp = System.currentTimeMillis(); // ✅ Lấy timestamp hiện tại
        if (adId != null) {
            HashMap<String, Object> postMap = new HashMap<>();
            postMap.put("title", title);
            postMap.put("adId", adId);
            postMap.put("imageUrl", imageUrl);
            postMap.put("timestamp", timestamp); // ✅ Thêm timestamp
            postMap.put("uid", uid); // ✅ Thêm uid
            postMap.put("userEmail", userEmail); // ✅ Thêm userEmail
            Log.d("FirebaseSave", "Dữ liệu đang lưu: " + postMap.toString()); // Log dữ liệu trước khi lưu
            databaseReference.child(adId).setValue(postMap)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseSave", "Đăng bài thành công!");
                        Toast.makeText(PostAdActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                        // ✅ Quay lại danh sách bài đăng & làm mới GridView
                        Intent intent = new Intent(PostAdActivity.this, HomeFragment.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Log.e("FirebaseSave", "Lỗi khi đăng bài: " + e.getMessage()));
        }
    }
    // ✅ Upload hình ảnh lên Imgur
    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", encodedImage)
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            String imageUrl = jsonObject.getJSONObject("data").getString("link");
                            runOnUiThread(() -> successListener.onSuccess(imageUrl));
                        } catch (JSONException e) {
                            runOnUiThread(() -> failureListener.onFailure("Lỗi xử lý dữ liệu từ Imgur"));
                        }
                    } else {
                        runOnUiThread(() -> failureListener.onFailure("Lỗi tải ảnh lên Imgur"));
                    }
                }
            });
        } catch (Exception e) {
            failureListener.onFailure(e.getMessage());
        }
    }
    // ✅ Interfaces để xử lý kết quả upload
    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }
    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }
}