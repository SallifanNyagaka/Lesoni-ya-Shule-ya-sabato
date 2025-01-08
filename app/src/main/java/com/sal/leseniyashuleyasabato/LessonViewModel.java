package com.sal.leseniyashuleyasabato;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sal.leseniyashuleyasabato.LessonModels;
import com.sal.leseniyashuleyasabato.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();







    private final MutableLiveData<List<String>> spinnerTitles = new MutableLiveData<>();
    private final MutableLiveData<List<LessonModels>> lessonDays = new MutableLiveData<>();
    private final Map<String, List<LessonModels>> cachedLessons = new HashMap<>(); // Cache

    public LiveData<List<String>> getSpinnerTitles() {
        return spinnerTitles;
    }

    public LiveData<List<LessonModels>> getLessonDays() {
        return lessonDays;
    }

    public void fetchSpinnerAndLessonData(int year, String quarter, String selectedTitle) {
        db.collection("quarters_" + year)
                .document(quarter)
                .collection("weeks")
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> titles = new ArrayList<>();
                    for (QueryDocumentSnapshot weekSnapshot : queryDocumentSnapshots) {
                        String weekTitle = weekSnapshot.getString("week_Title");
                        String weekDateRange = weekSnapshot.getString("weekDateRange");
                        if (weekTitle != null) {
                            titles.add(weekSnapshot.getId() + ":\n\n" + weekTitle + "\n" + weekDateRange);
                        }
                    }
                    spinnerTitles.postValue(titles);

                    // Fetch lesson data for the selected title
                    if (selectedTitle != null) {
                        String selectedWeekId = selectedTitle.split(":")[0];
                        if (cachedLessons.containsKey(selectedWeekId)) {
                            lessonDays.postValue(cachedLessons.get(selectedWeekId));
                        } else {
                            fetchLessonData(year, quarter, selectedWeekId);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LessonViewModel", "Failed to load spinner data: " + e.getMessage()));
    }


   /* public void fetchSpinnerAndLessonData(int year, String quarter, String selectedTitle) {

        db.collection("quarters_" + year)
                .document(quarter)
                .collection("weeks")
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> titles = new ArrayList<>();
                    for (QueryDocumentSnapshot weekSnapshot : queryDocumentSnapshots) {
                        String weekTitle = weekSnapshot.getString("week_Title");
                        String weekDateRange = weekSnapshot.getString("weekDateRange");
                        if (weekTitle != null) {
                            titles.add(weekSnapshot.getId() + ":\n\n" + weekTitle + "\n" + weekDateRange);
                        }
                    }
                    spinnerTitles.postValue(titles);

                    // Fetch lesson data for the selected title
                    if (selectedTitle != null) {
                        String selectedWeekId = selectedTitle.split(":")[0];
                        if (cachedLessons.containsKey(selectedWeekId)) {
                            lessonDays.postValue(cachedLessons.get(selectedWeekId));
                        } else {
                            fetchLessonData(year, quarter, selectedWeekId);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LessonViewModel", "Failed to load spinner data: " + e.getMessage()));
    }*/

    private void fetchLessonData(int year, String quarter, String weekId) {

        db.collection("quarters_" + year)
                .document(quarter)
                .collection("weeks")
                .document(weekId)
                .collection("days")
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(daysSnapshots -> {
                    List<LessonModels> lessonList = new ArrayList<>();
                    for (QueryDocumentSnapshot daySnapshot : daysSnapshots) {
                        String days = daySnapshot.getString("date");
                        String dayTitle = daySnapshot.getString("title");
                        String dayContent = daySnapshot.getString("content");
                        String dayQuestion = daySnapshot.getString("question");
                        String imageUri = daySnapshot.getString("image_url");
                        String dateEng = daySnapshot.getString("dateEng");
                        String weekRange = daySnapshot.getString("weekDateRange");

                        lessonList.add(new LessonModels(
                                days, dateEng, weekRange,
                                R.drawable.share_today, dayTitle,
                                dayContent, dayQuestion, imageUri));
                    }
                    cachedLessons.put(weekId, lessonList); // Cache the result
                    lessonDays.postValue(lessonList);
                })
                .addOnFailureListener(e -> Log.e("LessonViewModel", "Failed to load lessons: " + e.getMessage()));
    }
}

