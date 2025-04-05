package com.example.researchproject.History;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderHistoryDetailActivity extends AppCompatActivity {
    private TextView txtCustomerName, txtCustomerAddress, txtCustomerPhone, txtQuantity, txtTotalPrice,txtRentalPeriod, txtProductName, txtPrice,txtProductAddress, txtProductPhone;
    private ImageView imgProduct, img_review, btnUpload; ;
    private Uri imageUri;
    private DatabaseReference reviewsRef;
    private EditText edtReview;
    private ImageButton btnSubmitReview;
    private RatingBar ratingBar;
    private String globalPostId; // 🔥 Lưu postId vào biến toàn cục
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history_detail);

        txtCustomerName = findViewById(R.id.txtCustomerName_detail);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress_detail);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone_detail);
        txtQuantity = findViewById(R.id.txtQuantity_detail);
        txtTotalPrice = findViewById(R.id.txtTotal_detail);
        txtProductName = findViewById(R.id.txtTitle_detail);
        txtPrice = findViewById(R.id.txtPrice_detail);
        imgProduct = findViewById(R.id.imageView_detail);
        txtRentalPeriod = findViewById(R.id.txtRentalPeriod_detail);
        txtProductAddress = findViewById(R.id.txtAddress_detail);
        txtProductPhone = findViewById(R.id.txtPhone_detail);
        edtReview = findViewById(R.id.edtReview);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        ratingBar = findViewById(R.id.ratingBar);
        img_review = findViewById(R.id.img_send_review);
        btnUpload = findViewById(R.id.btnUploadImageReview);


        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        String postId =intent.getStringExtra("postId");
        Log.d("DEBUG", "orderId: " + orderId);
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadOrderDetails(orderId);



        // Xử lý sự kiện gửi review
        btnSubmitReview.setOnClickListener(v -> submitReview());
        btnUpload.setOnClickListener(v ->
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                openFileChooser();
            }
        });

    }
    private void loadOrderDetails(String orderId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order_History").child(userId).child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(OrderHistoryDetailActivity.this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Log.d("DEBUG", "Snapshot: " + snapshot.getValue());

                String customerName = snapshot.child("customerName").getValue(String.class);
                String customerPhone = snapshot.child("customerPhone").getValue(String.class);
                String customerAddress = snapshot.child("customerAddress").getValue(String.class);
                String rentalPeriod = snapshot.child("rentalPeriod").getValue(String.class);
                String quantity = snapshot.child("quantity").getValue(String.class);
                String totalPrice = snapshot.child("totalPrice").getValue(String.class);
                globalPostId = snapshot.child("postId").getValue(String.class); // 🔥 Lưu vào biến toàn cục


                Log.d("DEBUG", "quantity: " + quantity);
                Log.d("DEBUG", "totalPrice: " + totalPrice);
                Log.d("DEBUG", "rentalPeriod: " + rentalPeriod);
                Log.d("DEBUG", "customerName: " + customerName);
                Log.d("DEBUG", "customerPhone: " + customerPhone);
                Log.d("DEBUG", "customerAddress: " + customerAddress);

                txtCustomerName.setText(customerName);
                txtCustomerPhone.setText(customerPhone);
                txtCustomerAddress.setText(customerAddress);
                txtRentalPeriod.setText(rentalPeriod + " ngày");
                txtQuantity.setText(quantity);
                txtTotalPrice.setText(totalPrice + " VND");

                loadProductDetails(globalPostId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryDetailActivity.this, "Lỗi tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductDetails(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(OrderHistoryDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    return;
                }
                String title = snapshot.child("title").getValue(String.class);
                String price = snapshot.child("price").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String phone = snapshot.child("contact").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                Log.d("DEBUG", "imageUrl: " + imageUrl);

                txtProductName.setText(title);
                txtPrice.setText("Giá: " + price + " VND");
                txtProductAddress.setText("Địa chỉ: " + address);
                txtProductPhone.setText("SĐT liên hệ: " + phone);

                Glide.with(OrderHistoryDetailActivity.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.search_icon) // Một ảnh tạm lớn hơn
                        .error(R.drawable.search_icon)
                        .override(500, 500) // Tăng kích thước ảnh tải về
                        .into(imgProduct);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryDetailActivity.this, "Lỗi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveReviewToDatabase(String comment, float rating, String imageUrl) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(globalPostId);
        String reviewId = reviewsRef.push().getKey();

        HashMap<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("user", "Người dùng ẩn danh");
        reviewMap.put("rating", rating);
        reviewMap.put("comment", comment);
        reviewMap.put("imageUrl", imageUrl);

        reviewsRef.child(reviewId).setValue(reviewMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đánh giá đã gửi!", Toast.LENGTH_SHORT).show();
                    edtReview.setText("");
                    ratingBar.setRating(0);
                    img_review.setImageResource(R.drawable.search_icon);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi đánh giá!", Toast.LENGTH_SHORT).show());
    }
    private void submitReview() {
        if (globalPostId == null || globalPostId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm để đánh giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = edtReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (TextUtils.isEmpty(reviewText) || rating == 0) {
            Toast.makeText(this, "Vui lòng nhập đánh giá & chọn số sao!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageToImgur(imageUri, imageUrl -> saveReviewToDatabase(reviewText, rating, imageUrl),
                    errorMessage -> Toast.makeText(OrderHistoryDetailActivity.this, "Lỗi tải ảnh lên Imgur!", Toast.LENGTH_SHORT).show());
        } else {
            saveReviewToDatabase(reviewText, rating, null);
        }
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
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(img_review); // Hiển thị ảnh đã chọn
        }
    }
    // Xử lý khi người dùng cấp quyền truy cập bộ nhớ
    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
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
    private void uploadImageToImgur(Uri imageUri, OrderHistoryDetailActivity.OnUploadSuccessListener successListener, OrderHistoryDetailActivity.OnUploadFailureListener failureListener) {
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
    // Interfaces để xử lý callback khi upload ảnh
    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }
    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }

}