package com.example.lendahand;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class ViewProfileActivity extends AppCompatActivity {

    TextView phoneNumberTextView, bioTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // Find TextViews from layout
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        bioTextView = findViewById(R.id.bioTextView);

        // Retrieve the username from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        if (username.isEmpty()) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the URL to call the PHP script with POST parameters
        String url = "https://lamp.ms.wits.ac.za/home/s2611748/viewProfile.php";

        // Use Volley StringRequest with POST method
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);
                            phoneNumberTextView.setText(obj.getString("phone_number"));
                            bioTextView.setText(obj.getString("bio")); // ✅ fixed key name
                        } else {
                            Toast.makeText(this, "Profile not found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing profile data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error.", Toast.LENGTH_SHORT).show()
        ) {
            // Send the username as POST parameter
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("username", username);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void doBackViewProfile(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
