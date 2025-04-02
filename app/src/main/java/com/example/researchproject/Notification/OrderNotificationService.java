package com.example.researchproject.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.researchproject.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;

public class OrderNotificationService {
    private Context context;
    private DatabaseReference orderHistoryRef, postsRef;
    private static final String CHANNEL_ID = "order_channel";
    private static final String PREFS_NAME = "OrderNotifications";
    private static final String KEY_NOTIFIED_ORDERS = "notified_orders";
    private long appStartTime;

    public OrderNotificationService(Context context) {
        this.context = context;
        orderHistoryRef = FirebaseDatabase.getInstance().getReference("Order_History");
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        // Lưu thời gian ứng dụng khởi động
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        appStartTime = prefs.getLong("appStartTime", 0);
        if (appStartTime == 0) {
            appStartTime = System.currentTimeMillis();
            prefs.edit().putLong("appStartTime", appStartTime).apply();
        }

        createNotificationChannel();
        listenForNewOrders();
    }
    private void listenForNewOrders() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long appStartTime = prefs.getLong("appStartTime", System.currentTimeMillis());

        orderHistoryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot userSnapshot, String previousChildName) {

                if (!userSnapshot.exists()) {
                    Log.d("DEBUG", "Không có đơn hàng mới");
                    return;
                }
                Log.d("DEBUG", "Có dữ liệu mới: " + userSnapshot.getValue());

                for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                    Long orderTime = orderSnapshot.child("timestamp").getValue(Long.class);
                    String orderId = orderSnapshot.getKey();  // ID đơn hàng
                    String buyerId = userSnapshot.getKey();  // ID người mua
                    String postId = orderSnapshot.child("postId").getValue(String.class);
                    if (postId != null && !isOrderNotified(orderId)) {
                        notifyUsers(postId, buyerId, orderId);
                    }
                    if (orderTime != null && orderTime > appStartTime) {
                        notifyUsers(postId, buyerId, orderId);
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void notifyUsers(String postId, String buyerId, String orderId) {
        postsRef.child(postId).child("uid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sellerId = snapshot.getValue(String.class);

                if (sellerId != null) {
                    // Thông báo cho người đăng bài (người bán)
                    sendNotification(sellerId, "Ai đó đã đặt đơn hàng từ bài viết của bạn!");
                    // Thông báo cho người mua
                    sendNotification(buyerId, "Bạn đã đặt hàng thành công!");
                    // Lưu trạng thái đã thông báo
                    saveNotifiedOrder(orderId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void sendNotification(String userId, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.search_icon)
                .setContentTitle("Thông báo đơn hàng")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Order Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private boolean isOrderNotified(String orderId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> notifiedOrders = prefs.getStringSet(KEY_NOTIFIED_ORDERS, new HashSet<>());
        return notifiedOrders.contains(orderId);
    }

    private void saveNotifiedOrder(String orderId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> notifiedOrders = new HashSet<>(prefs.getStringSet(KEY_NOTIFIED_ORDERS, new HashSet<>()));

        notifiedOrders.add(orderId); // Thêm orderId vào danh sách đã thông báo

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_NOTIFIED_ORDERS, notifiedOrders);
        editor.apply();
    }


}