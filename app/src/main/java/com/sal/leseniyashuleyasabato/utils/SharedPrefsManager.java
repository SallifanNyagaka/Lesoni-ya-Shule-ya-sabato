package com.sal.leseniyashuleyasabato.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sal.leseniyashuleyasabato.LessonModels;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    public void saveWeekTitles(String year, String quarter, List<String> weekTitles) {
        String key = year + "_" + quarter + "_week_titles";
        String json = gson.toJson(weekTitles); // Serialize the list of titles
        Log.d("SharedPrefsManager", "Saving week titles with key: " + key + ", JSON: " + json);
        sharedPreferences.edit().putString(key, json).apply();
    }

    public List<String> getWeekTitles(String year, String quarter) {
        String key = year + "_" + quarter + "_week_titles";
        Log.d("SharedPrefsManager", "Retrieve Key: " + key);
        String weekTitlesJson = sharedPreferences.getString(key, null);
        Log.d("SharedPrefsManager", "Fetching week titles for year: " + year + ", quarter: " + quarter + ", JSON: " + weekTitlesJson);

        if (weekTitlesJson != null && !weekTitlesJson.isEmpty()) {
            try {
                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                return gson.fromJson(weekTitlesJson, type); // Deserialize the JSON into a List
            } catch (JsonSyntaxException e) {
                Log.e("SharedPrefsManager", "Error parsing JSON for week titles", e);
            }
        }
        return new ArrayList<>(); // Return an empty list if no data is found
    }

    public void saveAllLessonData(Map<String, Map<String, List<LessonModels>>> allQuarterData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Map.Entry<String, Map<String, List<LessonModels>>> quarterEntry : allQuarterData.entrySet()) {
            String quarterId = quarterEntry.getKey();
            Map<String, List<LessonModels>> weekData = quarterEntry.getValue();

            for (Map.Entry<String, List<LessonModels>> weekEntry : weekData.entrySet()) {
                String weekId = weekEntry.getKey();
                List<LessonModels> daysList = weekEntry.getValue();

                String key = quarterId + "_" + weekId;
                String json = new Gson().toJson(daysList);
                editor.putString(key, json);
            }
        }

        editor.apply(); // Save all at once
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
        Log.d("SharedPrefsManager", "Fetching data for year: " + year + ", raw JSON: " + daysJson); // <-- ADD THIS


        if (daysJson != null && !daysJson.isEmpty()) {
            try {
                Type type = new TypeToken<ArrayList<LessonModels>>() {}.getType();
                return gson.fromJson(daysJson, type);
            } catch (JsonSyntaxException e) {
                Log.e("SharedPrefsManager", "Error parsing JSON for week " + weekId, e);
            }
        }
        return null;
    }



    // ✅ Save Entire Year's Data (Firestore Data -> SharedPreferences)
    public void saveLessonData(String year, Map<String, Object> lessonData) {
        Map<String, Object> existingData = getAllLessonData();
        existingData.put(year, lessonData);  // Store data under the year

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LESSON_DATA, gson.toJson(existingData));  // Convert to JSON and store
        editor.apply();
    }

    // ✅ Retrieve Data for a Specific Year
    public Map<String, Object> getLessonDataForYear(String year) {
        Map<String, Object> allData = getAllLessonData();
        if (allData.containsKey(year)) {
            return (Map<String, Object>) allData.get(year);
        }
        return null;
    }

    // ✅ Get All Stored Data (All Years)
    public Map<String, Object> getAllLessonData() {
        String jsonData = sharedPreferences.getString(KEY_LESSON_DATA, null);
        if (jsonData == null) {
            return new HashMap<>();  // Return empty if no data exists
        }
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(jsonData, type);
    }

    // ✅ Check if Data Exists for a Year
    public boolean isYearDataAvailable(String year) {
        return getLessonDataForYear(year) != null;
    }

    public boolean isWeekDataAvailable(String year, String quarterId, String weekId) {
        String key = year + "_" + quarterId + "_" + weekId;
        return sharedPreferences.contains(key);
    }

    public List<String> getWeekTitlesForQuarter(String year, String quarter) {
        Map<String, Object> allData = getAllLessonData();

        if (!allData.containsKey(year)) {
            Log.d("DEBUG", "No data found for year: " + year);
            return new ArrayList<>();
        }

        Object yearObj = allData.get(year);
        if (!(yearObj instanceof Map)) {
            Log.e("DEBUG", "Year data is not a Map");
            return new ArrayList<>();
        }

        Map<String, Object> yearData = (Map<String, Object>) yearObj;

        if (!yearData.containsKey(quarter)) {
            Log.d("DEBUG", "No quarter data found for quarter: " + quarter);
            return new ArrayList<>();
        }

        Object quarterObj = yearData.get(quarter);
        if (!(quarterObj instanceof Map)) {
            Log.e("DEBUG", "Quarter data is not a Map");
            return new ArrayList<>();
        }

        Map<String, Object> quarterData = (Map<String, Object>) quarterObj;

        List<String> weekTitles = new ArrayList<>();
        for (Map.Entry<String, Object> weekEntry : quarterData.entrySet()) {
            Object weekVal = weekEntry.getValue();
            if (weekVal instanceof Map) {
                Map<String, Object> weekMap = (Map<String, Object>) weekVal;
                String title = (String) weekMap.getOrDefault("weekTitle", "Week " + weekEntry.getKey());
                weekTitles.add(title);
            }
        }

        return weekTitles;
    }


}
