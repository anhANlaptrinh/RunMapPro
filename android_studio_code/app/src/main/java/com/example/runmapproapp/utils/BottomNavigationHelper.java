package com.example.runmapproapp.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.runmapproapp.MapActivity;
import com.example.runmapproapp.R;
import com.example.runmapproapp.SettingsActivity;
import com.example.runmapproapp.ui.chat.ChatListActivity;
import com.example.runmapproapp.ui.groups.GroupListActivity;
import com.example.runmapproapp.ui.social.FeedActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    private static boolean isProgrammaticSelection = false;

    public static void setupBottomNavigation(
            @NonNull Activity activity,
            @NonNull BottomNavigationView bottomNav,
            int currentMenuItemId
    ) {

        // 1) Set selected item BEFORE enable listener (và chặn trigger)
        isProgrammaticSelection = true;
        bottomNav.setSelectedItemId(currentMenuItemId);
        isProgrammaticSelection = false;

        // 2) Setup listener AFTER selected item
        bottomNav.setOnItemSelectedListener(item -> {
            if (isProgrammaticSelection) return true;

            int itemId = item.getItemId();

            // Nếu đang ở màn đó rồi thì thôi
            if (itemId == currentMenuItemId) return true;

            Intent intent = null;

            if (itemId == R.id.nav_map) {
                intent = new Intent(activity, MapActivity.class);
            } else if (itemId == R.id.nav_feed) {
                intent = new Intent(activity, FeedActivity.class);
            } else if (itemId == R.id.nav_groups) {
                intent = new Intent(activity, GroupListActivity.class);
            } else if (itemId == R.id.nav_chat) {
                intent = new Intent(activity, ChatListActivity.class);
            } else if (itemId == R.id.nav_settings) {
                intent = new Intent(activity, SettingsActivity.class);
            }

            if (intent != null) {
                // ⚠️ CLEAR_TOP dễ gây reuse activity -> highlight sai
                // Dùng cái này ổn định hơn:
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
