package com.example.researchproject.iam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;
import com.example.researchproject.fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {
    private EditText editTextCode;
    Button buttonLognIn, btnResendOTP;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    ImageButton imgbtn_close;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        editTextCode = findViewById(R.id.editTextCode); // Liên kết với EditText nhập OTP
        buttonLognIn = findViewById(R.id.buttonLognIn);
        imgbtn_close = findViewById(R.id.imgbtn_close);
        btnResendOTP = findViewById(R.id.btnResendOTP);

        mAuth = FirebaseAuth.getInstance();
        String mobile = getIntent().getStringExtra("mobile"); // Nhận số điện thoại từ Intent
        sendVerificationCode(mobile);


        imgbtn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyPhoneActivity.this, SocialRegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonLognIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    editTextCode.setError("Enter valid OTP code");
                    editTextCode.requestFocus();
                    return;
                }
                verifyVerificationCode(code); // Gọi hàm xác minh OTP
            }
        });

        btnResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = getIntent().getStringExtra("mobile");
                resendVerificationCode(mobile, mResendToken);
            }
        });

    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + mobile)      // Số điện thoại
                        .setTimeout(60L, TimeUnit.SECONDS)  // Thời gian timeout
                        .setActivity(this)                 // Sử dụng Activity hiện tại
                        .setCallbacks(mCallbacks)          // Callback xử lý kết quả
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        editTextCode.setText(code); // Tự động điền OTP
                        verifyVerificationCode(code);
                    }
                }
                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.e("FirebaseAuth", "Verification failed", e);
                    Toast.makeText(VerifyPhoneActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }


                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    mVerificationId = s;  // Lưu verificationId vào biến toàn cục
                    mResendToken = token;  // Lưu token để resend nếu cần

                    Toast.makeText(VerifyPhoneActivity.this, "OTP code sent!", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyVerificationCode(String code) {
        if (mVerificationId == null) {
            Toast.makeText(this, "Error: Verification code not found. Please resend OTP!", Toast.LENGTH_LONG).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifyPhoneActivity.this, HomeFragment.class));
                        finish();
                    } else {
                        Toast.makeText(this, "OTP code is incorrect, try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            Intent intent = new Intent(VerifyPhoneActivity.this, HomeFragment.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            //verification unsuccessful.. display an error message
                            String message = "Somthing is wrong, we will fix it soon...";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }
    // Hàm gửi lại mã OTP
    private void resendVerificationCode(String mobile, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + mobile)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


}
