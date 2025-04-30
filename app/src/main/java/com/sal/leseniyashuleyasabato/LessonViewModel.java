package com.sal.leseniyashuleyasabato;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LessonViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<String>> spinnerTitles = new MutableLiveData<>();
    private final MutableLiveData<List<LessonModels>> lessonDays = new MutableLiveData<>();

    public LiveData<List<String>> getSpinnerTitles() {
        return spinnerTitles;
    }

    public LiveData<List<LessonModels>> getLessonDays() {
        return lessonDays;
    }

    public void fetchSpinnerAndLessonData(String year, String quarter, String selectedTitle, Boolean cache) {
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
                        fetchLessonData(year, quarter, selectedWeekId);
                    }
                })
                .addOnFailureListener(e -> Log.e("LessonViewModel", "Failed to load spinner data: " + e.getMessage()));
    }

    private void fetchLessonData(String year, String quarter, String weekId) {
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

                    // ✅ Check if new data is different before posting it
                    if (!lessonList.equals(lessonDays.getValue())) {
                        lessonDays.postValue(lessonList);
                    }
                })
                .addOnFailureListener(e -> Log.e("LessonViewModel", "Failed to load lessons: " + e.getMessage()));
    }

}
