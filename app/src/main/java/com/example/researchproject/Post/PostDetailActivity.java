package com.example.researchproject.Post;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.researchproject.Payment.Order.OrderInformationActivity;
import com.example.researchproject.R;
import com.example.researchproject.Review.Review;
import com.example.researchproject.Review.ReviewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class PostDetailActivity extends AppCompatActivity {
    private TextView txtTitle, txtServiceInfo, txtPrice, txtRentalTime, txtAddress, txtContact;
    private ImageView imgService;
    private ImageButton btnAddToCart, btnPay, btnSubmitReview;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private DatabaseReference  cartRef;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        // 🎯 Initialize Views
        txtTitle = findViewById(R.id.txtTitle);
        txtServiceInfo = findViewById(R.id.txtServiceInfo);
        txtPrice = findViewById(R.id.txtPrice);
        txtRentalTime = findViewById(R.id.txtRentalTime);
        txtAddress = findViewById(R.id.txtAddress);
        txtContact = findViewById(R.id.txtContact);
        imgService = findViewById(R.id.imgService);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnPay = findViewById(R.id.btnPay);

        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        // 🌟 Setup RecyclerView for Reviews
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);
        // ✅ Get postId from Intent
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài đăng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        cartRef = FirebaseDatabase.getInstance().getReference("Cart");
        // 📌 Get Data from Intent
        String title = getIntent().getStringExtra("title");
        String serviceInfo = getIntent().getStringExtra("serviceInfo");
        String price = getIntent().getStringExtra("price");
        String rentalTime = getIntent().getStringExtra("rentalTime");
        String address = getIntent().getStringExtra("address");
        String contact = getIntent().getStringExtra("contact");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        // ✅ Display Data
        txtTitle.setText(title);
        txtServiceInfo.setText(serviceInfo);
        txtPrice.setText("Giá: " + price + " VND");
        txtRentalTime.setText("Thời gian thuê: " + rentalTime);
        txtAddress.setText("Địa chỉ: " + address);
        txtContact.setText("Liên hệ: " + contact);
        Glide.with(this).load(imageUrl).into(imgService);
        // ✅ Add to Cart
        btnAddToCart.setOnClickListener(v -> {
            // Giả sử uid đã có sẵn từ quá trình đăng nhập
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userEmail = snapshot.child("email").getValue(String.class); // ✅ Lấy userEmail
                    String cartItemId = cartRef.push().getKey();
                    if (cartItemId != null) {
                        HashMap<String, Object> cartItem = new HashMap<>();
                        cartItem.put("postId", postId);
                        cartItem.put("title", title);
                        cartItem.put("price", price);
                        cartItem.put("imageUrl", imageUrl);
                        cartItem.put("uid", uid); // ✅ Thêm uid vào giỏ hàng
                        cartItem.put("userEmail", userEmail); // ✅ Thêm userEmail vào giỏ hàng

                        cartRef.child(cartItemId).setValue(cartItem)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(PostDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(PostDetailActivity.this, "Lỗi khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                                );
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PostDetailActivity.this, "Lỗi khi lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        // ✅ Payment Button
        btnPay.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, OrderInformationActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("title", title);
            intent.putExtra("price", price);
//            intent.putExtra("rentalTime", rentalTime);
            startActivity(intent);
        });
    }

}