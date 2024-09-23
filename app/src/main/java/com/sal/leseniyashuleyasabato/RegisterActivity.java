package com.sal.leseniyashuleyasabato;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    String countryCode;
    private ArrayList<String> countryNames = new ArrayList<>();
    private ArrayList<String> countryCodes = new ArrayList<>();
    private FirebaseUser user;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already registered
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean("isRegistered", false);
        boolean isLogged = prefs.getBoolean("isLogged", true);
        // Save registration status in SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
                   

        
                // Check if user is logged in
        if (user != null && isRegistered == true) {
            // User is logged in, show log out and add account options
            setContentView(R.layout.activity_logged_in);

            Button logoutButton = findViewById(R.id.logoutButton);
            Button addAccountButton = findViewById(R.id.addAccountButton);
            
            TextView welcomeText = findViewById(R.id.welcomeText);
            String userEmail = prefs.getString("userEmail", "Haipo/ Non Existent");
            
            // Display the email
            welcomeText.setText("Karibu! Umeingia kwenye akaunti "+ userEmail + "\n Welcome! You are logged in " + userEmail);

            // Set onClickListeners for the buttons
            logoutButton.setOnClickListener(v -> {
                auth.signOut(); // Sign out the user
                editor.putBoolean("isRegistered", false); // Clear registration status
                editor.apply();
                Toast.makeText(RegisterActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                // Redirect to the main activity after logging out
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            addAccountButton.setOnClickListener(v -> {
                // Allow the user to add another account
                Db();  
            });

        } else if (!isRegistered || isLogged) {
            auth.signOut(); // Sign out the user
            editor.putBoolean("isRegistered", false); // Clear registration status
            editor.apply();
            Db();
            }
    }
    
   private void Db(){
        
       // Show registration activity
            setContentView(R.layout.register);
            SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();

            // Initialize Firebase Database
            mDatabase = FirebaseDatabase.getInstance().getReference();
            
            // Find views by ID
            EditText nameField = findViewById(R.id.nameField);
            EditText phoneField = findViewById(R.id.phoneField);
            EditText emailField = findViewById(R.id.emailField);
            EditText passwordField = findViewById(R.id.passwordField);
            AutoCompleteTextView countryField = findViewById(R.id.countryField);
            Button registerButton = findViewById(R.id.registerButton);
            TextView registeredButton = findViewById(R.id.registered);
            TextView skipButton = findViewById(R.id.skipButton);
            
            
            // Load the countries from the JSON file
            loadCountriesFromJson();

            // Set up the adapter for the AutoCompleteTextView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countryNames);
            countryField.setAdapter(adapter);

            // Handle the selection of a country
            countryField.setOnItemClickListener((parent, view, position, id) -> {
                String selectedCountry = (String) parent.getItemAtPosition(position);

            // Find the corresponding country code
                int index = countryNames.indexOf(selectedCountry);
                if (index != -1) {
                   countryCode = countryCodes.get(index);
                    Toast.makeText(RegisterActivity.this, "Country: " + selectedCountry + ", Code: " + countryCode, Toast.LENGTH_SHORT).show();
                }
            });
            
            registeredButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoginPopup();
                }
            });
            

            // Set onClickListener for the register button
            registerButton.setOnClickListener(new View.OnClickListener() {
                    
                @Override
                public void onClick(View v) {
                    // Get text from EditText fields
                    String name = nameField.getText().toString().trim();
                    String phone = phoneField.getText().toString().trim();
                    String email = emailField.getText().toString().trim();
                    String country = countryField.getText().toString().trim();
                    String password = passwordField.getText().toString();    
                        
                    // Validate input
                    if (name.isEmpty()) {
                        nameField.setError("Jina linahitajika");
                        nameField.requestFocus();
                        return;
                    }

                    if (phone.isEmpty()) {
                        phoneField.setError("Nambari ya simu inahitajika");
                        phoneField.requestFocus();
                        return;
                    }

                    if (email.isEmpty()) {
                        emailField.setError("Barua pepe inahitajika");
                        emailField.requestFocus();
                        return;
                    }

                    if (country.isEmpty()) {
                        countryField.setError("Nchi inahitajika");
                        countryField.requestFocus();
                        return;
                    }    
                        
                    if (TextUtils.isEmpty(passwordField.getText())) {
                        passwordField.setError("Neno siri linahitajika");
                        passwordField.requestFocus();
                        return;
                    }    
                            
                    
                auth = FirebaseAuth.getInstance();
                        
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // User registered successfully
                        user = auth.getCurrentUser();
                        // Save user email and login status
                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("userEmail", email);  // Assuming userEmail contains the email
                        editor.apply();        

                        // Optionally send a verification email
                        user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                            if (verificationTask.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                            }
                        });
                                        
                        String userId = user.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        User newUser = new User(name, phone, email, country, countryCode);
                        userRef.setValue(newUser);

                        editor.putBoolean("isRegistered", true);
                        editor.apply();

                        // Navigate to main content
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        // User is already registered, navigate to main content
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                
                });
                        
                                 

            // Set onClickListener for the skip button    
            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Skip action (e.g., navigate to another activity)
                    editor.putBoolean("isRegistered", true);
                    editor.apply();
                    Toast.makeText(RegisterActivity.this, "Haujasajiliwa", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
        });

   }
    
    // Load countries and their codes from the JSON file
    private void loadCountriesFromJson() {
        try {
            // Read the JSON file
            InputStream is = getAssets().open("countries.json"); // For assets
            // InputStream is = getResources().openRawResource(R.raw.countries); // For raw directory

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Parse the JSON data
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject countryObject = jsonArray.getJSONObject(i);
                String countryName = countryObject.getString("name");
                String countryCode = countryObject.getString("dial_code");

                countryNames.add(countryName);
                countryCodes.add(countryCode);
            }

        } catch (Exception e) {
            Toast.makeText(RegisterActivity.this, "Jaribu Tena baadae", Toast.LENGTH_SHORT).show();
            Log.e("RegisterActivity", "Error reading JSON file", e);
        }
    }
    
    private void showLoginPopup() {
    final Dialog dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
    dialog.setContentView(R.layout.login_popup);

    // Find views in the dialog
    EditText emailLoginField = dialog.findViewById(R.id.emailLoginField);
    EditText passwordLoginField = dialog.findViewById(R.id.passwordLoginField);
    Button loginButton = dialog.findViewById(R.id.loginButton);
    Button cancelButton = dialog.findViewById(R.id.cancelButton);
    TextView forgot = dialog.findViewById(R.id.forgot);   

    // Set onClickListener for the login button
    loginButton.setOnClickListener(v -> {
        String email = emailLoginField.getText().toString().trim();
        String password = passwordLoginField.getText().toString().trim();
                
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userEmail", email);  // Assuming userEmail contains the email
        editor.apply();           

        // Validate input
        if (email.isEmpty()) {
            emailLoginField.setError("Barua pepe inahitajika");
            emailLoginField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordLoginField.setError("Neno siri linahitajika");
            passwordLoginField.requestFocus();
            return;
        }

        // Perform login action using FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Login successful, navigate to main activity
                    editor.putBoolean("isRegistered", true);
                    editor.apply();
        
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Login failed, show error message
                            
                    Toast.makeText(RegisterActivity.this, "Hakikisha kuna Mtandao pia angalia taarifa zako.", Toast.LENGTH_SHORT).show();
                    emailLoginField.setError("Huenda Barua pepe si sahihi. Rekebisha");
                    emailLoginField.requestFocus();
                    passwordLoginField.setError("Huenda Neno siri si sahihi. Rekebisha");
                    passwordLoginField.requestFocus();
                    return;        
                }
            });
    });
        
    forgot.setOnClickListener(v -> {
        String email = emailLoginField.getText().toString().trim();

        if (email.isEmpty()) {
            emailLoginField.setError("Barua pepe inahitajika");
            emailLoginField.requestFocus();
            return;
        }

        // Send password reset email using FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Barua pepe ya kurekebisha neno siri imetumwa.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Kuna tatizo. Jaribu tena.", Toast.LENGTH_SHORT).show();
                }
            });
    });    
        
    // Set onClickListener for the cancel button
    cancelButton.setOnClickListener(v -> dialog.dismiss());

    // Show the dialog
    dialog.show();
}

    // User class to store user data
    public static class User {
        public String name;
        public String phone;
        public String email;
        public String country;
        public String countryCode;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String name, String phone, String email, String country, String countryCode) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.country = country;
            this.countryCode = countryCode;
        }
    }
}
