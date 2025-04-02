package com.example.researchproject.iam;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;
import com.example.researchproject.fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EmailRegisterActivity extends AppCompatActivity {

    private static final String TAG = "EmailRegisterActivity";

    private EditText et_email, et_register_password, et_dob, et_nickname;
    private Button btn_continue;
    private ImageButton imgbtn_close;
    private TextView tv_login2;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Ánh xạ các View
        et_email = findViewById(R.id.et_email);
        et_register_password = findViewById(R.id.et_register_password);
        et_dob = findViewById(R.id.et_dob);
        et_nickname = findViewById(R.id.et_nickname);
        btn_continue = findViewById(R.id.btn_continue);
        imgbtn_close = findViewById(R.id.imgbtn_close);
        tv_login2 = findViewById(R.id.tv_login2);

        // Xử lý sự kiện khi nhấn nút "Đăng Ký"
        btn_continue.setOnClickListener(view -> registerEmail());

        imgbtn_close.setOnClickListener(view -> {
            Intent intent = new Intent(EmailRegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện khi nhấn vào Date of Birth
        et_dob.setOnClickListener(view -> showDatePicker());

        // Chuyển sang màn hình đăng nhập
        tv_login2.setOnClickListener(view -> {
            Intent intent = new Intent(EmailRegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePicker() {
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Hiển thị DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    et_dob.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void registerEmail() {
        String email = et_email.getText().toString().trim();
        String password = et_register_password.getText().toString().trim();
        String dob = et_dob.getText().toString().trim();
        String nickname = et_nickname.getText().toString().trim();

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Character.isUpperCase(password.charAt(0))) {
            Toast.makeText(this, "Password must start with an uppercase letter!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dob.isEmpty()) {
            Toast.makeText(this, "Date of Birth cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nickname.isEmpty()) {
            Toast.makeText(this, "Nickname cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid(); // Lấy UID từ Authentication
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");

                            // Tạo thông tin user
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("dob", dob);
                            userData.put("nickname", nickname);
                            userData.put("role", "user");  // 👤 Mặc định role = "user"
                            userData.put("status", "active");

                            // Lưu vào Firebase Database
                            database.child(uid).setValue(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User created successfully in database");
                                        Toast.makeText(EmailRegisterActivity.this, "Register successful!", Toast.LENGTH_SHORT).show();

                                        // Chuyển đến HomeMekong sau khi đăng ký thành công
                                        Intent intent = new Intent(EmailRegisterActivity.this, HomeFragment.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to save user data", e);
                                        Toast.makeText(EmailRegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(EmailRegisterActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}