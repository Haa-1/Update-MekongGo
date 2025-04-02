package com.example.researchproject.guide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;
import com.example.researchproject.fragment.SettingFragment;

public class UserGuideActivity extends AppCompatActivity {

    private TextView btn_operating, btn_book, btn_all, btn_pay;
    private ImageView btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        // Ánh xạ các Button
        btn_operating = findViewById(R.id.btn_operating);
        btn_pay = findViewById(R.id.btn_pay);
        btn_book = findViewById(R.id.btn_book);
        btn_all = findViewById(R.id.btn_all);
        btn_back = findViewById(R.id.btn_back);

        // Xử lý sự kiện khi nhấn vào từng Button
        btn_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UserGuideActivity.this, GeneralInstructionsActivity.class);
                startActivity(intent);
            }
        });

        btn_operating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UserGuideActivity.this, OperatingRegulationsActivity.class);
                startActivity(intent);
            }
        });

        btn_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UserGuideActivity.this, CarBookingInstructionsActivity.class);
                startActivity(intent);
            }
        });


        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UserGuideActivity.this, PaymentInstructionsActivity.class);
                startActivity(intent);
            }
        });


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UserGuideActivity.this, SettingFragment.class);
                startActivity(intent);
            }
        });

    }


}
