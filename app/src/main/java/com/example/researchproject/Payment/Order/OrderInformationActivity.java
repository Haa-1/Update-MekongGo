package com.example.researchproject.Payment.Order;

import static java.lang.String.valueOf;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.researchproject.Payment.Api.CreateOrder;
import com.example.researchproject.R;
import com.example.researchproject.fragment.HomeFragment;
import com.example.researchproject.Post.PostDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderInformationActivity extends AppCompatActivity implements OrderItemAdapter.OnQuantityChangeListener {
    private EditText editName, editAddress,editPhone;
    private ListView orderItem;
    private TextView txtTotal;
    private EditText editRentalPeriod;
    private Button btnDecrease, btnBack,btnIncrease, btnOrder;

    private int rentalPeriod = 1;
    private double totalPrice;
    private String postId, title;
    private double pricePerItem;
    private List<OrderItem> orderItems;
    private OrderItemAdapter orderItemAdapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_information);

        // Ánh xạ UI
        editName = findViewById(R.id.editName);
        editAddress = findViewById(R.id.editAddress);
        editPhone = findViewById(R.id.editPhone);
        orderItem = findViewById(R.id.orderItem);
        txtTotal = findViewById(R.id.txtTotal);
        editRentalPeriod = findViewById(R.id.editRetailPeriod);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnOrder = findViewById(R.id.btnOrder);
        Button btnBack = findViewById(R.id.btnBack);
        databaseReference = FirebaseDatabase.getInstance().getReference("Order_History");


        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            postId = intent.getStringExtra("postId");
            title = intent.getStringExtra("title");
            pricePerItem = Double.parseDouble(intent.getStringExtra("price"));
        }

        // Khởi tạo danh sách sản phẩm
        orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(title, pricePerItem, 1));

        // Thiết lập adapter
        orderItemAdapter = new OrderItemAdapter(this, orderItems, this);
        orderItem.setAdapter(orderItemAdapter);

        // Tính tổng tiền ban đầu
        updateTotalPrice();
        // Xử lý sự kiện khi bấm nút Back
        btnBack.setOnClickListener(v -> showExitConfirmationDialog());
        // Xử lý tăng giảm thời gian thuê
        btnIncrease.setOnClickListener(v -> {
            rentalPeriod++;
            editRentalPeriod.setText(valueOf(rentalPeriod));
            updateTotalPrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (rentalPeriod > 1) {
                rentalPeriod--;
                editRentalPeriod.setText(valueOf(rentalPeriod));
                updateTotalPrice();
            }
        });

        //cho phep truy cap zalopay
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        // Xử lý đặt hàng
        btnOrder.setOnClickListener(v -> {
            String customerName = editName.getText().toString().trim();
            String customerAddress = editAddress.getText().toString().trim();
            String customerPhone = editPhone.getText().toString().trim();

            if (customerName.isEmpty() || customerAddress.isEmpty() || customerPhone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            processZaloPayPayment();
            // Lưu đơn hàng (Có thể gửi lên Firebase hoặc lưu vào DB)
            saveOrderToDatabase();

        });

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông tin đặt hàng"); // Tùy chỉnh tiêu đề
        }
    }
    // Xử lý khi nhấn nút Back trên ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Quay lại PostDetailActivity
            Intent intent = new Intent(OrderInformationActivity.this, PostDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Đảm bảo không mở nhiều Activity trùng lặp
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onQuantityChanged() {
        updateTotalPrice();
    }
    private void updateTotalPrice() {
        totalPrice = 0;
        for (OrderItem item : orderItems) {
            totalPrice += item.getPrice() * item.getQuantity() * rentalPeriod;
        }
        txtTotal.setText(String.format("%.3f VNĐ", totalPrice));

    }

    private void processZaloPayPayment() {
        Toast.makeText(this, "Đang thực hiện thanh toán qua ZaloPay...", Toast.LENGTH_SHORT).show();
        String totalString = txtTotal.getText().toString().replaceAll("[^0-9.]", "").trim();
        Log.d("totalPrice", totalString);
        try {
            CreateOrder orderApi = new CreateOrder();
            JSONObject data = orderApi.createOrder(totalString);
            String code = data.getString("return_code");
            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(OrderInformationActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {

                        Log.d("result", "Thanh toán thành công"+s );
                        String customerName = editName.getText().toString().trim();
                        String customerAddress = editAddress.getText().toString().trim();
                        String customerPhone = editPhone.getText().toString().trim();
                        String rentalPeriod = editRentalPeriod.getText().toString().trim();
                        String totalPrice = txtTotal.getText().toString().trim();
                        String quantity = String.valueOf(orderItems.get(0).getQuantity());
                        String postId = getIntent().getStringExtra("postId");

                        Log.d("Zalo","Ma giao dich"+token);
                        Log.d("Zalo","return-code"+ code);


                        Intent intent1 = new Intent(OrderInformationActivity.this, OrderSuccessfulActivity.class);
                        intent1.putExtra("customerName", customerName);
                        intent1.putExtra("customerAddress", customerAddress);
                        intent1.putExtra("customerPhone", customerPhone);
                        intent1.putExtra("rentalPeriod", rentalPeriod);
                        intent1.putExtra("quantity", quantity);
                        intent1.putExtra("totalPrice", totalPrice);
                        intent1.putExtra("postId", postId);
                        Log.d("result", "Thanh toán thành công");
                        startActivity(intent1);
                        Log.d("result", "Thanh toán thành công");
                    }
                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Intent intent1 = new Intent(OrderInformationActivity.this, HomeFragment.class);
                        intent1.putExtra("result", "Hủy thanh toán");
                        startActivity(intent1);
                    }
                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Intent intent1 = new Intent(OrderInformationActivity.this, HomeFragment.class);
                        intent1.putExtra("result", "Lỗi thanh toán");
                        startActivity(intent1);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void saveOrderToDatabase(){
        String customerName = editName.getText().toString().trim();
        String customerAddress = editAddress.getText().toString().trim();
        String customerPhone = editPhone.getText().toString().trim();
        String rentalPeriod = editRentalPeriod.getText().toString().trim();

        String totalPrice = txtTotal.getText().toString().trim();
        String quantity = String.valueOf(orderItems.get(0).getQuantity());
        String postId = getIntent().getStringExtra("postId");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Order_History");
        String orderId = databaseReference.child(userId).push().getKey();
        Map<String, Object> orderHistory = new HashMap<>();
        orderHistory.put("customerName", customerName);
        orderHistory.put("customerAddress", customerAddress);
        orderHistory.put("customerPhone", customerPhone);
        orderHistory.put("rentalPeriod", rentalPeriod);
        orderHistory.put("orderId", orderId);
        orderHistory.put("quantity",quantity);
        orderHistory.put("totalPrice", totalPrice);
        orderHistory.put("postId", postId);
        databaseReference.child(userId).child(orderId).setValue(orderHistory)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Giao dịch được lưu thành công"))
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi lưu giao dịch", e));
    }
    private void showExitConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Khách hàng chưa thanh toán, vui lòng thanh toán!")
                .setPositiveButton("Tiếp tục thanh toán", (dialog, which) -> dialog.dismiss()) // Giữ nguyên trang
                .setNegativeButton("Thoát", (dialog, which) -> {
                    // Quay về PostDetailActivity
                    Intent intent = new Intent(OrderInformationActivity.this, PostDetailActivity.class);
                    startActivity(intent);
                    finish(); // Đóng Activity hiện tại
                })
                .setCancelable(false) // Không thể bấm ra ngoài để thoát
                .show();
    }
}