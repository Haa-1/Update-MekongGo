package com.example.researchproject;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.researchproject.fragment.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 mViewPager;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // Ánh xạ View
        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        // Thiết lập Adapter cho ViewPager2
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        mViewPager.setAdapter(adapter);

        // Lắng nghe sự kiện chuyển tab từ BottomNavigationView
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                mViewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_messageAI) {
                mViewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_cart) {
                mViewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_news) {
                mViewPager.setCurrentItem(3);
           /* } else if (itemId == R.id.navigation_guide) {
                mViewPager.setCurrentItem(4);*/
            } else if (itemId == R.id.navigation_settings) {
                mViewPager.setCurrentItem(4);
            }
            return true; // Cần thêm dòng này để xác nhận đã xử lý sự kiện
        });

        // Lắng nghe sự kiện vuốt của ViewPager2 để cập nhật BottomNavigationView
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_home);
                        break;
                    case 1:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_messageAI);
                        break;
                    case 2:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_cart);
                        break;
                    case 3:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_news);
                        break;
            //        case 4:
            //            mBottomNavigationView.setSelectedItemId(R.id.navigation_guide);
              //          break;
                    case 4:
                        mBottomNavigationView.setSelectedItemId(R.id.navigation_settings);
                        break;
                }
            }
        });

        // Nếu muốn cố định tab, ngăn người dùng vuốt ngang giữa các tab, hãy bật dòng này
        // mViewPager.setUserInputEnabled(false);
    }
}
