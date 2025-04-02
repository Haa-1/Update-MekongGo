package com.example.researchproject.admin;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class AdminDashboardActivity extends AppCompatActivity {
    private Button btnManageUsers, btnManagePosts, btnManageAds;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManagePosts = findViewById(R.id.btnManagePosts);
        btnManageAds = findViewById(R.id.btnManageAds);
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, ManageUsersActivity.class)));
        btnManagePosts.setOnClickListener(v -> startActivity(new Intent(this, ManagePostsActivity.class)));
        btnManageAds.setOnClickListener(v -> startActivity(new Intent(this, ManageAdsActivity.class)));
        checkAdminAccess();
    }
    private void checkAdminAccess() {
        String userId = mAuth.getCurrentUser().getUid();
        userRef.child(userId).child("role").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().getValue(String.class);
                if (!"admin".equals(role)) {
                    Toast.makeText(this, "Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Lỗi xác thực!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}