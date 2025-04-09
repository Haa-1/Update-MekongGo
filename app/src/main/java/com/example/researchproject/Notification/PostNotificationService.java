package com.example.researchproject.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PostNotificationService {
    private Context context;
    private DatabaseReference postsRef, notificationsRef, usersRef;
    private static final String CHANNEL_ID = "post_channel";
    private static final String PREFS_NAME = "NotifiedPosts";
    private static final String KEY_NOTIFIED_POST = "notified_orders";
    private long appStartTime;
    private SharedPreferences sharedPreferences;

    public PostNotificationService(Context context) {
        this.context = context;
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        appStartTime = prefs.getLong("appStartTime", 0);
        if (appStartTime == 0) {
            appStartTime = System.currentTimeMillis();
            prefs.edit().putLong("appStartTime", appStartTime).apply();
        }
        createNotificationChannel();
        listenForNewPosts();
    }
    private void listenForNewPosts() {
        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (!snapshot.exists()) {
                    Log.d("DEBUG", "Không có bài viết mới");
                    return;
                }
                Log.d("DEBUG", "Có bài viết mới: " + snapshot.getKey());

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String postId = snapshot.getKey();
                String authorId = snapshot.child("uid").getValue(String.class);
                Long postTime = snapshot.child("timestamp").getValue(Long.class);
                String title = snapshot.child("title").getValue(String.class);

                if(userId.equals(authorId) && postId != null && authorId != null && postTime != null && postTime > appStartTime && !isOrderNotified(postId)){
                    notifyAuthor(authorId, title, postId);
                }else if(!(userId.equals(authorId)) && postId != null && authorId != null && postTime != null && postTime > appStartTime && !isOrderNotified(postId)) {
                    notifyAllUsers(postId, title);
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

    private boolean isOrderNotified(String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> notifiedOrders = prefs.getStringSet(KEY_NOTIFIED_POST, new HashSet<>());
        return notifiedOrders.contains(postId);
    }

    private void saveNotifiedOrder(String postId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> notifiedOrders = new HashSet<>(prefs.getStringSet(KEY_NOTIFIED_POST, new HashSet<>()));

        notifiedOrders.add(postId);
        prefs.edit().putStringSet(KEY_NOTIFIED_POST, notifiedOrders).apply();
    }


    private void notifyAllUsers(String postId, String title) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        sendNotification(userId, "Bài viết mới", "Có bài viết mới: " + title, postId);
                    }
                    saveNotifiedOrder(postId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void notifyAuthor(String authorId, String title, String postId) {
        sendNotification(authorId, "Đăng bài thành công", "Bài viết của bạn đã được đăng: " + title, null);
        saveNotifiedOrder(postId);
    }

    private void sendNotification(String userId, String title, String message, String postId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.search_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        saveNotificationToFirebase(userId, "Post", title, message, postId);
    }

    private void saveNotificationToFirebase(String userId, String type, String title, String content, String postId) {
        DatabaseReference userNotificationsRef = notificationsRef.child(userId);
        String notificationId = userNotificationsRef.push().getKey();
        if (notificationId == null) return;

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", type);
        notificationData.put("title", title);
        notificationData.put("content", content);
        notificationData.put("timestamp", System.currentTimeMillis());
        if (postId != null) {
            notificationData.put("postId", postId);
        }

        userNotificationsRef.child(notificationId).setValue(notificationData);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Post Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}