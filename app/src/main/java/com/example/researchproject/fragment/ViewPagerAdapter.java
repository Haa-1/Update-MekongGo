package com.example.researchproject.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// Import đầy đủ các Fragment để tránh lỗi
import com.example.researchproject.fragment.HomeFragment;
import com.example.researchproject.fragment.MessageAiFragment;
import com.example.researchproject.fragment.CartFragment;
import com.example.researchproject.fragment.PostNewsFragment;
import com.example.researchproject.fragment.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new MessageAiFragment();
            case 2:
                return new CartFragment();
            case 3:
                return new PostNewsFragment();
            case 4:
                return new SettingFragment();
            default:
                return new HomeFragment(); // Mặc định trả về HomeFragment nếu có lỗi
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Số lượng Fragment đúng với menu
    }
}
