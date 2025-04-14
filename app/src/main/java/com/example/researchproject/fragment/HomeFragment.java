package com.example.researchproject.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import com.example.researchproject.Ad.Ad;
import com.example.researchproject.Ad.AdSliderAdapter;
import com.example.researchproject.MainActivity;
import com.example.researchproject.Notification.AdNotificationService;
import com.example.researchproject.Notification.OrderNotificationService;
import com.example.researchproject.Notification.PostNotificationService;
import com.example.researchproject.Post.Post;
import com.example.researchproject.Post.PostAdapterGrid;
import com.example.researchproject.Post.PostDetailActivity;
import com.example.researchproject.R;

import com.example.researchproject.mekoaipro.VoiceCommandManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;


import java.util.*;


public class HomeFragment extends Fragment {
    private static final int NOTIFICATION_PERMISSION_CODE = 5;
    // điều khiển thao tác bằng giọng nói
    private VoiceCommandManager voiceCommandManager;
    private static final int REQUEST_CODE_AUDIO = 1001;// dùng ghi âm
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    public static List<Post> postList = new ArrayList<>();
    private List<Post> originalPostList = new ArrayList<>();
    private List<String> searchSuggestions = new ArrayList<>();
    private ArrayAdapter<String> suggestionAdapter;
    private DatabaseReference databaseReference;
    private AutoCompleteTextView searchView;
    private FirebaseAuth mAuth;
    private ViewPager2 viewPagerAds;
    private AdSliderAdapter adSliderAdapter;
    private List<Ad> adList = new ArrayList<>();
    private DatabaseReference adsRef;
    private TabLayout tabDots;


    private Handler slideHandler = new Handler();


    public HomeFragment() {}


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
// điều khiển thao tác ứng dụng bằng giongj nói
        // Lấy VoiceCommandManager từ MainActivity
        voiceCommandManager = ((MainActivity) requireActivity()).getVoiceCommandManager();
        // Kiểm tra nếu voiceCommandManager không null, bắt đầu lắng nghe
        if (voiceCommandManager != null) {
            voiceCommandManager.startListeningWithSpeechRecognizer();
        } else {
            Log.e("HomeFragment", "VoiceCommandManager chưa được khởi tạo!");
        }
        VoiceCommandManager voiceCommandManager = new VoiceCommandManager(requireContext(), new VoiceCommandManager.VoiceCommandListener() {
            @Override
            public void onNavigateToHome() {
                Toast.makeText(requireContext(), "Đã ở trang chủ", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNavigateToMessageAI() {
                // Xử lý điều hướng đến MessageAI
            }
            @Override
            public void onNavigateToCart() {
                // Xử lý điều hướng đến Giỏ hàng
            }
            @Override
            public void onNavigateToNews() {
                // Xử lý điều hướng đến Tin tức
            }
            @Override
            public void onNavigateToSettings() {
                // Xử lý điều hướng đến Cài đặt
            }
            @Override
            public void onUnknownCommand(String command) {
                Toast.makeText(requireContext(), "Lệnh không xác định: " + command, Toast.LENGTH_SHORT).show();
            }
        });
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_AUDIO);
        }


        mAuth = FirebaseAuth.getInstance();
        gridView = view.findViewById(R.id.gridView);
        postAdapter = new PostAdapterGrid(requireContext(), postList);
        gridView.setAdapter(postAdapter);
        gridView.setNestedScrollingEnabled(true);


        searchView = view.findViewById(R.id.searchView);
        viewPagerAds = view.findViewById(R.id.viewPagerAds);
        tabDots = view.findViewById(R.id.tabDots);
        adSliderAdapter = new AdSliderAdapter(requireContext(), adList);
        viewPagerAds.setAdapter(adSliderAdapter);


        adsRef = FirebaseDatabase.getInstance().getReference("Ads");
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");


        loadAds();
        autoSlideAds();
        new TabLayoutMediator(tabDots, viewPagerAds, (tab, position) -> {}).attach();


        loadSearchSuggestions();


        suggestionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, searchSuggestions);
        searchView.setAdapter(suggestionAdapter);
        searchView.setThreshold(1);
        searchView.setOnItemClickListener((parent, v, position, id) -> {
            String selected = suggestionAdapter.getItem(position);
            searchView.setText(selected);
            filterPosts(selected);
        });
        searchView.setOnDismissListener(() -> filterPosts(searchView.getText().toString()));
        searchView.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchView.getText().toString().trim();
            if (!query.isEmpty()) filterPosts(query);
            return true;
        });


        gridView.setOnItemClickListener((parent, v, position, id) -> {
            Post selectedPost = postList.get(position);
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra("postId", selectedPost.getPostId());
            intent.putExtra("title", selectedPost.getTitle());
            intent.putExtra("serviceInfo", selectedPost.getServiceInfo());
            intent.putExtra("price", selectedPost.getPrice());
            intent.putExtra("rentalTime", selectedPost.getRentalTime());
            intent.putExtra("address", selectedPost.getAddress());
            intent.putExtra("contact", selectedPost.getContact());
            intent.putExtra("imageUrl", selectedPost.getImageUrl());
            startActivity(intent);
        });
        requestNotificationPermission();
        new PostNotificationService(requireContext());
        new OrderNotificationService(requireContext());
        new AdNotificationService(requireContext());


        return view;
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Bạn cần cấp quyền để nhận thông báo!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadSearchSuggestions() {
        databaseReference.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueSuggestions = new HashSet<>();
                List<Post> tempList = new ArrayList<>();


                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        tempList.add(post);
                        uniqueSuggestions.add(post.getTitle());
                        uniqueSuggestions.add(post.getAddress());
                        uniqueSuggestions.add(post.getServiceInfo());
                    }
                }


                tempList.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));


                postList.clear();
                postList.addAll(tempList);
                originalPostList.clear();
                originalPostList.addAll(tempList);


                searchSuggestions.clear();
                searchSuggestions.addAll(uniqueSuggestions);
                suggestionAdapter.notifyDataSetChanged();
                postAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filterPosts(String query) {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : originalPostList) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    post.getAddress().toLowerCase().contains(query.toLowerCase()) ||
                    post.getServiceInfo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(post);
            }
        }
        postAdapter.updateData(filteredList);
        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy kết quả!", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadAds() {
        adsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ad ad = dataSnapshot.getValue(Ad.class);
                    if (ad != null) adList.add(ad);
                }
                adSliderAdapter.notifyDataSetChanged();
                if (!adList.isEmpty()) {
                    new TabLayoutMediator(tabDots, viewPagerAds, (tab, position) -> {}).attach();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Lỗi tải quảng cáo!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void autoSlideAds() {
        slideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPagerAds.getCurrentItem();
                int totalItems = adSliderAdapter.getItemCount();
                viewPagerAds.setCurrentItem((currentItem + 1) % totalItems);
                slideHandler.postDelayed(this, 4000);
            }
        }, 4000);
    }
}



