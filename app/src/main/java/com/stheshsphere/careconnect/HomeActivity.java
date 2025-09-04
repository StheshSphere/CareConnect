package com.stheshsphere.careconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {
    Button btnLogout;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView email, username;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Make sure you have this layout file

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        btnLogout = findViewById(R.id.btnLogout);
        email = findViewById(R.id.tvProfileEmail);
        username = findViewById(R.id.tvProfileName);

        user = auth.getCurrentUser();

        if (user == null) {
            // No user logged in → send them to login
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        } else {
            // Set email
            email.setText(user.getEmail());

            // Fetch username from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String uname = documentSnapshot.getString("username");
                            username.setText(uname != null ? uname : "No name set");
                        } else {
                            username.setText("No name set");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this,
                                "Failed to load username: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        username.setText("No name set");
                    });
        }

        // Logout button
        btnLogout.setOnClickListener(view -> {
            auth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}
