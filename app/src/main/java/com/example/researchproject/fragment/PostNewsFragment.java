package com.example.researchproject.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.example.researchproject.Ad.Ad;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okhttp3.*;

public class PostNewsFragment extends Fragment {
    private EditText edtTitle;
    private ImageView imgService;
    private Button btnUploadImage, btnPost;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752";

    // Dùng ActivityResultLauncher thay cho startActivityForResult
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(imgService);
                }
            });

    public PostNewsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_news, container, false);

        // Ánh xạ View đúng ID
        edtTitle = view.findViewById(R.id.edtTitle);
        imgService = view.findViewById(R.id.imgService);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        btnPost = view.findViewById(R.id.btnPost);

        // Kết nối Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Advertisements");

        // Chọn hình ảnh
        btnUploadImage.setOnClickListener(v -> openFileChooser());

        // Đăng bài viết
        btnPost.setOnClickListener(v -> uploadAd());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadAd() {
        String adTitle = edtTitle.getText().toString().trim();

        if (adTitle.isEmpty() || imageUri == null) {
            Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề và chọn hình ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Đang tải hình ảnh lên Imgur...");
        progressDialog.show();

        uploadImageToImgur(imageUri, imageUrl -> {
            progressDialog.dismiss();
            saveAdToFirebase(adTitle, imageUrl);
        }, errorMessage -> {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Lỗi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
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

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            String imageUrl = jsonObject.getJSONObject("data").getString("link");

                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> successListener.onSuccess(imageUrl));
                            }
                        } catch (JSONException e) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> failureListener.onFailure("Lỗi xử lý dữ liệu từ Imgur"));
                            }
                        }
                    } else {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> failureListener.onFailure("Lỗi tải ảnh lên Imgur"));
                        }
                    }
                }
            });

        } catch (Exception e) {
            failureListener.onFailure(e.getMessage());
        }
    }

    private void saveAdToFirebase(String title, String imageUrl) {
        String adId = databaseReference.push().getKey();
        Ad ad = new Ad(adId, title, imageUrl);

        databaseReference.child(adId).setValue(ad)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Đăng quảng cáo thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi đăng quảng cáo!", Toast.LENGTH_SHORT).show());
    }

    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }

    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }
}
