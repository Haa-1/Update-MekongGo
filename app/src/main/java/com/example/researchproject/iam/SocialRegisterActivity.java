package com.example.researchproject.iam;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import com.example.researchproject.MainActivity;
import com.example.researchproject.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class SocialRegisterActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    private static final String TAG_GOOGLE = "GoogleActivity";
   // private static final String TAG_FACEBOOK = "FacebookLogin";

    TextView tv_login;
    EditText editTextMobile;
    Button btnUser, btnFacebook, btnGoogle, btnApple, btn_continue_phone;
    ImageButton imgbtn_close;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    // private GoogleSignInClient googleSignInClient;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_register);
        Log.d("SocialRegisterActivity", "SocialRegisterActivity started!");

        tv_login = findViewById(R.id.tv_login);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnUser = findViewById(R.id.btnUser);
        btn_continue_phone = findViewById(R.id.btn_continue_phone);
        imgbtn_close = findViewById(R.id.imgbtn_close);
        editTextMobile = findViewById(R.id.editTextMobile);
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(getBaseContext());

        // Khởi tạo Facebook Login
        mCallbackManager = CallbackManager.Factory.create();

        // Xử lý sự kiện đăng nhập
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
       // btnFacebook.setOnClickListener(v -> signInWithFacebook());

        tv_login.setOnClickListener(view -> {
            Intent intent = new Intent(SocialRegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        imgbtn_close.setOnClickListener(view -> {
            Intent intent = new Intent(SocialRegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnUser.setOnClickListener(view -> {
            Intent intent = new Intent(SocialRegisterActivity.this, EmailRegisterActivity.class);
            startActivity(intent);
        });

        btn_continue_phone.setOnClickListener(view -> {
            String mobile = editTextMobile.getText().toString().trim();
            if (mobile.isEmpty() || mobile.length() < 10) {
                editTextMobile.setError("Enter a valid mobile");
                editTextMobile.requestFocus();
                return;
            }
            Intent intent = new Intent(SocialRegisterActivity.this, VerifyPhoneActivity.class);
            intent.putExtra("mobile", mobile);
            startActivity(intent);
        });

//        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG_FACEBOOK, "facebook:onSuccess:" + loginResult);
//                handleFacebookAccessToken(loginResult.getAccessToken());
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG_FACEBOOK, "facebook:onCancel");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.d(TAG_FACEBOOK, "facebook:onError", error);
//            }
//        });
   }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            // Chuyển qua HomeMekong.java khi đăng nhập thành công
            Intent intent = new Intent(SocialRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }


    private void launchCredentialManager() {
        // [START create_credential_manager_request]
        // Instantiate a Google sign-in request
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(true).setServerClientId(getString(R.string.default_web_client_id)).build();

        // Create the Credential Manager request
        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();
        // [END create_credential_manager_request]

        // Launch Credential Manager UI
        credentialManager.getCredentialAsync(getBaseContext(), request, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {
            @Override
            public void onError(@NonNull androidx.credentials.exceptions.GetCredentialException e) {
                Log.e(TAG_GOOGLE, "Couldn't retrieve user's credentials: " + e.getLocalizedMessage());
            }

            @Override
            public void onResult(GetCredentialResponse result) {
                // Extract credential from the result returned by Credential Manager
                handleSignIn(result.getCredential());
            }
        });
    }

    // [START handle_sign_in]
    private void handleSignIn(Credential credential) {
        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential; // Ép kiểu thủ công
            if (customCredential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                // Xử lý tiếp
                Bundle credentialData = customCredential.getData();
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
                String idToken = googleIdTokenCredential.getIdToken();

                // Xác thực với Firebase
                AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
            }
        } else {
            Log.w(TAG_GOOGLE, "Credential is not of type Google ID!");
        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG_GOOGLE, "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                // If sign in fails, display a message to the user
                Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                updateUI(null);
            }
        });
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
    }


    // Đăng nhập bằng Google
    private void signInWithGoogle() {
        launchCredentialManager();
    }


    // Đăng nhập bằng Facebook
    /*private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_FACEBOOK, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG_FACEBOOK, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG_FACEBOOK, "facebook:onError", error);
            }
        });
    }*/


    // Xử lý kết quả đăng nhập
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // [START auth_with_facebook]
   /* private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG_FACEBOOK, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_FACEBOOK, "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_FACEBOOK, "signInWithCredential:failure", task.getException());
                    Toast.makeText(SocialRegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }*/
    // [END auth_with_facebook]
}