// DonateActivity.java
package com.example.lendahand;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
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
 * Activity for submitting donations, including:
 * - Selecting items from a server-loaded spinner
 * - Adding custom items via "Other" option
 * - Validating and submitting donations to server
 */
public class DonateActivity extends AppCompatActivity {

    // UI Components
    Spinner itemSpinner;
    EditText quantityInput, customItemInput;
    Button submitDonationButton;

    // Data structures
    List<String> itemList = new ArrayList<>();       // Holds display names for spinner
    Map<String, Integer> itemMap = new HashMap<>();  // Maps item names to their IDs
    ArrayAdapter<String> adapter;                    // Adapter for the spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        // Initialize UI components
        itemSpinner = findViewById(R.id.itemSpinner);
        quantityInput = findViewById(R.id.quantityInput);
        customItemInput = findViewById(R.id.customItemInput);
        submitDonationButton = findViewById(R.id.submitDonationButton);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button backToHomeButton = findViewById(R.id.backToHomeButton);

        backToHomeButton.setOnClickListener(v -> finish());
        // Set up spinner adapter with default Android layout
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(adapter);

        // Load available items from server
        loadItemsFromServer();

        // Handle "Other" selection - shows/hides custom item input
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = itemSpinner.getSelectedItem().toString();
                customItemInput.setVisibility(selected.equals("Other") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set submit button click handler
        submitDonationButton.setOnClickListener(this::doSubmit);
    }

    /**
     * Fetches available items from server and populates the spinner
     * Adds "Select an item" as first option and "Other" as last option
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
                            itemList.add(name);       // Add to display list
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    itemList.add("Other");  // Add "Other" option at the end
                    adapter.notifyDataSetChanged();  // Refresh spinner
                },
                error -> Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Handles donation submission with validation and server communication
     * @param v The clicked view (submit button)
     */
    public void doSubmit(View v) {
        if (!validateForm()) return;  // Validate before proceeding

        String selectedItem = itemSpinner.getSelectedItem().toString();
        String customItem = customItemInput.getText().toString().trim();
        String finalItem = selectedItem.equals("Other") ? customItem : selectedItem;
        String quantityStr = quantityInput.getText().toString().trim();

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting donation...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get logged-in username from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle "Other" item case (requires inserting new item first)
        if (selectedItem.equals("Other")) {
            String insertUrl = "https://lamp.ms.wits.ac.za/home/s2611748/insert_item.php";

            StringRequest insertItemRequest = new StringRequest(Request.Method.POST, insertUrl,
                    response -> {
                        try {
                            // Parse new item ID from response
                            JSONObject json = new JSONObject(response);
                            int itemId = json.getInt("item_id");
                            // Proceed with donation submission
                            sendDonation(username, itemId, quantityStr, progressDialog);
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Invalid item insert response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to insert item.", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("item_name", finalItem);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(insertItemRequest);
        } else {
            // Use existing item ID from the map
            int itemId = itemMap.get(finalItem);
            sendDonation(username, itemId, quantityStr, progressDialog);
        }
    }

    /**
     * Sends donation data to server
     * @param username Donor's username
     * @param itemId ID of the donated item
     * @param quantityStr Quantity being donated
     * @param progressDialog Loading dialog to dismiss when complete
     */
    private void sendDonation(String username, int itemId, String quantityStr, ProgressDialog progressDialog) {
        String url = "https://lamp.ms.wits.ac.za/home/s2611748/submit_donation.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.getString("status");
                        if (status.equals("success")) {
                            showSuccessDialog(json.optString("message", "Donation submitted successfully!"));
                        } else {
                            showErrorDialog(json.optString("message", "Submission failed"));
                        }
                    } catch (JSONException e) {
                        showErrorDialog("Invalid server response");
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    showErrorDialog("Network error: " + error.getMessage());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("item_id", String.valueOf(itemId));
                params.put("quantity", quantityStr);
                return params;
            }
        };

        // Set retry policy for the request
        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Shows success dialog and resets form
     * @param message Success message to display
     */
    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Reset form on success
                    itemSpinner.setSelection(0);
                    quantityInput.setText("");
                    customItemInput.setText("");
                })
                .show();
    }

    /**
     * Shows error dialog
     * @param errorMessage Error message to display
     */
    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Validates form inputs
     * @return true if all validations pass, false otherwise
     */
    public boolean validateForm() {
        String selectedItem = itemSpinner.getSelectedItem().toString();
        String quantityStr = quantityInput.getText().toString().trim();
        String customItem = customItemInput.getText().toString().trim();

        // Validate item selection
        if (selectedItem.equals("Select an item")) {
            Toast.makeText(this, "Please select an item", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate custom item if "Other" selected
        if (selectedItem.equals("Other")) {
            if (customItem.isEmpty() || customItem.length() < 4 || !customItem.matches("[a-zA-Z0-9 ]+")) {
                Toast.makeText(this, "Enter valid custom item (min 4 letters/numbers)", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Validate quantity presence
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate quantity format and range
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0 || quantity > 1000) {
                Toast.makeText(this, "Quantity must be 1-1000", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}