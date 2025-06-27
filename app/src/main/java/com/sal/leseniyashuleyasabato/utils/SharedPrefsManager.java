package com.sal.leseniyashuleyasabato.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sal.leseniyashuleyasabato.LessonModels;

import java.lang.reflect.Type;
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

public class SharedPrefsManager {
    private static final String PREFS_NAME = "lesson_prefs";  // SharedPreferences file name
    private static final String KEY_LESSON_DATA = "lesson_data";  // Key for storing data

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // SAVE properly

    public void saveWeekTitles(String year, String quarter, Map<String, String> weekTitlesWithRanges) {
        String key = year + "_" + quarter + "_week_titles";
        String json = gson.toJson(weekTitlesWithRanges); // Serialize the map
        Log.d("SharedPrefsManager", "Saving week titles with ranges with key: " + key + ", JSON: " + json);
        sharedPreferences.edit().putString(key, json).apply();
    }
    public Map<String, String> getWeekTitles(String year, String quarter) {
        String key = year + "_" + quarter + "_week_titles";
        Log.d("SharedPrefsManager", "Retrieve Key: " + key);
        String weekTitlesJson = sharedPreferences.getString(key, null);
        Log.d("SharedPrefsManager", "Fetching week titles for year: " + year + ", quarter: " + quarter + ", JSON: " + weekTitlesJson);

        if (weekTitlesJson != null && !weekTitlesJson.isEmpty()) {
            try {
                Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType(); // maintain order
                return gson.fromJson(weekTitlesJson, type);
            } catch (JsonSyntaxException e) {
                Log.e("SharedPrefsManager", "Error parsing JSON for week titles with ranges", e);
            }
        }
        return new LinkedHashMap<>(); // Return an empty map if nothing found
    }


    public void saveYearData(String year, Map<String, Map<String, List<LessonModels>>> allQuarterData) {
        Map<String, Map<String, Map<String, List<LessonModels>>>> existingData = getAllLessonData();
        existingData.put(year, allQuarterData);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LESSON_DATA, gson.toJson(existingData));
        editor.apply();
    }



    public void saveDaysForWeek(String year, String quarter, String weekId, List<LessonModels> dayList) {
        String key = year + "_" + quarter + "_" + weekId + "_days";
        String json = gson.toJson(dayList);
        sharedPreferences.edit().putString(key, json).apply();
    }

    // GET properly (no confusing map structure)
    public List<LessonModels> getDaysForWeek(String year, String quarter, String weekId) {
        String key = year + "_" + quarter + "_" + weekId + "_days";
        String daysJson = sharedPreferences.getString(key, null);

        if (daysJson != null && !daysJson.isEmpty()) {
            try {
                Type type = new TypeToken<List<LessonModels>>() {}.getType();
                List<LessonModels> daysList = gson.fromJson(daysJson, type);

                // ✅ Sort the list by dateEng
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH);
                Collections.sort(daysList, (d1, d2) -> {
                    try {
                        Date date1 = sdf.parse(d1.getDateEng());
                        Date date2 = sdf.parse(d2.getDateEng());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        Log.e("SharedPrefsManager", "Date parse error: " + e.getMessage());
                        return 0;
                    }
                });

                return daysList;
            } catch (JsonSyntaxException e) {
                Log.e("SharedPrefsManager", "Error parsing JSON for key: " + key, e);
            }
        } else {
            Log.d("SharedPrefsManager", "No data found for key: " + key);
        }
        return null;
    }




    // ✅ Retrieve Data for a Specific Year
    // ✅ Clean and type-safe retrieval of data for a specific year
    public Map<String, Object> getLessonDataForYear(String year) {
        Map<String, ?> allPrefs = sharedPreferences.getAll();
        Map<String, Map<String, List<LessonModels>>> yearData = new HashMap<>();

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith(year + "_") && key.endsWith("_days")) {
                String[] parts = key.split("_");
                if (parts.length >= 3) {
                    String quarter = parts[1];
                    String weekId = parts[2];

                    List<LessonModels> lessons = getDaysForWeek(year, quarter, weekId);
                    if (lessons != null && !lessons.isEmpty()) {
                        Map<String, List<LessonModels>> quarterMap = yearData.getOrDefault(quarter, new HashMap<>());
                        quarterMap.put(weekId, lessons);
                        yearData.put(quarter, quarterMap);
                    }
                }
            }
        }

        // Convert to Map<String, Object> before returning
        Map<String, Object> safeCastMap = new HashMap<>();
        for (Map.Entry<String, Map<String, List<LessonModels>>> entry : yearData.entrySet()) {
            safeCastMap.put(entry.getKey(), entry.getValue());
        }

        return safeCastMap.isEmpty() ? null : safeCastMap;
    }





    // ✅ Get All Stored Data (All Years)
    public Map<String, Map<String, Map<String, List<LessonModels>>>> getAllLessonData() {
        String json = sharedPreferences.getString(KEY_LESSON_DATA, null);
        if (json == null) return new HashMap<>();

        Type type = new TypeToken<Map<String, Map<String, Map<String, List<LessonModels>>>>>() {}.getType();
        return gson.fromJson(json, type);
    }


    // ✅ Check if Data Exists for a Year
    public boolean isYearDataAvailable(String year) {
        return getLessonDataForYear(year) != null;
    }

    public boolean isWeekDataAvailable(String year, String quarterId, String weekId) {
        String key = year + "_" + quarterId + "_" + weekId;
        return sharedPreferences.contains(key);
    }


}
