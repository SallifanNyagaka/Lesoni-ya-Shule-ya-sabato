package com.sal.leseniyashuleyasabato;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.sal.leseniyashuleyasabato.utils.SharedPrefsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LessonRepository {
    Gson gson = new Gson();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SharedPrefsManager sharedPrefsManager;

    List<String> weekTitles = new ArrayList<>();
    String quarterId = "Q1";

    public LessonRepository(Context context) {
        sharedPrefsManager = new SharedPrefsManager(context);
    }

    // ✅ Fetch Data for a Year (First from SharedPrefs, then Firestore if missing)
    // No major changes needed except:
// In fetchYearData, remove confusing Map<String, Object> conversions.

    /*public void fetchYearData(String year, boolean forceOfflineRefresh, YearDataCallback callback) {
        if (!forceOfflineRefresh && sharedPrefsManager.isYearDataAvailable(year)) {
            Log.d("LessonRepository", "Loading data from SharedPreferences for year: " + year);
            callback.onYearDataLoaded(sharedPrefsManager.getAllLessonData()); // <-- No need to force complicated yearData
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference yearRef = db.collection("quarters_" + year);

        Map<String, Map<String, List<LessonModels>>> allQuarterData = new HashMap<>();

        yearRef.get().addOnSuccessListener(quarterSnapshots -> {
            List<Task<Void>> allTasks = new ArrayList<>();

            for (QueryDocumentSnapshot quarterDoc : quarterSnapshots) {
                String quarterId = quarterDoc.getId();
                this.quarterId = quarterId;
                CollectionReference weeksRef = quarterDoc.getReference().collection("weeks");
                Map<String, List<LessonModels>> weekDataMap = new HashMap<>();
                allQuarterData.put(quarterId, weekDataMap);

                Task<QuerySnapshot> weeksTask = weeksRef.get();
                Task<Void> processWeeksTask = weeksTask.continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot weeksSnapshot = task.getResult();
                        List<Task<Void>> daysTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot weekDoc : weeksSnapshot) {
                            String weekId = weekDoc.getId();
                            String weekTitle = weekDoc.getString("week_Title"); // Extract the week_Title field
                            String weekDateRange = weekDoc.getString("weekDateRange"); // Extract the week_DateRange field
                            String timestamp = weekDoc.getString("timeStamp"); // Extract the timestamp field

                            if (weekTitle != null) {
                                weekTitles.add(weekTitle); // Add the week title to the list
                            }


                            Task<QuerySnapshot> daysTask = weekDoc.getReference().collection("days").get();
                            Task<Void> processDaysTask = daysTask.continueWithTask(daysResultTask -> {
                                if (daysResultTask.isSuccessful()) {
                                    QuerySnapshot daysSnapshot = daysResultTask.getResult();
                                    List<LessonModels> daysList = new ArrayList<>();
                                    for (QueryDocumentSnapshot dayDoc : daysSnapshot) {
                                        LessonModels day = new LessonModels(
                                                dayDoc.getString("date"),
                                                dayDoc.getString("dateEng"),
                                                dayDoc.getString("weekDateRange"),
                                                R.drawable.share_today,
                                                dayDoc.getString("title"),
                                                dayDoc.getString("content"),
                                                dayDoc.getString("question"),
                                                dayDoc.getString("image_url")
                                        );
                                        daysList.add(day);
                                    }
                                    synchronized (weekDataMap) {
                                        weekDataMap.put(weekId, daysList);
                                    }
                                    sharedPrefsManager.saveDaysForWeek(year, quarterId, weekId, daysList);
                                } else {
                                    Log.e("LessonRepository", "Failed to fetch days for " + weekId, daysResultTask.getException());
                                }
                                return null;
                            });
                            daysTasks.add(processDaysTask);
                        }
                        return Tasks.whenAll(daysTasks);
                    } else {
                        Log.e("LessonRepository", "Failed to get weeks for quarter " + quarterId, task.getException());
                        return Tasks.forException(task.getException());
                    }
                });
                allTasks.add(processWeeksTask);
            }


            Tasks.whenAllComplete(allTasks)
                    .addOnSuccessListener(done -> {
                        callback.onYearDataLoaded(sharedPrefsManager.getAllLessonData());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LessonRepository", "Failed to fetch year data: " + e.getMessage());
                        callback.onYearDataLoaded(null);
                    });

        }).addOnFailureListener(e -> {
            Log.e("LessonRepository", "Failed to fetch year data: " + e.getMessage());
            callback.onYearDataLoaded(null);
        });

    }*/

    public void fetchYearData(String year, boolean refreshCacheFromFirestore, YearDataCallback callback) {
        Log.d("DEBUG", "fetchYearData called for year: " + year + ", forceOfflineRefresh: " + refreshCacheFromFirestore);

        if (!refreshCacheFromFirestore && sharedPrefsManager.isYearDataAvailable(year)) {
            Log.d("LessonRepository", "Loading data from SharedPreferences for year: " + year);
            callback.onYearDataLoaded(sharedPrefsManager.getLessonDataForYear(year)); // ✅ UPDATED
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference yearRef = db.collection("quarters_" + year);
        Log.d("DEBUG", "Firestore reference initialized for year: " + year);

        Map<String, Map<String, List<LessonModels>>> allQuarterData = new HashMap<>();
        Map<String, Map<String, String>> allWeekTitlesByQuarter = new HashMap<>(); // ✅ updated type

        yearRef.get().addOnSuccessListener(quarterSnapshots -> {
            Log.d("DEBUG", "Successfully fetched data for year: " + year + ". Processing quarters...");
            List<Task<Void>> allTasks = new ArrayList<>();

            for (QueryDocumentSnapshot quarterDoc : quarterSnapshots) {
                String quarterId = quarterDoc.getId();
                Log.d("DEBUG", "Processing quarter: " + quarterId);

                CollectionReference weeksRef = quarterDoc.getReference().collection("weeks");
                Map<String, List<LessonModels>> weekDataMap = new HashMap<>();
                allQuarterData.put(quarterId, weekDataMap);

                Map<String, String> weekTitlesForThisQuarter = new LinkedHashMap<>(); // ✅ map of weekTitle → weekDateRange
                allWeekTitlesByQuarter.put(quarterId, weekTitlesForThisQuarter);

                Task<QuerySnapshot> weeksTask = weeksRef.get();
                Task<Void> processWeeksTask = weeksTask.continueWithTask(weeksResultTask -> {
                    if (!weeksResultTask.isSuccessful()) {
                        Exception e = weeksResultTask.getException();
                        Log.e("LessonRepository", "Failed to get weeks for quarter " + quarterId, e);
                        return Tasks.forException(e);
                    }

                    List<Task<Void>> dayTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot weekDoc : weeksResultTask.getResult()) {
                        String weekId = weekDoc.getId();
                        Log.d("DEBUG", "Processing week: " + weekId);

                        String weekTitle = weekDoc.getString("week_Title");
                        String weekDateRange = weekDoc.getString("weekDateRange");

                        if (weekTitle != null && weekDateRange != null) {
                            weekTitlesForThisQuarter.put(weekTitle, weekDateRange); // ✅ store both
                            Log.d("DEBUG", "Adding Week Title (" + quarterId + "): " + weekTitle + " → " + weekDateRange);
                        } else {
                            Log.w("DEBUG", "Week title or range is null for week: " + weekId);
                        }

                        Task<QuerySnapshot> daysTask = weekDoc.getReference().collection("days").get();
                        Task<Void> processDaysTask = daysTask.continueWithTask(daysResultTask -> {
                            if (!daysResultTask.isSuccessful()) {
                                Exception e = daysResultTask.getException();
                                Log.e("LessonRepository", "Failed to fetch days for week " + weekId, e);
                                return Tasks.forException(e);
                            }

                            List<LessonModels> daysList = new ArrayList<>();
                            for (QueryDocumentSnapshot dayDoc : daysResultTask.getResult()) {
                                LessonModels day = new LessonModels(
                                        dayDoc.getString("date"),
                                        dayDoc.getString("dateEng"),
                                        dayDoc.getString("weekDateRange"),
                                        R.drawable.share_today,
                                        dayDoc.getString("title"),
                                        dayDoc.getString("content"),
                                        dayDoc.getString("question"),
                                        dayDoc.getString("image_url")
                                );
                                daysList.add(day);
                            }

                            // ✅ Sort daysList by dateEng before saving
                            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH);
                            Collections.sort(daysList, (d1, d2) -> {
                                try {
                                    Date date1 = sdf.parse(d1.getDateEng());
                                    Date date2 = sdf.parse(d2.getDateEng());
                                    return date1.compareTo(date2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });

                            synchronized (weekDataMap) {
                                weekDataMap.put(weekId, daysList);
                            }
                            sharedPrefsManager.saveDaysForWeek(year, quarterId, weekId, daysList);
                            Log.d("DEBUG", "Saved days for week: " + weekId);
                            return Tasks.forResult(null);
                        });

                        dayTasks.add(processDaysTask);
                    }

                    return Tasks.whenAll(dayTasks);
                });

                allTasks.add(processWeeksTask);
            }

            Tasks.whenAllComplete(allTasks)
                    .addOnSuccessListener(done -> {
                        Log.d("DEBUG", "All tasks completed successfully for year: " + year);

                        for (Map.Entry<String, Map<String, String>> entry : allWeekTitlesByQuarter.entrySet()) {
                            String quarterId = entry.getKey();
                            Map<String, String> titleRangeMap = entry.getValue();
                            sharedPrefsManager.saveWeekTitles(year, quarterId, titleRangeMap); // ✅ update this method in SharedPrefsManager
                            Log.d("DEBUG", "Saved week titles + ranges for " + quarterId + ": " + titleRangeMap);
                        }

                        sharedPrefsManager.saveYearData(year, allQuarterData);
                        callback.onYearDataLoaded(sharedPrefsManager.getLessonDataForYear(year)); // ✅ UPDATED
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LessonRepository", "Failed to complete one or more tasks for year: " + year, e);
                        callback.onYearDataLoaded(null);
                    });

        }).addOnFailureListener(e -> {
            Log.e("LessonRepository", "Failed to fetch quarters for year: " + year, e);
            callback.onYearDataLoaded(null);
        });
    }



    public void fetchQuarterData(String year, String quarter, QuarterDataCallback callback) {
        Map<String, Object> yearData = sharedPrefsManager.getLessonDataForYear(year);
        if (yearData != null && yearData.containsKey(quarter)) {
            @SuppressWarnings("unchecked")
            Map<String, List<LessonModels>> quarterData = (Map<String, List<LessonModels>>) yearData.get(quarter);
            callback.onQuarterDataLoaded(quarterData);
        } else {
            callback.onQuarterDataLoaded(null);
        }
    }





    // ✅ Fetch Data for a Week (Now from SharedPreferences)
    public void fetchWeekData(String year, String quarter, String weekId, WeekDataCallback callback) {
        fetchQuarterData(year, quarter, quarterData -> {
            if (quarterData != null && quarterData.containsKey(weekId)) {
                callback.onWeekDataLoaded((List<LessonModels>) quarterData.get(weekId));
            } else {
                callback.onWeekDataLoaded(new ArrayList<>());  // Empty if no data
            }
        });
    }

    // ✅ Callbacks for Async Data Retrieval
    public interface YearDataCallback {
        void onYearDataLoaded(Map<String, Object> yearData);
    }

    public interface QuarterDataCallback {
        void onQuarterDataLoaded(Map<String, List<LessonModels>> quarterData);
    }


    public interface WeekDataCallback {
        void onWeekDataLoaded(List<LessonModels> weekLessons);
    }
}
