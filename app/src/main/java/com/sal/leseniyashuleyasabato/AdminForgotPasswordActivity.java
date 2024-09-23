package com.sal.leseniyashuleyasabato;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminForgotPasswordActivity extends AppCompatActivity {

    private EditText forgotPasswordEmailField, securityAnswerField;
    private Button submitSecurityAnswerButton;
    private TextView securityQuestionText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String securityQuestion;
    private String correctAnswer;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_forgot_password);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        editor = prefs.edit();

        // Initialize UI elements
        forgotPasswordEmailField = findViewById(R.id.forgotPasswordEmailField);
        securityAnswerField = findViewById(R.id.securityAnswerField);
        submitSecurityAnswerButton = findViewById(R.id.submitSecurityAnswerButton);
        securityQuestionText = findViewById(R.id.securityQuestionText);
        LinearLayout parentLayout = findViewById(R.id.parentLayout);

        // Initially hide security question and answer fields
        securityQuestionText.setVisibility(View.GONE);
        securityAnswerField.setVisibility(View.GONE);
        submitSecurityAnswerButton.setVisibility(View.GONE);
        
        
        // Assuming parentLayout is your root layout and forgotPasswordEmailField is your EditText
View parentsLayout = findViewById(R.id.parentLayout); // Replace with your parent layout ID

// Handle Email Input and Fetch Security Question
forgotPasswordEmailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) { // When user leaves the email field, fetch security question if email is valid
            String email = forgotPasswordEmailField.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                fetchSecurityQuestion(email);
            }
        }
    }
});

// Set an OnTouchListener on the parent layout to clear focus
parentsLayout.setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (forgotPasswordEmailField.isFocused()) {
                forgotPasswordEmailField.clearFocus();
                // Optionally hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(forgotPasswordEmailField.getWindowToken(), 0);
            }
        }
        return true; // Return true to consume the touch event
    }
});

        // Handle the submit security answer button click
        submitSecurityAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredAnswer = securityAnswerField.getText().toString().trim();

                if (TextUtils.isEmpty(enteredAnswer)) {
                    Toast.makeText(AdminForgotPasswordActivity.this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                } else {
                    verifySecurityAnswer(enteredAnswer);
                }
            }
        });
    }

    // Fetch the security question based on the provided email
    private void fetchSecurityQuestion(String email) {
        db.collection("admins").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    securityQuestion = document.getString("securityQuestion");
                    correctAnswer = document.getString("securityAnswer");

                    // Show the security question and enable answer field
                    securityQuestionText.setText(securityQuestion);
                    securityQuestionText.setVisibility(View.VISIBLE);
                    securityAnswerField.setVisibility(View.VISIBLE);
                    submitSecurityAnswerButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AdminForgotPasswordActivity.this, "No security question found for this email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Verify the entered security answer
    private void verifySecurityAnswer(String enteredAnswer) {
        if (enteredAnswer.equalsIgnoreCase(correctAnswer)) {
            // If the answer is correct, allow the admin to reset their password
            resetPassword();
        } else {
            Toast.makeText(AdminForgotPasswordActivity.this, "Incorrect answer. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Allow admin to reset the password
    private void resetPassword() {
        String email = forgotPasswordEmailField.getText().toString().trim();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminForgotPasswordActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    // Redirect to login page or close the activity
                    Intent intent = new Intent(AdminForgotPasswordActivity.this, AdminLoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminForgotPasswordActivity.this, "Error sending reset email. Try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}