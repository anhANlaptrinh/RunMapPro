package com.example.runmapproapp.ui.groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.model.Group;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.runmapproapp.utils.BottomNavigationHelper;
import com.example.runmapproapp.utils.LocaleHelper;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupListActivity extends AppCompatActivity 
        implements GroupAdapter.OnGroupClickListener {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreateGroup;
    private TabLayout tabLayout;
    private GroupApi groupApi;

    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentTab = 0; // 0 = My Groups, 1 = Public Groups

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        setupToolbar();
        initViews();
        setupRecyclerView();
        setupListeners();

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_groups);

        groupApi = ApiClient.getGroupApi();
        loadGroups(false);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.groups_title);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewGroups);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        fabCreateGroup = findViewById(R.id.fabCreateGroup);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Setup tabs
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_groups));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.explore_groups));
    }

    private void setupRecyclerView() {
        groupAdapter = new GroupAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupAdapter);

        // Pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadGroups(true);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            loadGroups(false);
        });

        fabCreateGroup.setOnClickListener(v -> showGroupActionDialog());
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                currentPage = 0;
                isLastPage = false;
                loadGroups(false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showGroupActionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_action)
                .setItems(new String[]{
                        getString(R.string.join_group_action),
                        getString(R.string.create_new_group_action)
                }, (dialog, which) -> {
                    if (which == 0) {
                        // Join Group
                        showJoinGroupDialog();
                    } else {
                        // Create Group
                        Intent intent = new Intent(this, CreateGroupActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void showJoinGroupDialog() {
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        EditText editText = new EditText(this);
        editText.setHint(R.string.enter_invite_code_hint);
        editText.setPadding(50, 30, 50, 30);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.join_group_title)
                .setMessage(R.string.enter_invite_code)
                .setView(editText)
                .setPositiveButton(R.string.action_join, (dialog, which) -> {
                    String inviteCode = editText.getText().toString().trim();
                    if (!inviteCode.isEmpty()) {
                        joinGroupWithCode(inviteCode);
                    } else {
                        Toast.makeText(this, R.string.please_enter_invite_code, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void joinGroupWithCode(String inviteCode) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> body = new java.util.HashMap<>();
        body.put("inviteCode", inviteCode);

        groupApi.joinByInviteCode(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    String status = (String) result.get("status");
                    
                    if ("joined".equals(status)) {
                        Toast.makeText(GroupListActivity.this,
                                R.string.joined_group_success, Toast.LENGTH_SHORT).show();
                    } else if ("pending".equals(status)) {
                        Toast.makeText(GroupListActivity.this,
                                R.string.join_request_sent_approval, Toast.LENGTH_LONG).show();
                    } else if ("approved".equals(status)) {
                        Toast.makeText(GroupListActivity.this,
                                R.string.joined_group_success, Toast.LENGTH_SHORT).show();
                    }
                    // Refresh the list
                    currentPage = 0;
                    isLastPage = false;
                    loadGroups(false);
                } else {
                    Toast.makeText(GroupListActivity.this,
                            R.string.cannot_join_invalid_code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupListActivity.this,
                        getString(R.string.network_error_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroups(boolean loadMore) {
        if (currentTab == 0) {
            loadMyGroups(loadMore);
        } else {
            loadPublicGroups(loadMore);
        }
    }

    private void loadMyGroups(boolean loadMore) {
        if (isLoading) return;

        isLoading = true;
        if (!loadMore) {
            progressBar.setVisibility(View.VISIBLE);
        }

        groupApi.getMyGroups(currentPage, pageSize).enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(@NonNull Call<List<Group>> call, 
                                   @NonNull Response<List<Group>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Group> allGroups = response.body();
                    
                    android.util.Log.d("GroupList", "My Groups count: " + allGroups.size());
                    for (Group g : allGroups) {
                        android.util.Log.d("GroupList", "My Group: " + g.getName() + ", userRole: " + g.getUserRole());
                    }

                    if (allGroups.isEmpty()) {
                        isLastPage = true;
                        if (currentPage == 0) {
                            groupAdapter.setGroups(new ArrayList<>()); // Clear adapter
                            Toast.makeText(GroupListActivity.this, 
                                    R.string.not_joined_any_group, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (loadMore) {
                        groupAdapter.addGroups(allGroups);
                    } else {
                        groupAdapter.setGroups(allGroups);
                    }

                    currentPage++;
                } else {
                    Toast.makeText(GroupListActivity.this, 
                            R.string.cannot_load_groups, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Group>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(GroupListActivity.this, 
                        getString(R.string.network_error_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPublicGroups(boolean loadMore) {
        if (isLoading) return;

        isLoading = true;
        if (!loadMore) {
            progressBar.setVisibility(View.VISIBLE);
        }

        groupApi.getPublicGroups(currentPage, pageSize).enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(@NonNull Call<List<Group>> call,
                                   @NonNull Response<List<Group>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Group> publicGroups = response.body();
                    
                    android.util.Log.d("GroupList", "Public Groups count: " + publicGroups.size());
                    for (Group g : publicGroups) {
                        android.util.Log.d("GroupList", "Public Group: " + g.getName() + ", userRole: " + g.getUserRole());
                    }

                    if (publicGroups.isEmpty()) {
                        isLastPage = true;
                        if (currentPage == 0) {
                            groupAdapter.setGroups(new ArrayList<>()); // Clear adapter
                            Toast.makeText(GroupListActivity.this, 
                                    R.string.no_public_groups, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (loadMore) {
                        groupAdapter.addGroups(publicGroups);
                    } else {
                        groupAdapter.setGroups(publicGroups);
                    }

                    currentPage++;
                } else {
                    Toast.makeText(GroupListActivity.this,
                            R.string.cannot_load_public_groups, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Group>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(GroupListActivity.this,
                        getString(R.string.network_error_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onGroupClick(Group group) {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("groupId", group.getId());
        intent.putExtra("groupName", group.getName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning from create/edit
        currentPage = 0;
        isLastPage = false;
        loadGroups(false);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_groups);
    }
}
