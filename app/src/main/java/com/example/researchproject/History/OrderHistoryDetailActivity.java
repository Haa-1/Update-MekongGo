package com.example.researchproject.History;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class OrderHistoryDetailActivity extends AppCompatActivity {


    private TextView txtCustomerName, txtCustomerAddress, txtCustomerPhone, txtQuantity, txtTotalPrice,txtRentalPeriod, txtProductName, txtPrice,txtProductAddress, txtProductPhone;
    private ImageView imgProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history_detail);

        txtCustomerName = findViewById(R.id.txtCustomerName_detail);
        txtCustomerAddress = findViewById(R.id.txtCustomerAddress_detail);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone_detail);
        txtQuantity = findViewById(R.id.txtQuantity_detail);
        txtTotalPrice = findViewById(R.id.txtTotal_detail);
        txtProductName = findViewById(R.id.txtTitle_detail);
        txtPrice = findViewById(R.id.txtPrice_detail);
        imgProduct = findViewById(R.id.imageView_detail);
        txtRentalPeriod = findViewById(R.id.txtRentalPeriod_detail);
        txtProductAddress = findViewById(R.id.txtAddress_detail);
        txtProductPhone = findViewById(R.id.txtPhone_detail);

        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        Log.d("DEBUG", "orderId: " + orderId);
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadOrderDetails(orderId);
    }
    private void loadOrderDetails(String orderId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order_History").child(userId).child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(OrderHistoryDetailActivity.this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Log.d("DEBUG", "Snapshot: " + snapshot.getValue());

                String customerName = snapshot.child("customerName").getValue(String.class);
                String customerPhone = snapshot.child("customerPhone").getValue(String.class);
                String customerAddress = snapshot.child("customerAddress").getValue(String.class);
                String rentalPeriod = snapshot.child("rentalPeriod").getValue(String.class);
                String quantity = snapshot.child("quantity").getValue(String.class);
                String totalPrice = snapshot.child("totalPrice").getValue(String.class);
                String postId = snapshot.child("postId").getValue(String.class);

                Log.d("DEBUG", "postId: " + postId);
                Log.d("DEBUG", "quantity: " + quantity);
                Log.d("DEBUG", "totalPrice: " + totalPrice);
                Log.d("DEBUG", "rentalPeriod: " + rentalPeriod);
                Log.d("DEBUG", "customerName: " + customerName);
                Log.d("DEBUG", "customerPhone: " + customerPhone);
                Log.d("DEBUG", "customerAddress: " + customerAddress);

                txtCustomerName.setText(customerName);
                txtCustomerPhone.setText(customerPhone);
                txtCustomerAddress.setText(customerAddress);
                txtRentalPeriod.setText(rentalPeriod + " ngày");
                txtQuantity.setText(quantity);
                txtTotalPrice.setText(totalPrice + " VND");

                loadProductDetails(postId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryDetailActivity.this, "Lỗi tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductDetails(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(OrderHistoryDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    return;
                }

                String title = snapshot.child("title").getValue(String.class);
                String price = snapshot.child("price").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String phone = snapshot.child("contact").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                txtProductName.setText(title);
                txtPrice.setText("Giá: " + price + " VND");
                txtProductAddress.setText("Địa chỉ: " + address);
                txtProductPhone.setText("SĐT liên hệ: " + phone);

                Glide.with(OrderHistoryDetailActivity.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.search_icon)
                        .error(R.drawable.search_icon)
                        .into(imgProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryDetailActivity.this, "Lỗi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

}