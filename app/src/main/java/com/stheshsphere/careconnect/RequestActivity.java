package com.stheshsphere.careconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity {

    Spinner spinnerItems;
    TextInputLayout layoutCustomItem, layoutCondition;
    TextInputEditText etCustomItem, etCondition;
    TextView tvQuantity;
    Button btnMinus, btnPlus, btnRequest, btnViewMatch;
    int quantity = 1;

    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

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

        // Populate spinner with items
        String[] items = {"Rice", "Beans", "Bread", "Milk", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerItems.setAdapter(adapter);

        // Show/hide custom item input
        spinnerItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinnerItems.getSelectedItem().toString();
                if (selected.equals("Other")) {
                    layoutCustomItem.setVisibility(View.VISIBLE);
                } else {
                    layoutCustomItem.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutCustomItem.setVisibility(View.GONE);
            }
        });

        // Quantity increment/decrement
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

        // Handle request button click
        btnRequest.setOnClickListener(v -> sendRequest());

        // TODO: btnViewMatch click logic
        btnViewMatch.setOnClickListener(v -> {
            Toast.makeText(this, "View match clicked (logic not implemented yet)", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendRequest() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedItem = spinnerItems.getSelectedItem().toString();
        String customItem = etCustomItem.getText() != null ? etCustomItem.getText().toString().trim() : "";
        String condition = etCondition.getText() != null ? etCondition.getText().toString().trim() : "";

        if (selectedItem.equals("Other") && customItem.isEmpty()) {
            etCustomItem.setError("Please enter item name");
            etCustomItem.requestFocus();
            return;
        }

        // Prepare data
        Map<String, Object> request = new HashMap<>();
        request.put("userId", currentUser.getUid());
        request.put("item", selectedItem.equals("Other") ? customItem : selectedItem);
        request.put("quantity", quantity);
        request.put("condition", condition);

        // Save to Firestore (collection: "requests")
        db.collection("requests").add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Reset form
                    spinnerItems.setSelection(0);
                    etCustomItem.setText("");
                    layoutCustomItem.setVisibility(View.GONE);
                    etCondition.setText("");
                    quantity = 1;
                    tvQuantity.setText(String.valueOf(quantity));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
