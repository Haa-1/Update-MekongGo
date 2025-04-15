package com.example.researchproject.Payment.Order;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.researchproject.MainActivity;
import com.example.researchproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderSuccessfulActivity extends AppCompatActivity {

    private TextView txtCustomerName, txtCustomerAddress, txtPhone, txtQuantity, txtTotalPrice, txtProductName, txtPrice, txtAdress, txtCustomerPhone, txtRentalPeriod;
    private ImageView imgProduct, img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_successful);

        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtTotalPrice = findViewById(R.id.txtTotal);
        txtProductName = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        imgProduct = findViewById(R.id.imageView);
        img_back = findViewById(R.id.img_back);
        txtAdress = findViewById(R.id.txtAddress);
        txtPhone = findViewById(R.id.txtPhone);
        txtRentalPeriod = findViewById(R.id.txtRentalPeriod);

        img_back.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessfulActivity.this, MainActivity.class);
            startActivity(intent);
        });
        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String customerName = intent.getStringExtra("customerName");
        String customerAddress = intent.getStringExtra("customerAddress");
        String customerPhone = intent.getStringExtra("customerPhone");
        String quantity = intent.getStringExtra("quantity");
        String totalPrice = intent.getStringExtra("totalPrice");
        String postId = intent.getStringExtra("postId");
        String rentalPeriod = intent.getStringExtra("rentalPeriod");

        // Hiển thị thông tin nhận hàng
        txtCustomerName.setText("Người nhận: " + customerName);
        txtCustomerAddress.setText("Địa chỉ: " + customerAddress);
        txtCustomerPhone.setText("SĐT: " + customerPhone);
        txtQuantity.setText(quantity);
        txtTotalPrice.setText(totalPrice);
        txtRentalPeriod.setText(rentalPeriod);
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String productName = snapshot.child("title").getValue(String.class);
                    String productPrice = snapshot.child("price").getValue(String.class);
                    String productImage = snapshot.child("imageUrl").getValue(String.class);
                    String productAddress = snapshot.child("address").getValue(String.class);
                    String productPhone = snapshot.child("phone").getValue(String.class);

                    txtProductName.setText(productName);
                    txtPrice.setText("Giá: " + productPrice + " VNĐ");
                    txtAdress.setText("Địa chỉ: " + productAddress);
                    txtPhone.setText("SĐT: " + productPhone);


                    Glide.with(OrderSuccessfulActivity.this)
                            .load(productImage)
                            .error(R.drawable.search_icon)
                            .override(500, 500)
                            .into(imgProduct);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }
}