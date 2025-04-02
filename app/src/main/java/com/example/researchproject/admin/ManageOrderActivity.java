package com.example.researchproject.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.History.OrderHistoryActivity;
import com.example.researchproject.History.OrderHistoryAdapter;
import com.example.researchproject.History.OrderHistoryDetailActivity;
import com.example.researchproject.History.OrderHistoryDisplay;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ManageOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderHistoryAdapter adapter;
    private List<OrderHistoryDisplay> orderList;
    private DatabaseReference orderRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        recyclerViewOrders = findViewById(R.id.recyclerViewOrder);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, orderList,this::onOrderClicked);
        recyclerViewOrders.setAdapter(adapter);

        // Nhận UID từ Intent
        uid = getIntent().getStringExtra("uid");
        Log.d("ManageOrderActivity", "UID: " + uid);
        if (uid != null) {
            orderRef = FirebaseDatabase.getInstance().getReference("Order_History").child(uid);
            loadOrders();
        } else {
            Toast.makeText(this, "Không tìm thấy UID!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onOrderClicked(String orderId) {
        Log.d("ManageOrderActivity", "Order ID clicked: " + orderId);
        Intent intent = new Intent(this, OrderDetailAdminActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("uid", uid);
        Log.d("ManageOrderActivity", "Order ID sent: " + orderId);
        startActivity(intent);
    }


    private void loadOrders() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order_History").child(uid);
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");

        if (adapter == null) {
            Log.e("ERROR", "orderHistoryAdapter is NULL before fetching data");
            Log.e("orderHistoryAdapter", adapter.toString());
            return;
        }

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                Log.d("DEBUG", "Snapshot: " + snapshot.getValue());

                if (!snapshot.exists()) {
                    Log.e("ERROR", "No order history found for user: " + uid);
                    return;
                }
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String postId = orderSnapshot.child("postId").getValue(String.class);

                    // Kiểm tra postId có hợp lệ không
                    if (postId == null || postId.isEmpty()) {
                        Log.e("ERROR", "postId is null or empty");
                        continue;
                    }
                    Log.d("DEBUG", "postId: " + postId);

                    // Lấy dữ liệu an toàn, tránh lỗi kiểu dữ liệu
                    String rentalPeriod = orderSnapshot.child("rentalPeriod").getValue(String.class);
                    String quantityStr = orderSnapshot.child("quantity").getValue(String.class);
                    String totalPriceStr = orderSnapshot.child("totalPrice").getValue(String.class);
                    String orderId = orderSnapshot.getKey();

                    // Ép kiểu từ String sang int

                    int quantity = parseSafeInt(quantityStr);
                    int totalPrice = parseSafeInt(totalPriceStr);

                    Log.d("DEBUG", "quantity: " + quantity);
                    Log.d("DEBUG", "totalPrice: " + totalPrice);
                    Log.d("DEBUG", "rentalPeriod: " + rentalPeriod);

                    // Lấy thông tin từ Post
                    postRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                            Log.d("DEBUG", "Checking Post for postId: " + postId);
                            Log.d("DEBUG", "Post Data: " + postSnapshot.getValue());
                            if (!postSnapshot.exists()) {
                                Log.e("ERROR", "Post not found for postId: " + postId);
                                return;
                            }

                            Log.d("DEBUG","Post Snapshot: " + postSnapshot.getValue());
                            String title = postSnapshot.child("title").getValue(String.class);
                            String imageUrl = postSnapshot.child("imageUrl").getValue(String.class);

                            Log.d("DEBUG", "Fetched title: " + title);
                            Log.d("DEBUG", "Fetched imageUrl: " + imageUrl);

                            // Tạo đối tượng hiển thị và cập nhật Adapter
                            OrderHistoryDisplay orderDisplay = new OrderHistoryDisplay(title, imageUrl, rentalPeriod, quantity, totalPrice,orderId);
                            orderList.add(orderDisplay);
                            adapter.notifyDataSetChanged();


                            // Kiểm tra adapter trước khi cập nhật
                            if (adapter != null) {
                                Log.d("DEBUG", "Updating adapter with " + orderList.size() + " items.");
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.e("ERROR", "orderHistoryAdapter is NULL when updating.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ManageOrderActivity.this, "Lỗi tải dữ liệu Post", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "Failed to load post data: " + error.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageOrderActivity.this, "Lỗi tải lịch sử đặt hàng", Toast.LENGTH_SHORT).show();
                Log.e("ERROR", "Failed to load  order history: " + error.getMessage());
            }
        });
    }
    private int parseSafeInt(String value) {
        if (value == null || value.isEmpty()) {
            return 0; // Trả về 0 nếu giá trị rỗng
        }
        try {
            // Loại bỏ dấu phẩy và chữ "VNĐ"
            value = value.replace(",", "").replace(" VNĐ", "").trim();
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0; // Tránh crash, trả về 0 nếu lỗi
        }
    }
}