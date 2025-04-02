package com.example.researchproject.admin;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.example.researchproject.Post.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
public class EditPost extends AppCompatActivity {
    private EditText edtTitle, edtServiceInfo, edtPrice, edtRentalTime, edtAddress, edtContact;
    private ImageView imgService;
    private Button btnSave;
    private DatabaseReference databaseReference;
    private String postId;
    private long timestamp;
    private TextView tvDate;
    private TextView tvUserEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        tvDate = findViewById(R.id.tvDate);
        tvUserEmail = findViewById(R.id.tvUserEmail); // Initialize the TextView
        edtTitle = findViewById(R.id.edtTitle);
        edtServiceInfo = findViewById(R.id.edtServiceInfo);
        edtPrice = findViewById(R.id.edtPrice);
        edtRentalTime = findViewById(R.id.edtRentalTime);
        edtAddress = findViewById(R.id.edtAddress);
        edtContact = findViewById(R.id.edtContact);
        imgService = findViewById(R.id.imgService);
        btnSave = findViewById(R.id.btnSave);
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        // Get data passed from PostsAdapter
        postId = getIntent().getStringExtra("postId");
        timestamp = getIntent().getLongExtra("timestamp", 0);
        // Load post data
        loadPostData();
        btnSave.setOnClickListener(v -> updatePost());
    }
    private void loadPostData() {
        databaseReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    edtTitle.setText(post.getTitle());
                    edtServiceInfo.setText(post.getServiceInfo());
                    edtPrice.setText(post.getPrice());
                    edtRentalTime.setText(post.getRentalTime());
                    edtAddress.setText(post.getAddress());
                    edtContact.setText(post.getContact());
                    Glide.with(EditPost.this)
                            .load(post.getImageUrl())
                            .placeholder(R.drawable.search_icon)
                            .into(imgService);
                    // Format timestamp as date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    String date = sdf.format(new Date(post.getTimestamp()));
                    tvDate.setText("Ngày đăng: " + date);
                    // Display user email
                    tvUserEmail.setText("Email: " + post.getUserEmail());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPost.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updatePost() {
        String title = edtTitle.getText().toString();
        String serviceInfo = edtServiceInfo.getText().toString();
        String price = edtPrice.getText().toString();
        String rentalTime = edtRentalTime.getText().toString();
        String address = edtAddress.getText().toString();
        String contact = edtContact.getText().toString();
        // Update post in Firebase
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("serviceInfo", serviceInfo);
        postMap.put("price", price);
        postMap.put("rentalTime", rentalTime);
        postMap.put("address", address);
        postMap.put("contact", contact);
        postMap.put("timestamp", timestamp);
        databaseReference.child(postId).updateChildren(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPost.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditPost.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}