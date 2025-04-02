package com.example.researchproject.admin;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.R;
import com.example.researchproject.Ad.Ad;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class ManageAdsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAds;
    private AdsAdapter adapter;
    private List<Ad> adList;
    private DatabaseReference adRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ads);
        recyclerViewAds = findViewById(R.id.recyclerViewAds);
        recyclerViewAds.setLayoutManager(new LinearLayoutManager(this));
        adRef = FirebaseDatabase.getInstance().getReference("Ads");
        adList = new ArrayList<>();
        adapter = new AdsAdapter(this, adList);
        recyclerViewAds.setAdapter(adapter);
        loadAds();
    }
    private void loadAds() {
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adList.clear();
                for (DataSnapshot adSnapshot : snapshot.getChildren()) {
                    Ad ad = adSnapshot.getValue(Ad.class);
                    adList.add(ad);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAdsActivity.this, "Lỗi tải quảng cáo!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}