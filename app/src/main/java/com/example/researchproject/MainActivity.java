package com.example.researchproject;


import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.researchproject.fragment.ViewPagerAdapter;
import com.example.researchproject.mekoaipro.VoiceCommandManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
public class MainActivity extends AppCompatActivity implements VoiceCommandManager.VoiceCommandListener {
    private VoiceCommandManager voiceCommandManager;// điều khiển thao tác bằng giọng nói
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
        // điều khiển thao tác ứng dụng bằng giọng nói
        voiceCommandManager = new VoiceCommandManager(this, this);


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
                if (position == 1) { // Vị trí của navigation_messageAI
                    if (voiceCommandManager != null) {
                        voiceCommandManager.destroy(); // Dừng khi vào MessageAI
                    }
                } else {
                    if (voiceCommandManager == null) {
                        voiceCommandManager = new VoiceCommandManager(MainActivity.this, MainActivity.this);
                    }
                }
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
    // điều khiển thao tác bằng giọng nói
    @Override
    public void onNavigateToHome() {
        mViewPager.setCurrentItem(0);
    }


    @Override
    public void onNavigateToMessageAI() {
        mViewPager.setCurrentItem(1);
    }


    @Override
    public void onNavigateToCart() {
        mViewPager.setCurrentItem(2);
    }


    @Override
    public void onNavigateToNews() {
        mViewPager.setCurrentItem(3);
    }


    @Override
    public void onNavigateToSettings() {
        mViewPager.setCurrentItem(4);
    }


    @Override
    public void onUnknownCommand(String command) {
        Toast.makeText(this, "Lệnh không xác định: " + command, Toast.LENGTH_SHORT).show();
    }
    public VoiceCommandManager getVoiceCommandManager() {
        return voiceCommandManager;
    }
    public void setupVoiceCommandManager() {
        if (voiceCommandManager == null) { // Chỉ tạo nếu chưa tồn tại
            voiceCommandManager = new VoiceCommandManager(this, this);
        }
    }
}



