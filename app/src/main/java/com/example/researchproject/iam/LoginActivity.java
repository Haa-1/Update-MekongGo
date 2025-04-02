package com.example.researchproject.iam;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.MainActivity;
import com.example.researchproject.R;
import com.example.researchproject.admin.AdminDashboardActivity;
import com.example.researchproject.fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private Button btn_login_submit;
    private EditText et_login_user, et_password;
    private TextView tv_re_password, tv_signup;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo UI
        btn_login_submit = findViewById(R.id.btn_login_submit);
        et_login_user = findViewById(R.id.et_login_user);
        et_password = findViewById(R.id.et_password);
        tv_re_password = findViewById(R.id.tv_re_password);
        tv_signup = findViewById(R.id.tv_signup);
        mAuth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        if (intent!=null) {
            Bundle ex = intent.getExtras();
            if (ex!=null){
                et_login_user.setText(ex.getString("email"));
                et_password.setText(ex.getString("password"));
            }}


        btn_login_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et_login_user.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Do not leave blank!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(LoginActivity.this, "Invalid email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("LoginActivity", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        String uid = user.getUid();
                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

                                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    String role = snapshot.child("role").getValue(String.class);

                                                    if ("admin".equals(role)) {
                                                        // ✅ Chuyển đến trang quản trị nếu là admin
                                                        Toast.makeText(LoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                                        startActivity(intent);
                                                    } else if ("user".equals(role)) {
                                                        // ✅ Chuyển đến Home nếu là user
                                                        Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "Access Denied!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    finish(); // Đóng LoginActivity
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Wrong Account Or Password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LoginActivity", "Sign Up button clicked");
                Intent intent = new Intent(LoginActivity.this, SocialRegisterActivity.class);
                startActivity(intent);
            }
        });
        tv_re_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivity(in);
            }
        });
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
