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

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.researchproject.R;
import com.example.researchproject.Post.PostDetailActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;

public class AdNotificationService {
    private Context context;
    private DatabaseReference adRef;
    private static final String CHANNEL_ID = "ad_channel";
    private long appStartTime;

    public AdNotificationService(Context context) {
        this.context = context;
        adRef = FirebaseDatabase.getInstance().getReference("Ads");

        // Lưu thời gian ứng dụng khởi động
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        appStartTime = prefs.getLong("appStartTime", 0);
        if (appStartTime == 0) {
            appStartTime = System.currentTimeMillis();
            prefs.edit().putLong("appStartTime", appStartTime).apply();
        }
        createNotificationChannel();
        listenForNewAds();
    }

    private void listenForNewAds() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long appStartTime = prefs.getLong("appStartTime", System.currentTimeMillis());

        adRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (snapshot.exists()) {
                    Long AdTime = snapshot.child("timestamp").getValue(Long.class);
                    String adId = snapshot.getKey(); // Lấy ID bài đăng
                    String title = snapshot.child("title").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Kiểm tra xem bài đăng đã được thông báo chưa
                    if (adId != null && !isPostNotified(adId)) {
                        showNotification(adId,title,imageUrl);
                        saveNotifiedPost(adId); // Lưu bài đăng đã thông báo
                    }
                    // Chỉ gửi thông báo nếu bài đăng mới hơn thời gian ứng dụng khởi chạy
                    if (AdTime != null && AdTime > appStartTime) {
                        showNotification(adId,title,imageUrl);
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
    private void showNotification(String adId, String title,  String imageUrl) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra("adId", adId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.search_icon)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                                    .bigPicture(resource)
                                    .bigLargeIcon((Icon) null); // Ẩn icon lớn khi mở rộng

                            builder.setStyle(bigPictureStyle)
                                    .setLargeIcon(resource);

                            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        } else {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Ads Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void saveNotifiedPost(String adId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(adId, true); // Đánh dấu bài đăng đã thông báo
        editor.apply();
    }

    private boolean isPostNotified(String adId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(adId, false); // Kiểm tra xem bài đăng đã được thông báo chưa
    }

}