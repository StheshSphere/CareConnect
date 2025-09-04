package com.stheshsphere.careconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    TextInputEditText username, email, password, confirmPassword;
    Button signupButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginRedirectText;
    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        progressBar = findViewById(R.id.progressBar);
        username = findViewById(R.id.inputUsername);
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputPasswordConfirm);
        signupButton = findViewById(R.id.btnSignUp);
        loginRedirectText = findViewById(R.id.txtLoginHere);

        loginRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        signupButton.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String semail = email.getText().toString().trim();
        String spassword = password.getText().toString().trim();
        String susername = username.getText().toString().trim();
        String sconfirmPassword = confirmPassword.getText().toString().trim();

        // Validations
        if (susername.isEmpty()) {
            username.setError("Username is required");
            username.requestFocus();
            return;
        }

        if (semail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(semail).matches()) {
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }

        if (spassword.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }
        if (spassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return;
        }

        if (!spassword.equals(sconfirmPassword)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Firebase signup
        mAuth.createUserWithEmailAndPassword(semail, spassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                // Create user document in Firestore
                                DocumentReference docRef = db.collection("users").document(uid);
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", susername);
                                userData.put("email", semail);
                                userData.put("createdAt", System.currentTimeMillis());
                                userData.put("profilePicUrl", ""); // optional, can update later
                                userData.put("role", "user");

                                docRef.set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignupActivity.this,
                                                    "Signup successful.", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignupActivity.this,
                                                    "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Signup failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
