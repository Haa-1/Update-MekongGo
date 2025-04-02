package com.example.researchproject.Support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;

public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Button callButton = findViewById(R.id.call_button);
        Button sendMessageButton = findViewById(R.id.send_message_button);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện khi nút Call được nhấn
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:00000000")); // Thay 00000000 bằng số điện thoại thực tế
                startActivity(intent);
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện khi nút Send message được nhấn
                String fanpageUrl = "https://www.facebook.com/profile.php?id=61575013943017"; // Thay bằng URL fanpage thực tế
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fanpageUrl));
                startActivity(intent);
            }
        });
    }
}