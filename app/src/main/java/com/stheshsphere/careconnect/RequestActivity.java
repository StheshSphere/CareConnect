package com.stheshsphere.careconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestActivity extends AppCompatActivity {

    private Spinner spinnerItems;
    private TextInputLayout layoutCustomItem, layoutCondition;
    private TextInputEditText etCustomItem, etCondition;
    private TextView tvQuantity;
    private Button btnMinus, btnPlus, btnRequest, btnViewMatch;

    private int quantity = 1;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        spinnerItems = findViewById(R.id.spinnerItems);
        layoutCustomItem = findViewById(R.id.layoutCustomItem);
        etCustomItem = findViewById(R.id.etCustomItem);
        layoutCondition = findViewById(R.id.layoutCondition);
        etCondition = findViewById(R.id.etCondition);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnRequest = findViewById(R.id.btnRequest);
        btnViewMatch = findViewById(R.id.btnViewMatch);

        // Load items from Firestore
        loadItemsFromFirestore();

        // Quantity buttons
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Request button
        btnRequest.setOnClickListener(v -> {
            String item = spinnerItems.getSelectedItem().toString();
            if (item.equals("Other")) {
                item = etCustomItem.getText().toString().trim();
                if (item.isEmpty()) {
                    etCustomItem.setError("Enter item name");
                    etCustomItem.requestFocus();
                    return;
                }
            }

            String condition = etCondition.getText().toString().trim();

            // Create request map
            Map<String, Object> request = new HashMap<>();
            request.put("userId", user.getUid());
            request.put("itemName", item);
            request.put("quantity", quantity);
            request.put("condition", condition.isEmpty() ? "N/A" : condition);
            request.put("timestamp", System.currentTimeMillis());

            // Save to Firestore
            db.collection("requests")
                    .add(request)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(RequestActivity.this,
                                "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                        // Reset form
                        spinnerItems.setSelection(0);
                        etCustomItem.setText("");
                        etCondition.setText("");
                        quantity = 1;
                        tvQuantity.setText(String.valueOf(quantity));
                        layoutCustomItem.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> Toast.makeText(RequestActivity.this,
                            "Failed to submit request: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        // View your match button
        btnViewMatch.setOnClickListener(v -> {
            // TODO: Navigate to match page
            Toast.makeText(RequestActivity.this,
                    "View your match clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadItemsFromFirestore() {
        db.collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> itemsList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String itemName = doc.getString("name");
                        if (itemName != null) {
                            itemsList.add(itemName);
                        }
                    }
                    itemsList.add("Other"); // Add "Other" at the end

                    // Populate spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RequestActivity.this,
                            android.R.layout.simple_spinner_item,
                            itemsList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerItems.setAdapter(adapter);

                    // Show/hide custom item EditText
                    spinnerItems.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            String selected = spinnerItems.getSelectedItem().toString();
                            if (selected.equals("Other")) {
                                layoutCustomItem.setVisibility(View.VISIBLE);
                            } else {
                                layoutCustomItem.setVisibility(View.GONE);
                                etCustomItem.setText(""); // Clear previous input
                            }
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestActivity.this,
                            "Failed to load items: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
