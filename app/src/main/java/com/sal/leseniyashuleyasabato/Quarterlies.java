package com.sal.leseniyashuleyasabato;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sal.leseniyashuleyasabato.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Quarterlies extends AppCompatActivity {
    private Spinner yearSpinner;
    private LinearLayout quarterLayout;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<String> years = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quarterlies);


        yearSpinner = findViewById(R.id.yearSpinner);
        quarterLayout = findViewById(R.id.quarterLayout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        db = FirebaseFirestore.getInstance();

        fetchYears();
    }

    private void fetchYears() {
        progressDialog.show();

        DocumentReference yearsRef = db.collection("All_Years").document("years_list");

        yearsRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> yearList = (List<String>) documentSnapshot.get("years");

                if (yearList != null && !yearList.isEmpty()) {
                    years.clear();
                    years.addAll(yearList);
                    updateYearSpinner(); // Update the spinner after fetching
                } else {
                    Toast.makeText(Quarterlies.this, "No years found in Firestore.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Quarterlies.this, "Year list document does not exist.", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(Quarterlies.this, "Error fetching years: " + e.getMessage(), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        });
    }


    private void updateYearSpinner() {
        if (years.isEmpty()) {
            return; // Don't update spinner if no years are found
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);

        // Set listener for when an item is selected
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYear = parent.getItemAtPosition(position).toString().trim();
                fetchQuarters(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }



    private void fetchQuarters(String year) {
        progressDialog.show();

        Toast.makeText(Quarterlies.this, "Fetching quarters for year: " + year, Toast.LENGTH_SHORT).show();

        DocumentReference yearDocRef = db.collection("All_Quarters").document(year);

        yearDocRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists() && document.contains("quarters")) {
                    quarterLayout.removeAllViews(); // Clear previous views

                    List<String> quartersList = (List<String>) document.get("quarters");

                    Log.d("Firestore", "Quarters found: " + quartersList.size()); // Debugging log

                    if (quartersList.isEmpty()) {
                        Toast.makeText(this, "No quarters found for year " + year, Toast.LENGTH_SHORT).show();
                    } else {
                        for (String quarterName : quartersList) {
                            Log.d("Firestore", "Quarter found: " + quarterName);
                            Toast.makeText(this, "Found quarter: " + quarterName, Toast.LENGTH_SHORT).show();

                            Button quarterButton = new Button(this);
                            quarterButton.setText(quarterName);
                            quarterButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));
                            quarterButton.setOnClickListener(v -> openMainActivity(year, quarterName));
                            quarterLayout.addView(quarterButton);
                        }
                    }
                } else {
                    Toast.makeText(this, "No quarters found for year " + year, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("Firestore", "Error fetching quarters: ", task.getException());
                Toast.makeText(this, "Error fetching quarters: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    private void openMainActivity(String year, String quarter) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("YEAR", year);
        intent.putExtra("QUARTER", quarter);
        startActivity(intent);
    }
}
