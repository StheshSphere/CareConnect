package com.stheshsphere.careconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private LinearLayout emptyState;
    private LinearLayout filterOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize views
        rvHistory = findViewById(R.id.rvHistory);
        emptyState = findViewById(R.id.emptyState);
        filterOptions = findViewById(R.id.filterOptions);

        // Setup toolbar
        ImageView btnBack = findViewById(R.id.btnBackHistory);
        btnBack.setOnClickListener(v -> finish());

        // Setup filter button
        ImageView btnFilter = findViewById(R.id.btnFilterHistory);
        btnFilter.setOnClickListener(v -> toggleFilterOptions());

        // Setup filter chips
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipDonations = findViewById(R.id.chipDonations);
        Chip chipRequests = findViewById(R.id.chipRequests);

        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.addView(chipAll);
        chipGroup.addView(chipDonations);
        chipGroup.addView(chipRequests);

        chipAll.setOnClickListener(v -> filterHistory("all"));
        chipDonations.setOnClickListener(v -> filterHistory("donations"));
        chipRequests.setOnClickListener(v -> filterHistory("requests"));

        // Setup RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(getHistoryItems());
        rvHistory.setAdapter(adapter);

        // Show empty state if no history
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        }
    }

    private void toggleFilterOptions() {
        if (filterOptions.getVisibility() == View.VISIBLE) {
            filterOptions.setVisibility(View.GONE);
        } else {
            filterOptions.setVisibility(View.VISIBLE);
        }
    }

    private void filterHistory(String filter) {
        // Implement filtering logic here
        Toast.makeText(this, "Filtering by: " + filter, Toast.LENGTH_SHORT).show();
        filterOptions.setVisibility(View.GONE);
    }

    private List<HistoryItem> getHistoryItems() {
        // This would typically come from your database
        List<HistoryItem> items = new ArrayList<>();

        // Sample data
        items.add(new HistoryItem(
                "Donated Blankets",
                "Sep 5, 2023",
                "Completed",
                "donation",
                R.drawable.donate
        ));

        items.add(new HistoryItem(
                "Requested Books",
                "Sep 3, 2023",
                "In Progress",
                "request",
                R.drawable.request
        ));

        items.add(new HistoryItem(
                "Donated Food",
                "Aug 28, 2023",
                "Completed",
                "donation",
                R.drawable.donate
        ));

        return items;
    }
}