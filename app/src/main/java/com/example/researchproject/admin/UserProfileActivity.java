package com.example.researchproject.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvProfile, txtPhone, txtEmail, txtDob, txtGender;
    private ImageView imgProfile;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        tvProfile = findViewById(R.id.tv_profile);
        txtPhone = findViewById(R.id.txtPhone);
        txtEmail = findViewById(R.id.txtEmail);
        txtDob = findViewById(R.id.txtDob);
        txtGender = findViewById(R.id.txtGender);
        imgProfile = findViewById(R.id.img_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();

        uid = getIntent().getStringExtra("uid"); // Lấy UID của người dùng hiện tại
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Gọi hàm lấy dữ liệu từ Firebase
        loadUserData();
    }
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu từ Firebase
                    String name = snapshot.child("nickname").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    // Cập nhật giao diện
                    tvProfile.setText(name != null ? name : "Chưa có tên");
                    txtPhone.setText(phone != null ? phone : "Chưa xác thực");
                    txtEmail.setText(email != null ? email : "Chưa xác thực");
                    txtDob.setText(dob != null ? dob : "Chưa xác thực");
                    txtGender.setText(gender != null ? gender : "Chưa xác thực");

                    // Load ảnh đại diện nếu có
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(UserProfileActivity.this)
                                .load(profileImage)
                                .into(imgProfile);
                    } else {
                        imgProfile.setImageResource(R.drawable.profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }


}