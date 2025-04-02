package com.example.researchproject.fragment;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.researchproject.History.OrderHistoryActivity;
import com.example.researchproject.Notification.NotificationActivity;
import com.example.researchproject.R;
import com.example.researchproject.Support.SupportActivity;
import com.example.researchproject.guide.UserGuideActivity;
import com.example.researchproject.iam.ChangePasswordActivity;
import com.example.researchproject.iam.EditProfileActivity;
import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.iam.VerifyPhoneActivity;
import com.example.researchproject.mekoaipro.MekoAIPro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SettingFragment extends Fragment {
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private ImageView imgProfile, imgbtnAccountEdit, btnClose;
    private TextView tvProfile, tvPhone, tvEmail, tvGoogle, tv_birthdate, tv_gender;
    private Button btnLogout, btnHelp, btnHistoryOrder, btnMekoAIPro, btnNotification,btnChangePassword, btnProblem;
    private ActivityResultLauncher<Intent> launcher;

    public SettingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        btnMekoAIPro = view.findViewById(R.id.btnMekoAIPro);
        btnHistoryOrder = view.findViewById(R.id.btnHistoryOrder);
        imgProfile = view.findViewById(R.id.img_profile);
        tvProfile = view.findViewById(R.id.tv_profile);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvEmail = view.findViewById(R.id.tv_email);
        tv_birthdate = view.findViewById(R.id.tv_birthdate);
        tv_gender = view.findViewById(R.id.tv_gender);
        imgbtnAccountEdit = view.findViewById(R.id.imgbtn_account_edit);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnNotification = view.findViewById(R.id.btnNotification);
        btnProblem = view.findViewById(R.id.btnProblem);


        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getBooleanExtra("updated", false)) {
                loadUserData();
            }
        });

        imgbtnAccountEdit.setOnClickListener(v -> launcher.launch(new Intent(requireActivity(), EditProfileActivity.class)));
        btnHelp.setOnClickListener(v -> startActivity(new Intent(getActivity(), UserGuideActivity.class)));
        view.findViewById(R.id.btnHelp).setOnClickListener(v -> startActivity(new Intent(getActivity(), UserGuideActivity.class)));
        btnLogout.setOnClickListener(v -> logoutUser());
        tvPhone.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), VerifyPhoneActivity.class);
            intent.putExtra("phoneNumber", tvPhone.getText().toString());
            launcher.launch(intent);
        });

        btnMekoAIPro.setOnClickListener(v -> startActivity(new Intent(getActivity(), MekoAIPro.class)));
        btnHistoryOrder.setOnClickListener(v -> startActivity(new Intent(getActivity(), OrderHistoryActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(getActivity(), NotificationActivity.class)));
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));
        btnProblem.setOnClickListener(v -> startActivity(new Intent(getActivity(), SupportActivity.class)));

        loadUserData();

        return view;


    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvProfile.setText(snapshot.child("nickname").getValue(String.class));
                    tv_birthdate.setText(snapshot.child("birthdate").getValue(String.class));
                    tv_gender.setText(snapshot.child("gender").getValue(String.class));
                    tvPhone.setText(snapshot.hasChild("phoneNumber") ? snapshot.child("phoneNumber").getValue(String.class) : "Chưa xác thực");
                    tvEmail.setText(snapshot.hasChild("email") ? snapshot.child("email").getValue(String.class) : "Chưa xác thực");

                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireActivity()).load(profileImageUrl).into(imgProfile);
                    } else {
                        imgProfile.setImageResource(R.drawable.profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        requireActivity().finish();
    }
}
