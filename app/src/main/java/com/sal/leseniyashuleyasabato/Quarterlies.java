package com.sal.leseniyashuleyasabato;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sal.leseniyashuleyasabato.MainActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Quarterlies extends AppCompatActivity {
    private Spinner yearSpinner;
    private LinearLayout quarterLayout;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<String> years = new ArrayList<>();

    private static final String PREFS_NAME = "QuarterDataPrefs";
    private static final String YEARS_KEY = "years";
    private static final String QUARTERS_MAP_KEY = "quarters_map";
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quarterlies);


        yearSpinner = findViewById(R.id.yearSpinner);
        quarterLayout = findViewById(R.id.quarterLayout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        fetchYears();
    }

    private void fetchYears() {
        if (!isOnline()) {
            loadYearsFromPrefs();
            return;
        }

        progressDialog.show();
        DocumentReference yearsRef = db.collection("All_Years").document("years_list");

        yearsRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> yearList = (List<String>) documentSnapshot.get("years");

                if (yearList != null && !yearList.isEmpty()) {
                    years.clear();
                    years.addAll(yearList);

                    saveYearsToPrefs(yearList); // Save years
                    updateYearSpinner();
                } else {
                    Toast.makeText(this, "No years found in Firestore.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Year list document does not exist.", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching years: " + e.getMessage(), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            loadYearsFromPrefs(); // Fallback if failed
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

        if (!isOnline()) {
            loadQuartersFromPrefs(year);
            progressDialog.dismiss();
            return;
        }

        DocumentReference yearDocRef = db.collection("All_Quarters").document(year);
        yearDocRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists() && document.contains("quarters")) {
                    List<String> quartersList = (List<String>) document.get("quarters");
                    saveQuartersToPrefs(year, quartersList); // Save to SharedPreferences
                    displayQuarterButtons(year, quartersList);
                } else {
                    Toast.makeText(this, "No quarters found for year " + year, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error fetching quarters: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayQuarterButtons(String year, List<String> quartersList) {
        quarterLayout.removeAllViews();

        for (String quarterName : quartersList) {
            Button quarterButton = new Button(this);
            quarterButton.setText(quarterName);
            quarterButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));
            quarterButton.setOnClickListener(v -> openMainActivity(year, quarterName));

            int marginInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            quarterLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            quarterButton.setLayoutParams(params);

            quarterLayout.addView(quarterButton);
        }
    }

    private void saveYearsToPrefs(List<String> yearList) {
        String json = gson.toJson(yearList);
        sharedPreferences.edit().putString(YEARS_KEY, json).apply();
    }

    private void loadYearsFromPrefs() {
        String json = sharedPreferences.getString(YEARS_KEY, null);
        if (json != null) {
            List<String> savedYears = gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
            if (savedYears != null && !savedYears.isEmpty()) {
                years.clear();
                years.addAll(savedYears);
                updateYearSpinner();
            }
        }
    }

    private void saveQuartersToPrefs(String year, List<String> quarters) {
        String existingMapJson = sharedPreferences.getString(QUARTERS_MAP_KEY, "{}");
        Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
        Map<String, List<String>> map = gson.fromJson(existingMapJson, type);
        map.put(year, quarters);
        sharedPreferences.edit().putString(QUARTERS_MAP_KEY, gson.toJson(map)).apply();
    }

    private void loadQuartersFromPrefs(String year) {
        String mapJson = sharedPreferences.getString(QUARTERS_MAP_KEY, null);
        if (mapJson != null) {
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> map = gson.fromJson(mapJson, type);
            if (map.containsKey(year)) {
                List<String> quartersList = map.get(year);
                displayQuarterButtons(year, quartersList);
            }
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void openMainActivity(String year, String quarter) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("YEAR", year);
        intent.putExtra("QUARTER", quarter);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
