package com.example.researchproject.Post;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;


import com.example.researchproject.Post.PostAdActivity;
import com.example.researchproject.R;
import com.example.researchproject.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;

public class PostActivity extends AppCompatActivity {
    private EditText edtTitle, edtServiceInfo, edtPrice, edtRentalTime, edtAddress, edtContact;
    private Button  btnPost;
    private ImageView imgService;
    private Uri imageUri;
    private Button btnUploadImage,btnPostAd;
    private TextView txtImageUrl;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752"; // Thay bằng Client ID của bạn
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_news);
        // Ánh xạ giao diện
        edtTitle = findViewById(R.id.edtTitle);
        edtServiceInfo = findViewById(R.id.edtServiceInfo);
        edtPrice = findViewById(R.id.edtPrice);
        edtRentalTime = findViewById(R.id.edtRentalTime);
        edtAddress = findViewById(R.id.edtAddress);
        edtContact = findViewById(R.id.edtContact);
        btnPost = findViewById(R.id.btnPost);
        imgService = findViewById(R.id.imgService);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        txtImageUrl = findViewById(R.id.txtImageUrl);
        Button btnPostAd = findViewById(R.id.btnPostAd);
        btnPostAd.setOnClickListener(v -> {
            Intent intent = new Intent(PostActivity.this, PostAdActivity.class);
            startActivity(intent);
        });
        btnUploadImage.setOnClickListener(v -> openFileChooser());
        btnUploadImage.setOnClickListener(v -> checkStoragePermission());
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

// Kiểm tra quyền truy cập bộ nhớ trước khi chọn ảnh
        btnUploadImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openFileChooser();
            }
        });
//        ZaloPaySDK.init(2553, Environment.SANDBOX);
//        StrictMode.ThreadPolicy policy = new
//                StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        // Xử lý đăng bài
        btnPost.setOnClickListener(v -> {
//           processZaloPayPayment();
            uploadPost();
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Lọc chỉ chọn file ảnh
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Chỉ hiện các file có thể mở được
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }
    // Xử lý kết quả sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Lưu URI vào biến toàn cục
            Glide.with(this).load(imageUri).into(imgService); // Hiển thị ảnh đã chọn
        }
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

    // Upload ảnh lên Imgur
    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
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
            OkHttpClient client = new OkHttpClient();
            // ✅ Đúng định dạng multipart/form-data cho API Imgur
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", encodedImage)
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();
            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = Objects.requireNonNull(response.body()).string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String imageUrl = jsonObject.getJSONObject("data").getString("link");
                        Log.d("ImgurUpload", "Ảnh tải lên thành công: " + imageUrl);
                        runOnUiThread(() -> successListener.onSuccess(imageUrl));
                    } else {
                        Log.e("ImgurUpload", "Lỗi tải ảnh lên Imgur: " + response.message());
                        runOnUiThread(() -> failureListener.onFailure("Tải lên Imgur thất bại."));
                    }
                } catch (Exception e) {
                    Log.e("ImgurUpload", "Lỗi khi tải ảnh lên Imgur: " + e.getMessage());
                    runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
                }
            }).start();
        } catch (Exception e) {
            Log.e("ImgurUpload", "Lỗi đọc file ảnh: " + e.getMessage());
            failureListener.onFailure(e.getMessage());
        }
    }
    // Xử lý đăng bài
    private void uploadPost() {
        String title = edtTitle.getText().toString().trim();
        String serviceInfo = edtServiceInfo.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String rentalTime = edtRentalTime.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();
        if (title.isEmpty() || serviceInfo.isEmpty() || price.isEmpty() || rentalTime.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
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
                        txtImageUrl.setText(imageUrl); // Cập nhật URL ảnh vào TextView
                        savePostToDatabase(title, serviceInfo, price, rentalTime, address, contact, imageUrl, uid, userEmail);
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }, (errorMessage) -> {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Lỗi khi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    savePostToDatabase(title, serviceInfo, price, rentalTime, address, contact, "", uid, userEmail);
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "Lỗi khi lấy thông tin người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lưu thông tin bài đăng vào Firebase
    private void savePostToDatabase(String title, String serviceInfo, String price, String rentalTime, String address, String contact, String imageUrl, String uid, String userEmail) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        String postId = databaseReference.push().getKey(); // ✅ Tạo ID duy nhất cho bài đăng
        long timestamp = System.currentTimeMillis(); // ✅ Lấy timestamp hiện tại

        if (postId != null) {
            HashMap<String, Object> postMap = new HashMap<>();
            postMap.put("title", title);
            postMap.put("postId", postId);
            postMap.put("serviceInfo", serviceInfo);
            postMap.put("price", price);
            postMap.put("rentalTime", rentalTime);
            postMap.put("address", address);
            postMap.put("contact", contact);
            postMap.put("imageUrl", imageUrl);
            postMap.put("timestamp", timestamp); // ✅ Thêm timestamp
            postMap.put("uid", uid); // ✅ Thêm uid
            postMap.put("userEmail", userEmail); // ✅ Thêm userEmail

            Log.d("FirebaseSave", "Dữ liệu đang lưu: " + postMap.toString()); // Log dữ liệu trước khi lưu

            databaseReference.child(postId).setValue(postMap)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseSave", "Đăng bài thành công!");
                        Toast.makeText(PostActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();

                        // ✅ Quay lại danh sách bài đăng & làm mới GridView
                        Intent intent = new Intent(PostActivity.this, HomeFragment.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Log.e("FirebaseSave", "Lỗi khi đăng bài: " + e.getMessage()));
        }
    }


    // Interfaces để xử lý callback khi upload ảnh
    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }
    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }

//    private void processZaloPayPayment() {
//        Toast.makeText(this, "Đang thực hiện thanh toán qua ZaloPay...", Toast.LENGTH_SHORT).show();
//        String totalString = "100000";
//        try {
//            CreateOrder orderApi = new CreateOrder();
//            JSONObject data = orderApi.createOrder(totalString);
//            String code = data.getString("return_code");
//            Log.d("ZaloPay", "Phản hồi từ API: " + data.toString());
//            if (code.equals("1")) {
//                String token = data.getString("zp_trans_token");
//                Log.d("ZaloPay", "Mã giao dịch: " + token);
//                int i;
//                ZaloPaySDK.getInstance().payOrder(PostActivity.this, token, "demozpdk://app", new PayOrderListener() {
//                    @Override
//                    public void onPaymentSucceeded(String s, String s1, String s2) {
//                        Log.d("ZaloPay", "Thanh toán thành công: " + s);
//                        Toast.makeText(PostActivity.this, "Thanh toán thành công! Đang đăng bài...", Toast.LENGTH_SHORT).show();
//                        Log.d("PostActivity", "uploadPost() được gọi sau khi thanh toán thành công.");
//                        int i=1;
//                    }
//
//                    @Override
//                    public void onPaymentCanceled(String s, String s1) {
//                        Intent intent1 = new Intent(PostActivity.this, HomeMekong.class);
//                        intent1.putExtra("result", "Hủy thanh toán");
//                        startActivity(intent1);
//                        int i=2;
//                    }
//                    @Override
//                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
//                        Intent intent1 = new Intent(PostActivity.this, HomeMekong.class);
//                        intent1.putExtra("result", "Lỗi thanh toán");
//                        startActivity(intent1);
//                    }
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Lỗi khi tạo đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        ZaloPaySDK.getInstance().onResult(intent);
//    }
}