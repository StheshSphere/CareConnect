package com.example.lendahand;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateActivity extends AppCompatActivity {

    private EditText phonenumber, bio;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        phonenumber = findViewById(R.id.phonenumberUpdate);
        bio = findViewById(R.id.boiupdate);

        // Retrieve the username from SharedPreferences
        SharedPreferences sharedpreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        username = sharedpreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Username is not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void docontact(View v) {
        String phone = phonenumber.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            new UpdateContactTask().execute(phone);
        }
    }

    public void doBio(View v) {
        String newBio = bio.getText().toString().trim();

        if (newBio.isEmpty()) {
            Toast.makeText(this, "Bio cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            new UpdateBioTask().execute(newBio);
        }
    }

    // Task to update phone number
    private class UpdateContactTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String phoneNumber = params[0];
            String result = "";
            try {
                URL url = new URL("https://lamp.ms.wits.ac.za/home/s2611748/updateProfile.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String postData = "username=" + username + "&phone=" + phoneNumber;

                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                result = response.toString();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(UpdateActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    // Task to update bio
    private class UpdateBioTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String bioText = params[0];
            String result = "";
            try {
                URL url = new URL("https://lamp.ms.wits.ac.za/home/s2611748/updateProfile.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String postData = "username=" + username + "&bio=" + bioText;

                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                result = response.toString();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(UpdateActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    public void DoBack(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish(); // Optional: close current activity so it's removed from the back stack
    }
}
