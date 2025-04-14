package com.example.researchproject.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.researchproject.Payment.Api.CreateOrder;
import com.example.researchproject.Post.PostAdActivity;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import okhttp3.*;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PostNewsFragment extends Fragment {

    private EditText edtTitle, edtServiceInfo, edtPrice, edtRentalTime, edtAddress, edtContact;
    private Button btnPost, btnUploadImage, btnPostAd;
    private ImageView imgService;
    private TextView txtImageUrl;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_news, container, false);

        edtTitle = view.findViewById(R.id.edtTitle);
        edtServiceInfo = view.findViewById(R.id.edtServiceInfo);
        edtPrice = view.findViewById(R.id.edtPrice);
        edtRentalTime = view.findViewById(R.id.edtRentalTime);
        edtAddress = view.findViewById(R.id.edtAddress);
        edtContact = view.findViewById(R.id.edtContact);
        btnPost = view.findViewById(R.id.btnPost);
        imgService = view.findViewById(R.id.imgService);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        txtImageUrl = view.findViewById(R.id.txtImageUrl);
        btnPostAd = view.findViewById(R.id.btnPostAd);

        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        btnPostAd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PostAdActivity.class);
            startActivity(intent);
        });

        btnUploadImage.setOnClickListener(v -> checkStoragePermission());

        ZaloPaySDK.init(2553, Environment.SANDBOX);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        btnPost.setOnClickListener(v -> uploadPost());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
        } else {
            openFileChooser();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgService);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(requireContext(), "Bạn cần cấp quyền truy cập bộ nhớ để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadPost() {
        String title = edtTitle.getText().toString().trim();
        String serviceInfo = edtServiceInfo.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String rentalTime = edtRentalTime.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();

        if (title.isEmpty() || serviceInfo.isEmpty() || price.isEmpty() || rentalTime.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Đang đăng tin...");
        progressDialog.show();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userEmail = snapshot.child("email").getValue(String.class);
                if (imageUri != null) {
                    uploadImageToImgur(imageUri, imageUrl -> {
                        txtImageUrl.setText(imageUrl);
                        savePostToDatabase(title, serviceInfo, price, rentalTime, address, contact, imageUrl, uid, userEmail);
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                    }, errorMessage -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Lỗi khi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    savePostToDatabase(title, serviceInfo, price, rentalTime, address, contact, "", uid, userEmail);
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                }
                processZaloPayPayment();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Lỗi khi lấy thông tin người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

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

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        String imageUrl = new JSONObject(responseData).getJSONObject("data").getString("link");
                        requireActivity().runOnUiThread(() -> successListener.onSuccess(imageUrl));
                    } else {
                        requireActivity().runOnUiThread(() -> failureListener.onFailure("Tải lên Imgur thất bại."));
                    }
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
                }
            }).start();
        } catch (Exception e) {
            failureListener.onFailure(e.getMessage());
        }
    }

    private void savePostToDatabase(String title, String serviceInfo, String price, String rentalTime, String address, String contact, String imageUrl, String uid, String userEmail) {
        String postId = databaseReference.push().getKey();
        long timestamp = System.currentTimeMillis();

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
            postMap.put("timestamp", timestamp);
            postMap.put("uid", uid);
            postMap.put("userEmail", userEmail);

            databaseReference.child(postId).setValue(postMap)
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseSave", "Đăng bài thành công!"))
                    .addOnFailureListener(e -> Log.e("FirebaseSave", "Lỗi khi đăng bài: " + e.getMessage()));
        }
    }

    private void processZaloPayPayment() {
        Toast.makeText(requireContext(), "Đang thực hiện thanh toán qua ZaloPay...", Toast.LENGTH_SHORT).show();
        try {
            JSONObject data = new CreateOrder().createOrder("100000");
            if (data.getString("return_code").equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(requireActivity(), token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Toast.makeText(requireContext(), "Hủy thanh toán", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Toast.makeText(requireContext(), "Lỗi thanh toán", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi khi tạo đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }
    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }
}
