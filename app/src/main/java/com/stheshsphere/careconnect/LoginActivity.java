package com.stheshsphere.careconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText email, password;
    Button loginBtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView registerRedirectText;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        loginBtn = findViewById(R.id.btnSignIn);
        registerRedirectText = findViewById(R.id.txtCreateAccount);

        registerRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        loginBtn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String semail = email.getText().toString().trim();
        String spassword = password.getText().toString().trim();

        // Validations
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

        progressBar.setVisibility(View.VISIBLE);

        // Firebase sign in
        mAuth.signInWithEmailAndPassword(semail, spassword).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Exception e = task.getException();
                Log.e("LoginError", "Authentication failed", e);  // ← Add this
                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
