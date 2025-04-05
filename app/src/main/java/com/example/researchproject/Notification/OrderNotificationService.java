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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrderNotificationService {
    private Context context;
    private DatabaseReference orderHistoryRef, postsRef, notificationsRef;
    private static final String CHANNEL_ID = "order_channel";
    private static final String PREFS_NAME = "OrderNotifications";
    private static final String KEY_NOTIFIED_ORDERS = "notified_orders";
    private long appStartTime;

    public OrderNotificationService(Context context) {
        this.context = context;
        orderHistoryRef = FirebaseDatabase.getInstance().getReference("Order_History");
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");

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
                    String orderId = orderSnapshot.getKey();
                    String buyerId = userSnapshot.getKey();
                    String postId = orderSnapshot.child("postId").getValue(String.class);

                    if (postId != null && orderTime != null && orderTime > appStartTime && !isOrderNotified(orderId)) {
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
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Log.d("DEBUG", "sellerId: " + sellerId);
                Log.d("DEBUG", "buyerId: " + buyerId);
                Log.d("DEBUG", "userId: " + userId);

                if(!(sellerId.equals(buyerId)) &&(sellerId.equals(userId))) {
                    sendNotification(sellerId, "Đơn hàng mới từ bài viết của bạn", "Ai đó đã đặt đơn hàng từ bài viết của bạn!", orderId);
                }else if(!(sellerId.equals(buyerId)) &&(buyerId.equals(userId))) {
                    sendNotification(buyerId, "Đơn hàng dat thanh cong", "ban đã đặt đơn hàng thanh cong!", orderId);
                }else if (sellerId.equals(buyerId)){
                    sendNotification(buyerId, "Đơn hàng dat thanh cong", "ban đã đặt đơn hàng thanh cong tu bai dang cu minh!", orderId);
                }
                // Lưu trạng thái đã thông báo
                saveNotifiedOrder(orderId);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void sendNotification(String userId, String title, String message, String orderId) {
        // Gửi thông báo cục bộ
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.search_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        // Lưu thông báo vào Firebase để đồng bộ hóa trên các thiết bị
        saveNotificationToFirebase(userId, "Order", title, message, orderId);
    }

    private void saveNotificationToFirebase(String userId, String type, String title, String content, String orderId) {
        DatabaseReference userNotificationsRef = notificationsRef.child(userId);
        String notificationId = userNotificationsRef.push().getKey();
        if (notificationId == null) return;

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", type);
        notificationData.put("title", title);
        notificationData.put("content", content);
        notificationData.put("orderId", orderId);
        notificationData.put("timestamp", System.currentTimeMillis());

        userNotificationsRef.child(notificationId).setValue(notificationData);
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

        notifiedOrders.add(orderId);
        prefs.edit().putStringSet(KEY_NOTIFIED_ORDERS, notifiedOrders).apply();
    }

}
