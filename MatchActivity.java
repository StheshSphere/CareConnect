package com.example.lendahand;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchActivity extends AppCompatActivity {

    private ListView matchListView;
    private ArrayList<HashMap<String, String>> matchList = new ArrayList<>();
    private MatchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        matchListView = findViewById(R.id.matchListView);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        adapter = new MatchAdapter(this, matchList);
        matchListView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing matches...", Toast.LENGTH_SHORT).show();
            fetchMatches();
        });

        fetchMatches();
    }

    private void fetchMatches() {
        String url = "https://lamp.ms.wits.ac.za/home/s2611748/get_matches.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    matchList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            HashMap<String, String> match = new HashMap<>();
                            match.put("request_id", obj.getString("request_id"));
                            match.put("requester_username", obj.getString("requester_username"));
                            match.put("item_name", obj.getString("item_name"));
                            match.put("quantity_needed", obj.getString("quantity_needed"));
                            matchList.add(match);
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error parsing match data", Toast.LENGTH_SHORT).show();
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (matchList.isEmpty()) {
                        Toast.makeText(this, "No current matches found", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch matches", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Update the matchList after a donation.
     * If quantityDonated < quantity_needed, reduce the quantity needed.
     * If quantityDonated >= quantity_needed, remove the match.
     */
    public void donateAmount(String requestId, int quantityDonated) {
        for (int i = 0; i < matchList.size(); i++) {
            HashMap<String, String> match = matchList.get(i);
            if (match.get("request_id").equals(requestId)) {
                int currentQty = Integer.parseInt(match.get("quantity_needed"));
                int newQty = currentQty - quantityDonated;

                if (newQty > 0) {
                    match.put("quantity_needed", String.valueOf(newQty));
                } else {
                    matchList.remove(i);
                }

                adapter.notifyDataSetChanged();
                break;
            }
        }
    }
}