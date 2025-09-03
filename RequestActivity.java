// RequestActivity.java
package com.example.lendahand;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for submitting item requests from available items.
 * Features:
 * - Loads available items from server
 * - Validates request inputs
 * - Submits requests to server
 * - Handles navigation back to HomeActivity
 */
public class RequestActivity extends AppCompatActivity {

    // UI Components
    Spinner itemSpinner;
    EditText edQuantity;
    Button btnRequest;

    // Data structures
    List<String> itemList = new ArrayList<>();       // Holds item names for spinner
    Map<String, Integer> itemMap = new HashMap<>();  // Maps item names to their IDs
    ArrayAdapter<String> adapter;                    // Adapter for the spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Initialize UI components
        itemSpinner = findViewById(R.id.itemSpinner);
        edQuantity = findViewById(R.id.editTextQuantity);
        btnRequest = findViewById(R.id.buttonSubmitRequest);
        Button buttonBack = findViewById(R.id.buttonBack);

        // Set up back button to return to HomeActivity
        buttonBack.setOnClickListener(v -> finish());

        // Configure spinner adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(adapter);

        // Load available items from server
        loadItemsFromServer();

        // Set submit button click handler
        btnRequest.setOnClickListener(this::submitRequest);
    }

    /**
     * Fetches available items from server and populates the spinner
     * Adds "Select an item" as default first option
     */
    private void loadItemsFromServer() {
        String url = "https://lamp.ms.wits.ac.za/home/s2611748/get_items.php";
        itemList.clear();
        itemList.add("Select an item");  // Default first option

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    // Parse JSON response and populate item list/map
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            String name = item.getString("item_name");
                            int id = item.getInt("item_id");
                            itemMap.put(name, id);  // Store name-ID mapping
                            itemList.add(name);     // Add to display list
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();  // Refresh spinner
                },
                error -> Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Handles request submission with validation and server communication
     * @param v The clicked view (submit button)
     */
    private void submitRequest(View v) {
        if (!validateForm()) return;  // Validate before proceeding

        String selectedItem = itemSpinner.getSelectedItem().toString();
        String quantityStr = edQuantity.getText().toString().trim();

        // Get logged-in username from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get item ID from the map and prepare server request
        int itemId = itemMap.get(selectedItem);
        String url = "https://lamp.ms.wits.ac.za/home/s2611748/submit_request.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Handle successful submission
                    Toast.makeText(this, "Request submitted!", Toast.LENGTH_SHORT).show();
                    // Reset form
                    itemSpinner.setSelection(0);
                    edQuantity.setText("");
                },
                error -> Toast.makeText(this, "Submission failed.", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Set POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("item_id", String.valueOf(itemId));
                params.put("quantity", quantityStr);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Validates form inputs
     * @return true if all validations pass, false otherwise
     */
    private boolean validateForm() {
        String selectedItem = itemSpinner.getSelectedItem().toString();
        String quantityStr = edQuantity.getText().toString().trim();

        // Validate item selection
        if (selectedItem.equals("Select an item")) {
            Toast.makeText(this, "Please select an item.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate quantity presence
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter quantity.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate quantity format and value
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
