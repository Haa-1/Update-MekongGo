package com.example.researchproject.History;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.Payment.Order.OrderItem;
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

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<OrderHistoryDisplay> orderHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerView = findViewById(R.id.recyclerViewOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryList = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(this, orderHistoryList, this::onOrderClicked);
        Log.e("orderHistoryAdapter", orderHistoryAdapter.toString());
        recyclerView.setAdapter(orderHistoryAdapter);

        loadOrderHistory();
    }
    private void onOrderClicked(String orderId) {
        Intent intent = new Intent(this, OrderHistoryDetailActivity.class);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }
    private void loadOrderHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order_History").child(userId);
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");

        if (orderHistoryAdapter == null) {
            Log.e("ERROR", "orderHistoryAdapter is NULL before fetching data");
            Log.e("orderHistoryAdapter", orderHistoryAdapter.toString());
            return;
        }

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderHistoryList.clear();

                Log.d("DEBUG", "Snapshot: " + snapshot.getValue());

                if (!snapshot.exists()) {
                    Log.e("ERROR", "No order history found for user: " + userId);
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
                            orderHistoryList.add(orderDisplay);
                            orderHistoryAdapter.notifyDataSetChanged();


                            // Kiểm tra adapter trước khi cập nhật
                            if (orderHistoryAdapter != null) {
                                Log.d("DEBUG", "Updating adapter with " + orderHistoryList.size() + " items.");
                                orderHistoryAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("ERROR", "orderHistoryAdapter is NULL when updating.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(OrderHistoryActivity.this, "Lỗi tải dữ liệu Post", Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "Failed to load post data: " + error.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryActivity.this, "Lỗi tải lịch sử đặt hàng", Toast.LENGTH_SHORT).show();
                Log.e("ERROR", "Failed to load  order history: " + error.getMessage());
            }
        });
    }
    // Hàm xử lý chuỗi số có đơn vị VNĐ
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