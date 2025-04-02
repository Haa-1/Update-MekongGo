package com.example.researchproject.Notification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseReference notificationsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerViewNotification);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications").child(uid);
            loadNotifications();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                    // Sắp xếp danh sách theo timestamp từ mới đến cũ
                    Collections.sort(notificationList, new Comparator<Notification>() {
                        @Override
                        public int compare(Notification n1, Notification n2) {
                            return Long.compare(n2.getTimestamp(), n1.getTimestamp()); // Sắp xếp giảm dần
                        }
                    });
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Không thể tải thông báo", error.toException());
                Toast.makeText(NotificationActivity.this, "Lỗi khi tải thông báo!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteAllNotifications() {
        notificationsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    notificationList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(NotificationActivity.this, "Đã xóa tất cả thông báo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(NotificationActivity.this, "Lỗi khi xóa thông báo!", Toast.LENGTH_SHORT).show()
                );
    }
}