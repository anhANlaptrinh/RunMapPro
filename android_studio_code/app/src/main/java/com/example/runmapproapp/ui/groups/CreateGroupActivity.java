package com.example.runmapproapp.ui.groups;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.model.CreateGroupRequest;
import com.example.runmapproapp.data.model.Group;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText etGroupName;
    private EditText etGroupDescription;
    private RadioGroup rgPrivacy;
    private Button btnCreate;
    private ProgressBar progressBar;
    private GroupApi groupApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        setupToolbar();
        initViews();
        setupListeners();

        groupApi = ApiClient.getGroupApi();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.create_group_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        etGroupName = findViewById(R.id.etGroupName);
        etGroupDescription = findViewById(R.id.etGroupDescription);
        rgPrivacy = findViewById(R.id.rgPrivacy);
        btnCreate = findViewById(R.id.btnCreate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnCreate.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String name = etGroupName.getText().toString().trim();
        String description = etGroupDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_group_name, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPrivacyId = rgPrivacy.getCheckedRadioButtonId();
        String privacy = selectedPrivacyId == R.id.rbPrivate ? "PRIVATE" : "PUBLIC";

        progressBar.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        CreateGroupRequest request = new CreateGroupRequest(name, description, null, privacy);
        
        groupApi.createGroup(request).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                progressBar.setVisibility(View.GONE);
                btnCreate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateGroupActivity.this, 
                            R.string.create_group_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateGroupActivity.this, 
                            R.string.cannot_create_group, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreate.setEnabled(true);
                Toast.makeText(CreateGroupActivity.this, 
                        getString(R.string.error_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
