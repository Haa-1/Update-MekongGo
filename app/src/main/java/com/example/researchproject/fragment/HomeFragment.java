package com.example.researchproject.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.researchproject.Ad.Ad;
import com.example.researchproject.Ad.AdSliderAdapter;
import com.example.researchproject.Post.Post;
import com.example.researchproject.Post.PostAdapterGrid;
import com.example.researchproject.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.*;

import java.util.*;

public class HomeFragment extends Fragment {
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    private List<Post> postList;
    private List<String> searchSuggestions;
    private ArrayAdapter<String> suggestionAdapter;
    private DatabaseReference databaseReference;
    private AutoCompleteTextView searchView;
    private ViewPager2 viewPagerAds;
    private AdSliderAdapter adSliderAdapter;
    private List<Ad> adList;
    private DatabaseReference adsRef;
    private TabLayout tabDots;
    private final Handler handler = new Handler();
    private Runnable autoSlideRunnable;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postList = new ArrayList<>();
        searchSuggestions = new ArrayList<>();
        gridView = view.findViewById(R.id.gridView);
        searchView = view.findViewById(R.id.searchView);
        viewPagerAds = view.findViewById(R.id.viewPagerAds);
        tabDots = view.findViewById(R.id.tabDots);
        adList = new ArrayList<>();
        adSliderAdapter = new AdSliderAdapter(requireContext(), adList);
        viewPagerAds.setAdapter(adSliderAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(requireContext(), postList);
        gridView.setAdapter(postAdapter);
        loadSearchSuggestions();

        suggestionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, searchSuggestions);
        searchView.setAdapter(suggestionAdapter);

        // Tránh lỗi khi searchView rỗng
        searchView.setOnItemClickListener((parent, v, position, id) -> {
            String selectedQuery = suggestionAdapter.getItem(position);
            if (selectedQuery != null) {
                filterPosts(selectedQuery);
            }
        });

        searchView.setOnDismissListener(() -> {
            String query = searchView.getText().toString();
            if (query != null && !query.isEmpty()) {
                filterPosts(query);
            }
        });

        adsRef = FirebaseDatabase.getInstance().getReference("Ads");
        loadAds();
    }

    private void loadSearchSuggestions() {
        databaseReference.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueSuggestions = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        if (post.getTitle() != null) uniqueSuggestions.add(post.getTitle());
                        if (post.getAddress() != null) uniqueSuggestions.add(post.getAddress());
                        if (post.getServiceInfo() != null) uniqueSuggestions.add(post.getServiceInfo());
                    }
                }
                searchSuggestions.clear();
                searchSuggestions.addAll(uniqueSuggestions);
                suggestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterPosts(String query) {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : postList) {
            if (post.getTitle() != null && post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    post.getAddress() != null && post.getAddress().toLowerCase().contains(query.toLowerCase()) ||
                    post.getServiceInfo() != null && post.getServiceInfo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(post);
            }
        }
        postAdapter.updateData(filteredList);
    }

    private void loadAds() {
        adsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ad ad = dataSnapshot.getValue(Ad.class);
                    if (ad != null) {
                        adList.add(ad);
                    }
                }
                adSliderAdapter.notifyDataSetChanged();
                if (!adList.isEmpty()) {
                    new TabLayoutMediator(tabDots, viewPagerAds, (tab, position) -> {}).attach();
                    autoSlideAds();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi tải quảng cáo!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void autoSlideAds() {
        if (autoSlideRunnable != null) {
            handler.removeCallbacks(autoSlideRunnable);
        }

        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                if (adSliderAdapter.getItemCount() > 0) {
                    int currentItem = viewPagerAds.getCurrentItem();
                    int totalItems = adSliderAdapter.getItemCount();
                    viewPagerAds.setCurrentItem((currentItem + 1) % totalItems);
                    handler.postDelayed(this, 4000);
                }
            }
        };

        handler.postDelayed(autoSlideRunnable, 4000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoSlideRunnable != null) {
            handler.removeCallbacks(autoSlideRunnable);
        }
    }
}
