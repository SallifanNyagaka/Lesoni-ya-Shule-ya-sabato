package com.sal.leseniyashuleyasabato;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sal.leseniyashuleyasabato.admin;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText adminEmailField, adminPasswordField;
    private Button adminLoginButton;
    private TextView adminForgotPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        editor = prefs.edit();

        // Initialize UI elements
        adminEmailField = findViewById(R.id.adminEmailField);
        adminPasswordField = findViewById(R.id.adminPasswordField);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        adminForgotPassword = findViewById(R.id.adminForgotPassword);

        // Handle Login Button Click
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = adminEmailField.getText().toString().trim();
                String password = adminPasswordField.getText().toString().trim();

                // Validate input
                if (TextUtils.isEmpty(email)) {
                    adminEmailField.setError("Email is required");
                    adminEmailField.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    adminPasswordField.setError("Password is required");
                    adminPasswordField.requestFocus();
                    return;
                }

                // Authenticate with FirebaseAuth
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(AdminLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    assert user != null;
                                    String userEmail = user.getEmail();

                                    // Check if the user is an admin
                                    checkIfAdmin(userEmail);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(AdminLoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Handle Forgot Password Click
        adminForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminLoginActivity.this, AdminForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkIfAdmin(String email) {
        db.collection("admins").document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                // User is an admin
                                Toast.makeText(AdminLoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();

                                // Save login status and email
                                editor.putString("adminEmail", email);
                                editor.putBoolean("isAdminLoggedIn", true);
                                editor.apply();

                                // Redirect to AdminSettingsActivity
                                Intent intent = new Intent(AdminLoginActivity.this, admin.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // User is not an admin
                                Toast.makeText(AdminLoginActivity.this, "Access Denied. Not an Admin.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Sign out the user
                            }
                        } else {
                            // Failed to check admin status
                            Toast.makeText(AdminLoginActivity.this, "Error checking admin status.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut(); // Sign out the user
                        }
                    }
                });
    }
}