package com.example.runmapproapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.runmapproapp.R;
import com.example.runmapproapp.ui.admin.content.GroupsListFragment;
import com.example.runmapproapp.ui.admin.content.PostsListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ContentModerationFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_moderation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        setupViewPager();
    }

    private void setupViewPager() {
        ContentPagerAdapter adapter = new ContentPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.posts);
                    break;
                case 1:
                    tab.setText(R.string.groups);
                    break;
            }
        }).attach();
    }

    private static class ContentPagerAdapter extends FragmentStateAdapter {

        public ContentPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PostsListFragment();
                case 1:
                    return new GroupsListFragment();
                default:
                    return new PostsListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
