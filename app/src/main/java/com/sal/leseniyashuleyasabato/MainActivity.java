package com.sal.leseniyashuleyasabato;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Source;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;
import com.sal.leseniyashuleyasabato.utils.SharedPrefsManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;


/* loaded from: classes3.dex */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                DatePickerDialog.OnDateSetListener,
                TextToSpeech.OnInitListener {
    String Q;
    Spinner QWeeks;
    RecyclerView day;
    String day_content;
    String day_question;
    String day_title;
    String days;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawer;
    Adapter lesson_adapter;
    List<LessonModels> lesson_days;
    Calendar currentCal = Calendar.getInstance();
    Integer month = currentCal.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based;
    String quarterCollectionPath;
    CollectionReference quarterWeekDays;
    String quarterWeekDocumentPath;
    int quarterWeeks;
    String quarterTitle = "none";
    String quarterIntroduction = "Inatafuta";
    String title;
    Integer today = currentCal.get(Calendar.DAY_OF_MONTH);
    TextView verses;
    DocumentReference weekT;
    TextView weekTitle;
    Button btnToggle;
    Integer weekday = currentCal.get(Calendar.DAY_OF_WEEK);
    RecyclerView.LayoutManager wk_day_manager;
    Integer year = currentCal.get(Calendar.YEAR);
    String imageUrl;
    ImageView quarter_image;
    private TextToSpeech textToSpeech;
    private boolean isExpanded = false;
    BroadcastReceiver networkReceiver;
    ArrayList<Integer> spin = new ArrayList<>();

    int currentPosition = 0; // Track current position globally

    LessonRepository repository;

    private String teacherPathName;  //"quarters_"+year.toString() +"/"+ Quarter() + "/" + "weeks" + "/" + "WK-"+spinPosition+ "/"+ "teacher";
    private String lessonPathName;  //"quarters_"+year.toString() +"/"+ Quarter() + "/" + "weeks" + "/" + "WK-"+spinPosition+"/"+ "days";      

    String choosenQuarter, choosenYear;
    Boolean clearCache;

    private List<String> weekTitles = new ArrayList<>();  // Holds week titles
    private LessonRepository lessonRepository;
    private SharedPrefsManager sharedPrefsManager;

    private Map<String, String> weekTitleToIdMap = new HashMap<>();
    private String todayEngDate = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(new Date());
    int scrollToIndexAfterLoad = -1;
    private boolean isInitialSpinnerLoad = true;
    private boolean isFirstLaunch = true;

    public String todayWeekId;


    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity,
              // androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the SharedPreferences to check registration status
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean("isRegistered", false);
        boolean isLoggedIn = prefs.getBoolean("isLogged", true);
        lesson_days = new ArrayList<>();

        // Check if user is registered
        if (!isRegistered) {
            // User is not registered, redirect to RegisterActivity
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish(); // Prevent going back to this activity
            return;
        }
        // User is registered, load the main content
        setContentView(R.layout.activity_main);
        LessonViewModel viewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        this.textToSpeech = new TextToSpeech(this, this);
        this.quarter_image = findViewById(R.id.quarter_image);
        this.verses = findViewById(R.id.verses);
        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        Intent choosen = getIntent();
        repository = new LessonRepository(this);
        choosenQuarter = Quarter();  // Default value, change as needed
        choosenYear = year.toString(); // Default current year
        clearCache = false;

        if (choosen.hasExtra("QUARTER") && choosen.hasExtra("YEAR")) {
            choosenQuarter = choosen.getStringExtra("QUARTER");
            choosenYear = choosen.getStringExtra("YEAR");
            clearCache = true;
        }

// Debugging
        Log.d("IntentDebug", "QUARTER: " + choosenQuarter);
        Log.d("IntentDebug", "YEAR: " + choosenYear);
        Log.d("IntentDebug", "Today's Date: " + todayEngDate);


        Toast.makeText(this, "Quarter: " + choosenQuarter + " Year: " + choosenYear, Toast.LENGTH_SHORT).show();


        try {

            final DocumentReference quarter =
                    db.collection("quarters_" + choosenYear).document(Quarter());

// First attempt to fetch from cache
            quarter.get(Source.CACHE)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            processDocument(task.getResult());
                        } else {
                            // Fallback to server if cache misses or an error occurs
                            quarter.get()
                                    .addOnCompleteListener(serverTask -> {
                                        if (serverTask.isSuccessful() && serverTask.getResult() != null) {
                                            processDocument(serverTask.getResult());
                                        } else {
                                            Toast.makeText(
                                                            getApplicationContext(),
                                                            "Failed to load document",
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                        }
                    });

            //With caching included


            /*final DocumentReference quarter =
                    db.collection("quarters_" + year.toString()).document(Quarter());
            quarter.get()
                    .addOnCompleteListener(
                            new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            quarterTitle = document.getString("QuarterTitle");
                                            quarterIntroduction =
                                                    document.getString("quarterIntroduction");
                                            imageUrl = document.getString("image_url");

                                            if (getSupportActionBar() != null) {
                                                getSupportActionBar().setTitle(quarterTitle);
                                            }

                                            if (!MainActivity.this.isDestroyed()
                                                    && imageUrl != null
                                                    && !imageUrl.isEmpty()) {
                                                Glide.with(MainActivity.this)
                                                        .load(imageUrl)
                                                        .into(quarter_image);
                                            } else {
                                                quarter_image.setImageResource(
                                                        R.drawable.lesson); // Or any default image
                                            }
                                            if (quarterIntroduction != null) {
                                                SpannableString highlightedText =
                                                        spannableBibleText(quarterIntroduction);
                                                verses.setText(highlightedText);
                                                verses.setTextSize(textsize);
                                                verses.setMovementMethod(
                                                        LinkMovementMethod.getInstance());
                                            } else {
                                                Toast.makeText(
                                                                getApplicationContext(),
                                                                "Quarter Intro not loaded",
                                                                Toast.LENGTH_LONG)
                                                        .show();
                                            }

                                            Toast.makeText(
                                                            getApplicationContext(),
                                                            quarterTitle,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        } else {
                                            Toast.makeText(
                                                            getApplicationContext(),
                                                            "No such Document",
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    } else {
                                        Toast.makeText(
                                                        getApplicationContext(),
                                                        "failed to load document",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            }); */

            weekTitle = findViewById(R.id.weekTitle);
            weekTitle.setText(quarterMonths(Quarter()));

            this.QWeeks = findViewById(R.id.quarterWeeks);
            String str = "quarters_" + choosenYear + "/" + choosenQuarter + "/weeks";
            this.quarterCollectionPath = str;
            this.quarterWeekDocumentPath = "/WK-1/days";
            this.weekT =
                    this.db
                            .collection("quarters_" + choosenYear)
                            .document(choosenQuarter)
                            .collection("weeks")
                            .document("WK-1");
            this.quarterWeekDays = this.db.collection("quarters" + choosenYear);
            this.drawer = findViewById(R.id.drawer_layout);
            btnToggle = findViewById(R.id.btnToggle);
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            ActionBarDrawerToggle toggle =
                    new ActionBarDrawerToggle(
                            this,
                            this.drawer,
                            toolbar,
                            R.string.navigation_drawer_open,
                            R.string.navigation_drawer_close);
            this.drawer.addDrawerListener(toggle);
            toggle.syncState();

            btnToggle.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isExpanded) {
                                verses.setMaxLines(3);
                                btnToggle.setText("Soma zaidi");
                            } else {
                                verses.setMaxLines(Integer.MAX_VALUE);
                                btnToggle.setText("Soma Kidogo");
                            }
                            isExpanded = !isExpanded;
                        }
                    });



            //With caching included




           int spinPos = currentPosition + 1;
           String teacherPathName = "quarters_" + choosenYear + "/" + choosenQuarter + "/weeks/WK-" + spinPos + "/teacher";
           String lessonPathName = "quarters_" + choosenYear + "/" + choosenQuarter + "/weeks/WK-" + spinPos + "/days";
           lesson_adapter = new Adapter(lesson_days, MainActivity.this, teacherPathName, lessonPathName);
           day = findViewById(R.id.wk_day);
           wk_day_manager = new LinearLayoutManager(getApplicationContext());
           day.setLayoutManager(wk_day_manager);
           day.setAdapter(lesson_adapter);






            if (savedInstanceState == null){
                // Observe Spinner Titles

                boolean isOnline = isNetworkAvailable(this);
                lessonRepository = new LessonRepository(this);
                sharedPrefsManager = new SharedPrefsManager(this);

// ✅ Show progress bar before loading starts

                ProgressBar dataProgressBar = findViewById(R.id.dataProgressBar);
                TextView progressText = findViewById(R.id.progressText);

                dataProgressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);

                lessonRepository.fetchYearData(choosenYear, isOnline, yearData -> {
                    Log.d("DEBUG", "fetchYearData completed. Waiting before loading spinner...");

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Log.d("DEBUG", "Loading spinner with fresh data from SharedPrefs...");

                        // ✅ Hide the progress bar after data is fetched and delay passed
                        dataProgressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);

                        loadSpinnerWithWeeks(); // Load weeks now
                    }, 1000); // 1 second delay
                });


                QWeeks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (isInitialSpinnerLoad) {
                            isInitialSpinnerLoad = false;

                            String todayEngDate = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(new Date());
                            todayWeekId = findWeekIdForToday(choosenYear, choosenQuarter, todayEngDate);

                            if (todayWeekId != null) {
                                Log.d("AutoWeek", "Scrolling to today's week: " + todayWeekId);
                                fetchDaysForWeek(choosenYear, choosenQuarter, todayWeekId);
                                setSpinnerToWeekId(todayWeekId);
                                return; // Don't continue with manual WK-x logic
                            }
                        }

                        // Manual selection fallback
                        String selectedWeekTitle = parent.getItemAtPosition(position).toString();
                        if (!selectedWeekTitle.equals("No data available")) {
                            String selectedWeekId = "WK-" + (position + 1);
                            fetchDaysForWeek(choosenYear, choosenQuarter, selectedWeekId);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });


                printLessonDataForYear(choosenYear);
                Log.d("DEBUG", "Week Titles Saved: " + new Gson().toJson(sharedPrefsManager.getWeekTitles(choosenYear, choosenQuarter)));


                //fetchLessonsWithoutLiveData(choosenYear, choosenQuarter, 1);


               /* viewModel.getSpinnerTitles().observe(this, titles -> {
                    if (titles != null && !titles.isEmpty()){
                        ArrayAdapter<String> weekTitles = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                titles);
                        QWeeks.setAdapter(weekTitles);
                        QWeeks.setSelection(currentPosition);
                    }

                });

                // Observe Lesson Days
                viewModel.getLessonDays().observe(this, lessonDays -> {
                    if (lessonDays != null && !lessonDays.isEmpty()) {
                        lesson_adapter.updateLessonDays(lessonDays);
                    }
                });*/

                // Spinner Selection



            }


            // Initial Data Fetch
           // viewModel.fetchSpinnerAndLessonData(choosenYear, choosenQuarter, null, clearCache);


            // FloatingActionButton listeners outside the spinner listener
FloatingActionButton scrollRightButton = findViewById(R.id.scrollRight);
FloatingActionButton scrollLeftButton = findViewById(R.id.scrollLeft);

scrollRightButton.setOnClickListener(v -> {
    if (currentPosition < QWeeks.getCount() - 1) { // Ensure we don't exceed bounds
        currentPosition++;
        QWeeks.setSelection(currentPosition); // Change spinner position
    }
});

scrollLeftButton.setOnClickListener(v -> {
    if (currentPosition > 0) { // Ensure we don't go below 0
        currentPosition--;
        QWeeks.setSelection(currentPosition); // Change spinner position
    }
});
            
// In your MainActivity
FloatingActionButton btnOpenFragment = findViewById(R.id.btnOpenFragment);
btnOpenFragment.setOnClickListener(v -> {
    String teacher_Titles = "quarters_"+choosenYear +"/"+ choosenQuarter + "/" + "weeks";
    Integer spinnerPosition = QWeeks.getSelectedItemPosition();
    Intent teacherComments = new Intent(MainActivity.this, TeacherCommentsActivity.class);
    teacherComments.putExtra("path_name", teacher_Titles);
    teacherComments.putExtra("wk_comment", spinnerPosition);
    startActivity(teacherComments);

    /* fragment data:
    Bundle args = new Bundle();
    args.putString("path_name", teacher_Titles);
    args.putInt("wk_comment", spinnerPosition);
    TeacherCommentsFragment fragment = new TeacherCommentsFragment();
    fragment.setArguments(args);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.fragment_container, fragment).addToBackStack(null) // Replace the existing fragment
               .commitAllowingStateLoss(); // Commit the transaction */
});
            
            
            
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            networkReceiver =
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            try {
                                if (lesson_adapter.isConnected()) {
                                    // Synchronize offline comments with Firestore when connectivity is
                                    // restored
                                    lesson_adapter
                                            .syncOfflineComments(); // Call adapter method to sync
                                    // comments
                                    lesson_adapter.syncOfflineHighlights(); // sync highlights
                                    lesson_adapter.syncRemovedHighlights(); // sync removed highlights
                                }
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                            }

                        }
                    };
            registerReceiver(networkReceiver, filter);

        } catch (Exception err) {
            Toast.makeText(
                            getApplicationContext(),
                            "onCreate() " + err.toString(),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void setSpinnerToWeekId(String weekId) {
        // Extract the index from weekId (e.g., WK-3 → 3 → index 2)
        int index = Integer.parseInt(weekId.replace("WK-", "")) - 1;

        // Ensure index is within bounds of the spinner adapter
        if (QWeeks.getAdapter() != null && index >= 0 && index < QWeeks.getAdapter().getCount()) {
            QWeeks.setSelection(index);
        } else {
            Log.w("Spinner", "Invalid index for weekId: " + weekId + " — Spinner count: " + QWeeks.getAdapter().getCount());
        }
    }


    //new changes
    private void fetchDaysForWeek(String year, String quarter, String selectedWeekId) {
        Log.d("FetchDays", "🚀 Fast load for Year: " + year + ", Quarter: " + quarter + ", WeekID: " + selectedWeekId);

        // ✅ Load SharedPrefs data immediately
        Map<String, Object> yearData = sharedPrefsManager.getLessonDataForYear(year);
        if (yearData == null) {
            Log.e("FetchDays", "⚠️ No year data in cache");
            return;
        }

        Map<String, List<LessonModels>> quarterData = (Map<String, List<LessonModels>>) yearData.get(quarter);
        if (quarterData == null) {
            Log.e("FetchDays", "⚠️ No quarter data in cache");
            return;
        }

        List<LessonModels> lessons = quarterData.get(selectedWeekId);
        if (lessons == null || lessons.isEmpty()) {
            Log.e("FetchDays", "⚠️ No lesson data for week: " + selectedWeekId);
            return;
        }

        // ✅ Lightning-fast local display
        lesson_days.clear();
        lesson_days.addAll(lessons);
        lesson_adapter.notifyDataSetChanged();

        Toast.makeText(this, "📦 Loaded from cache", Toast.LENGTH_SHORT).show();

        // 🔄 Scroll to today
        String todayFormatted = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(new Date());
        int index = IntStream.range(0, lessons.size())
                .filter(i -> todayFormatted.equals(lessons.get(i).getDateEng()))
                .findFirst().orElse(-1);

        if (index != -1) {
            day.post(() -> day.scrollToPosition(index));
        }

        // 🌐 Background refresh (cache only, no UI change)
        if (isNetworkAvailable(this)) {
            Log.d("FetchDays", "🌍 Updating cache in background from Firestore...");
        }
    }




    private String findWeekIdForToday(String year, String quarter, String todayEngDate) {
        Map<String, Object> yearData = sharedPrefsManager.getLessonDataForYear(year);
        if (yearData == null) return null;

        Map<String, List<LessonModels>> quarterData = (Map<String, List<LessonModels>>) yearData.get(quarter);
        if (quarterData == null) return null;

        for (Map.Entry<String, List<LessonModels>> entry : quarterData.entrySet()) {
            String weekId = entry.getKey();
            List<LessonModels> lessons = entry.getValue();

            for (int i = 0; i < lessons.size(); i++) {
                LessonModels lesson = lessons.get(i);
                if (todayEngDate.equals(lesson.getDateEng())) {
                    // Store index in a variable for scrolling later
                    scrollToIndexAfterLoad = i; // make this a global variable
                    return weekId;
                }
            }
        }

        return null; // Not found
    }




    private void printLessonDataForYear(String year) {
        Map<String, Object> yearData = sharedPrefsManager.getLessonDataForYear(year);

        if (yearData == null) {
            Log.e("LessonDataPrinter", "No data found for year: " + year);
            return;
        }

        for (Map.Entry<String, Object> quarterEntry : yearData.entrySet()) {
            String quarter = quarterEntry.getKey();
            Log.d("LessonDataPrinter", "📘 Quarter: " + quarter);

            Map<String, Object> quarterData = (Map<String, Object>) quarterEntry.getValue();
            if (quarterData != null) {
                for (Map.Entry<String, Object> weekEntry : quarterData.entrySet()) {
                    String weekId = weekEntry.getKey();
                    Log.d("LessonDataPrinter", "  📗 Week ID: " + weekId);

                    List<LessonModels> lessons = (List<LessonModels>) weekEntry.getValue();
                    if (lessons != null && !lessons.isEmpty()) {
                        for (LessonModels lesson : lessons) {
                            Log.d("LessonDataPrinter", "    📅 Date: " + lesson.getDate());
                            Log.d("LessonDataPrinter", "    🗓️ DateEng: " + lesson.getDateEng());
                            Log.d("LessonDataPrinter", "    📆 Week Range: " + lesson.getWeekRange());
                            Log.d("LessonDataPrinter", "    📝 Title: " + lesson.getDay_title());
                            Log.d("LessonDataPrinter", "    📖 Content: " + lesson.getDay_content());
                            Log.d("LessonDataPrinter", "    ❓ Question: " + lesson.getDay_question());
                            Log.d("LessonDataPrinter", "    🖼️ Image URL: " + lesson.getSaturday_image_uri());
                            Log.d("LessonDataPrinter", "    ----------------------------------------");
                        }
                    } else {
                        Log.d("LessonDataPrinter", "    🚫 No lessons found for this week.");
                    }
                }
            } else {
                Log.e("LessonDataPrinter", "  ⚠️ Quarter data is null for quarter: " + quarter);
            }
        }
    }


    private void loadSpinnerWithWeeks() {
        Map<String, String> weekTitleToDateRangeMap = sharedPrefsManager.getWeekTitles(choosenYear, choosenQuarter);

        // Map to hold title → date
        List<Map.Entry<String, String>> entries = new ArrayList<>(weekTitleToDateRangeMap.entrySet());

        // Swahili to English month mapping
        Map<String, String> swahiliToEnglishMonths = new HashMap<>();
        swahiliToEnglishMonths.put("Januari", "January");
        swahiliToEnglishMonths.put("Februari", "February");
        swahiliToEnglishMonths.put("Machi", "March");
        swahiliToEnglishMonths.put("Aprili", "April");
        swahiliToEnglishMonths.put("Mei", "May");
        swahiliToEnglishMonths.put("Juni", "June");
        swahiliToEnglishMonths.put("Julai", "July");
        swahiliToEnglishMonths.put("Agosti", "August");
        swahiliToEnglishMonths.put("Septemba", "September");
        swahiliToEnglishMonths.put("Oktoba", "October");
        swahiliToEnglishMonths.put("Novemba", "November");
        swahiliToEnglishMonths.put("Desemba", "December");

        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

        // Sort entries by parsed start date
        entries.sort((e1, e2) -> {
            try {
                String range1 = e1.getValue();
                String range2 = e2.getValue();

                Date date1 = parseSwahiliStartDate(range1, swahiliToEnglishMonths);
                Date date2 = parseSwahiliStartDate(range2, swahiliToEnglishMonths);

                return date1.compareTo(date2);
            } catch (Exception e) {
                return 0;
            }
        });

        List<String> displayTitles = new ArrayList<>();
        Map<String, String> sortedTitleToDateRangeMap = new LinkedHashMap<>();

        int index = 1;
        for (Map.Entry<String, String> entry : entries) {
            String title = entry.getKey();
            String dateRange = entry.getValue();

            String displayText = "WK_" + index + ": " + title + " -> " + dateRange;
            displayTitles.add(displayText);

            sortedTitleToDateRangeMap.put(title, dateRange); // In case needed later
            index++;
        }

        if (displayTitles.isEmpty()) {
            displayTitles.add("No data available");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayTitles);
        QWeeks.setAdapter(adapter);
    }

    // Helper to parse swahili range like "Juni 21 - 27 ,2025" or "Juni 28 - Julai 4"
    private Date parseSwahiliStartDate(String range, Map<String, String> monthMap) throws ParseException {
        String startPart = range.split("-")[0].trim(); // "Juni 21"
        String month = startPart.split(" ")[0];        // "Juni"
        String day = startPart.split(" ")[1];          // "21"

        String englishMonth = monthMap.getOrDefault(month, month);
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)); // fallback

        if (range.contains(",")) {
            currentYear = range.split(",")[1].trim(); // "2025"
        }

        String finalDate = englishMonth + " " + day + ", " + currentYear;
        return new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(finalDate);
    }



    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    private void processDocument(DocumentSnapshot document) {
        if (document.exists()) {
            String quarterTitle = document.getString("QuarterTitle");
            String quarterIntroduction = document.getString("quarterIntroduction");
            String imageUrl = document.getString("image_url");

            // Update ActionBar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(quarterTitle);
            }

            // Load the image
            if (!isDestroyed() && imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .into(quarter_image);
            } else {
                quarter_image.setImageResource(R.drawable.lesson); // Default image
            }

            // Update the TextView with spannable text
            if (quarterIntroduction != null) {
                SpannableString highlightedText = spannableBibleText(quarterIntroduction);
                verses.setText(highlightedText);
                verses.setTextSize(getTextSize());
                verses.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                Toast.makeText(
                                getApplicationContext(),
                                "Quarter Intro not loaded",
                                Toast.LENGTH_LONG)
                        .show();
            }

            Toast.makeText(
                            getApplicationContext(),
                            quarterTitle,
                            Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(
                            getApplicationContext(),
                            "No such Document",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private int getTextSize(){
        SharedPreferences textSize = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return textSize.getInt("textSize", 16);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity,
              // android.app.Activity
    public void onStart() {
        super.onStart();

        super.onStart();

        if (isFirstLaunch) {
            // ✅ Only refresh once, after a true cold start
            fetchDaysForWeek(choosenYear, choosenQuarter, todayWeekId);
            isFirstLaunch = false;
        } else {
            Log.d("MainActivity", "🔄 App resumed from background — no refresh");
        }

        this.weekT.addSnapshotListener(
                this,
                new EventListener<
                        DocumentSnapshot>() { // from class:
                                              // com.sal.leseniyashuleyasabato.MainActivity.1
                    static final /* synthetic */ boolean $assertionsDisabled = false;

                    @Override // com.google.firebase.firestore.EventListener
                    public void onEvent(DocumentSnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(
                                            MainActivity.this.getApplicationContext(),
                                            "Error while loading",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                        if (value == null) {
                            throw new AssertionError();
                        }
                        if (value.exists()) {
                            MainActivity.this.title = value.getString("title");
                        }
                    }
                });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("sw"));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Swahili language is not supported!");
                Toast.makeText(
                                getApplicationContext(),
                                "Samahani Lugha ya Kiswahili haikubaliki kwa mtambo huu",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Log.e("TTS", "Initialization failed!");
            Toast.makeText(
                            getApplicationContext(),
                            "Imeshindwa Kuanzisha msomaji",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        // Unregister the receiver to prevent memory leaks
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }

        super.onDestroy();
        isFirstLaunch = true;
    }

    public String Quarter() {
        String[] Qs = {"Q1", "Q2", "Q3", "Q4"};
        if (this.month != 0 && this.month < 4) {
            this.Q = Qs[0];
        } else if (this.month >= 4 && this.month < 7) {
            this.Q = Qs[1];
        } else if (this.month >= 7 && this.month < 10) {
            this.Q = Qs[2];
        } else if (this.month >= 10 && this.month <= 12) {
            this.Q = Qs[3];
        }
        return this.Q;
    }

    public String quarterMonths(String quarter) {
        String months = "Lesoni ya shule ya Sabato";
        if (quarter == "Q1") {
            months = "Januari, Februari, Machi";
        } else if (quarter == "Q2") {
            months = "Aprili, Mei, Juni";
        } else if (quarter == "Q3") {
            months = "Julai, Agosti, Septemba";
        } else if (quarter == "Q4") {
            months = "Oktoba, Novemba, Desemba";
        }

        return months;
    }

    @Override // com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.admin_section) {
            Intent intent = new Intent(getApplicationContext(), AdminLoginActivity.class);
            startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.quarterlies) {
            Intent intent = new Intent(getApplicationContext(), Quarterlies.class);
            startActivity(intent);
            return true;
        }

        return true;
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        if (this.drawer.isDrawerOpen(GravityCompat.START)) {
            this.drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initDays() {

        
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.calender) {
            DialogFragment pickDate = new DatePickerFragment();
            pickDate.show(getSupportFragmentManager(), "date picker");
            Toast.makeText(MainActivity.this, "calender selected", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.bible) {
            Intent bible = new Intent(MainActivity.this, com.sal.leseniyashuleyasabato.bible.class);
            bible.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(bible);
            return true;
        }
        if (item.getItemId() == R.id.login_register) {
            Intent reg = new Intent(MainActivity.this, RegisterActivity.class);
            reg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(reg);
            return true;
        }

        if (item.getItemId() == R.id.settings) {
            openSettingsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tools, menu);
        return true;
    }

    @Override // android.app.DatePickerDialog.OnDateSetListener
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Calendar c = Calendar.getInstance();
        c.set(1, i);
        c.set(2, i1);
        c.set(5, i2);
        this.year = i;
        this.month = i1 + 1;
        this.today = i2;
    }

    public ArrayList<Integer> bible_Chapters(String fileName, int chaptr) {

        ArrayList<Integer> chapters = new ArrayList<>();
        Integer prevline = 0;
        ArrayList<BibleMemory> chapterNlineNo = new ArrayList<>();
        ArrayList<Integer> preVerse = new ArrayList<>();

        try {
            InputStream stream = getAssets().open(fileName);
            BufferedReader read = new BufferedReader(new InputStreamReader(stream));
            StringBuffer result = new StringBuffer();
            String buffer;
            chapterNlineNo = new ArrayList<>();
            Integer Chapt = 0;
            Integer ChaptLine = 0;

            while ((buffer = read.readLine()) != null) {
                result.append(buffer).append("\n");
                ChaptLine += 1;
                prevline = ChaptLine;

                if (!buffer.startsWith(" ")) {
                    Chapt += 1;
                    chapterNlineNo.add(new BibleMemory(Chapt, ChaptLine));
                }
            }

        } catch (Exception err) {
            Toast.makeText(
                            getApplicationContext(),
                            "Kifungu hakikupatikana.\n Inafungua Biblia...",
                            Toast.LENGTH_LONG)
                    .show();
            cannotOpen();
        }
        try {
            for (BibleMemory memory : chapterNlineNo) {
                int chapter = memory.getLineIncrement();
                Integer chapterNo = memory.getChapterLine();
                if (chapterNo > 1) {
                    preVerse.add(chapterNo);
                }

                if (chapter == chaptr) {
                    chapters.add(chapterNo);
                }
            }
            preVerse.add(prevline + 1);
            int stop = preVerse.get(chaptr - 1);
            chapters.add(stop);

        } catch (IndexOutOfBoundsException err) {
            Toast.makeText(
                            getApplicationContext(),
                            "Samahani Kifungu Hiki Hakikupatikana Katika Biblia. Inafungua Biblia...",
                            Toast.LENGTH_LONG)
                    .show();
            cannotOpen();
        }

        return chapters;
    }

    // Define a regex pattern to match all Swahili book names and verse numbers
   
    public SpannableString spannableBibleText(String mafungu) {
    // Start with the original text
    SpannableString spannableString = new SpannableString(mafungu);

    // Define the regex for verses
    Pattern versePattern =
            Pattern.compile(
                    "(Mwanzo|Kutoka|Walawi|Hesabu|Kumbukumbu la Torati|Yoshua|Waamuzi|Ruthu|1 Samweli|2 Samweli|1 Wafalme|2 Wafalme|1 Mambo ya Nyakati|2 Mambo ya Nyakati|Ezra|Nehemia|Esta|Ayubu|Zaburi|Mithali|Mhubiri|Wimbo Ulio Bora|Isaya|Yeremia|Maombolezo|Ezekieli|Danieli|Hosea|Yoeli|Amosi|Obadia|Yona|Mika|Nahumu|Habakuki|Sefania|Hagai|Zekaria|Malaki|Mathayo|Marko|Luka|Yohana|Matendo ya Mitume|Warumi|1 Wakorintho|2 Wakorintho|Wagalatia|Waefeso|Wafilipi|Wakolosai|1 Wathesalonike|2 Wathesalonike|1 Timotheo|2 Timotheo|Tito|Filemoni|Waebrania|Yakobo|1 Petro|2 Petro|1 Yohana|2 Yohana|3 Yohana|Yuda|Ufunuo wa Yohana)\\s\\d+(:\\d+(-\\d+)?(,\\s*\\d+(:\\d+(-\\d+)?)*|,\\s*\\d+)*(\\s*,\\s*\\d+(:\\d+(-\\d+)?)*?)?)?\\s*;");

    // Regex for bold text surrounded by asterisks (*text*)
    Pattern boldPattern = Pattern.compile("\\*(.*?)\\*");

    // Step 1: Handle bold text
    Matcher boldMatcher = boldPattern.matcher(mafungu);
    StringBuilder modifiedText = new StringBuilder(mafungu);
    int adjustment = 0;

    while (boldMatcher.find()) {
        int start = boldMatcher.start() - adjustment;
        int end = boldMatcher.end() - adjustment;
        String boldText = boldMatcher.group(1); // Extract text between *

        // Replace *text* with text
        modifiedText.replace(start, end, boldText);
        adjustment += 2; // Each * removed changes the length by 2
    }

    SpannableString finalSpannable = new SpannableString(modifiedText.toString());

    // Apply bold spans
    boldMatcher = boldPattern.matcher(mafungu); // Recreate matcher for applying spans
    adjustment = 0;

    while (boldMatcher.find()) {
        int start = boldMatcher.start() - adjustment;
        int end = start + boldMatcher.group(1).length();

        finalSpannable.setSpan(
                new StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        adjustment += 2; // Account for removed asterisks
    }

    // Step 2: Handle Bible verse spans
    Matcher verseMatcher = versePattern.matcher(modifiedText);
    while (verseMatcher.find()) {
        int start = verseMatcher.start();
        int end = verseMatcher.end();

        // Apply color, underline, and clickable spans
        finalSpannable.setSpan(
                new ForegroundColorSpan(Color.BLUE),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        finalSpannable.setSpan(
                new UnderlineSpan(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        finalSpannable.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        String verse = modifiedText.substring(start, end);
                        Toast.makeText(MainActivity.this, verse, Toast.LENGTH_SHORT).show();
                        handleVerseClick(verse, start, end);
                    }
                },
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    return finalSpannable;
}
    
    /* public SpannableString spannableBibleText(String mafungu) {
        SpannableString spannableString = new SpannableString(mafungu);

        // Define the regex pattern to match simple verse structures
        Pattern versePattern =
                Pattern.compile(
                        "(Mwanzo|Kutoka|Walawi|Hesabu|Kumbukumbu la Torati|Yoshua|Waamuzi|Ruthu|1 Samweli|2 Samweli|1 Wafalme|2 Wafalme|1 Mambo ya Nyakati|2 Mambo ya Nyakati|Ezra|Nehemia|Esta|Ayubu|Zaburi|Mithali|Mhubiri|Wimbo Ulio Bora|Isaya|Yeremia|Maombolezo|Ezekieli|Danieli|Hosea|Yoeli|Amosi|Obadia|Yona|Mika|Nahumu|Habakuki|Sefania|Hagai|Zekaria|Malaki|Mathayo|Marko|Luka|Yohana|Matendo ya Mitume|Warumi|1 Wakorintho|2 Wakorintho|Wagalatia|Waefeso|Wafilipi|Wakolosai|1 Wathesalonike|2 Wathesalonike|1 Timotheo|2 Timotheo|Tito|Filemoni|Waebrania|Yakobo|1 Petro|2 Petro|1 Yohana|2 Yohana|3 Yohana|Yuda|Ufunuo wa Yohana)\\s\\d+(:\\d+(-\\d+)?(,\\s*\\d+(:\\d+(-\\d+)?)*|,\\s*\\d+)*(\\s*,\\s*\\d+(:\\d+(-\\d+)?)*?)?)?\\s*;");

        Matcher matcher = versePattern.matcher(mafungu);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String compVerse = mafungu.substring(start, end);

            // Split the verse into segments based on commas and semicolons
            String[] verseSegments = compVerse.split(";");

            // Iterate over each segment to apply spans
            for (String segment : verseSegments) {
                segment = segment.trim();
                if (!segment.isEmpty()) {
                    String[] parts = segment.split(",\\s*");

                    for (String part : parts) {
                        part = part.trim();
                        int partStart = mafungu.indexOf(part, start);
                        int partEnd = partStart + part.length();
                        String fullVerse = segment.trim();

                        // Apply color and underline spans
                        spannableString.setSpan(
                                new ForegroundColorSpan(Color.BLUE),
                                partStart,
                                partEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(
                                new UnderlineSpan(),
                                partStart,
                                partEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Apply clickable span
                        final String verse =
                                fullVerse; // Make the verse effectively final for the inner class
                        spannableString.setSpan(
                                new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Toast.makeText(
                                                        getApplicationContext(),
                                                        verse,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                        handleVerseClick(verse, partStart, partEnd);
                                    }
                                },
                                partStart,
                                partEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        return spannableString;
    }*/

    private void handleVerseClick(String compVerse, int startIndex, int stopIndex) {
        // Define default values
        String book = "books/mwanzo.txt";
        Integer chapter = 1;
        Integer verse = 1;
        Integer to = 1;

        // Remove duplicated book names
        Pattern duplicatePattern = Pattern.compile("^(\\w+)(\\s\\1)+");
        Matcher duplicateMatcher = duplicatePattern.matcher(compVerse);
        if (duplicateMatcher.find()) {
            compVerse = duplicateMatcher.replaceAll("$1");
        }

        // Trim spaces
        compVerse = compVerse.trim();

        // Split the input into segments based on commas
        String[] segments = compVerse.split("\\s*,\\s*");

        // Attach ":1" to segments without a verse
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i].trim();
            if (!segment.contains(":") && segment.matches(".*\\d+")) {
                segments[i] = segment + ":1";
            }
        }

        // Join the segments back into the compVerse string
        compVerse = String.join(", ", segments);

        // Initialize regex pattern for verse segments
        Pattern pattern = Pattern.compile("^(.*?\\d*\\s*\\w+)(\\s(\\d+):(\\d+)(?:-(\\d+))?)?$");

        try {
            for (String segment : segments) {
                Matcher matcher = pattern.matcher(segment.trim());

                if (matcher.find()) {
                    String currentBook = matcher.group(1).toLowerCase().trim();
                    Integer currentChapter =
                            matcher.group(3) != null
                                    ? Integer.parseInt(matcher.group(3).trim())
                                    : chapter;
                    Integer currentVerse =
                            matcher.group(4) != null
                                    ? Integer.parseInt(matcher.group(4).trim())
                                    : 1;
                    Integer currentTo =
                            matcher.group(5) != null
                                    ? Integer.parseInt(matcher.group(5).trim())
                                    : currentVerse;

                    // Check for comma-separated verses
                    if (segment.contains(",")) {
                        String[] parts = segment.split(",");
                        for (String part : parts) {
                            Matcher partMatcher = pattern.matcher(part.trim());
                            if (partMatcher.find()) {
                                Integer partTo =
                                        partMatcher.group(5) != null
                                                ? Integer.parseInt(partMatcher.group(5).trim())
                                                : null;
                                if (partTo != null) {
                                    currentTo =
                                            Math.max(
                                                    currentTo,
                                                    partTo); // Update currentTo with the maximum
                                                             // value
                                }
                            }
                        }
                    }

                    // Update chapter, verse, and to with the current values
                    chapter = currentChapter;
                    verse = currentVerse;
                    to = currentTo;

                    // Handle the case where the book name ends with a number but isn't supposed to
                    if (!currentBook.isEmpty()
                            && currentBook.matches(".*\\d$")
                            && !currentBook.matches("^\\d.*")) {
                        currentBook = currentBook.replaceAll("\\d+$", "").trim();
                    }

                    // Update the book if it's still the default
                    if (book.equals("books/mwanzo.txt") && !currentBook.isEmpty()) {
                        book = "books/" + currentBook + ".txt";
                    }

                    // Construct the full verse reference
                    String fullVerse = segment.trim();
                    if (!fullVerse.startsWith(currentBook)) {
                        fullVerse = currentBook + " " + fullVerse;
                    }

                    // Display the extracted information for debugging
                    Toast.makeText(
                                    getApplicationContext(),
                                    "Book: "
                                            + book
                                            + "\nChapter: "
                                            + chapter
                                            + "\nVerse: "
                                            + verse
                                            + "\nTo: "
                                            + to,
                                    Toast.LENGTH_LONG)
                            .show();

                    // Handle each segment (e.g., create spans, store ranges, etc.)
                    // You can add your logic here to process each chapter and verse
                } else {
                    // Handle the case where the format does not match
                    Toast.makeText(
                                    getApplicationContext(),
                                    "Error parsing verse: " + segment,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(
                            getApplicationContext(),
                            "Error parsing verse: " + compVerse,
                            Toast.LENGTH_LONG)
                    .show();
        }

        // Start the Bible_Read activity with the extracted information
        Intent bible = new Intent(getApplicationContext(), Bible_Read.class);
        ArrayList<Integer> StartStop = bible_Chapters(book, chapter);
        bible.putExtra("chapter", book + " " + chapter);
        bible.putExtra("startVerse", StartStop.get(0));
        bible.putExtra("stopVerse", StartStop.get(1));
        bible.putExtra("bookName", book);
        startActivity(bible);
    }

    private void cannotOpen() {
        Intent intent = new Intent(getApplicationContext(), bible.class);
        startActivity(intent);
    }


    //settings section:

    private void openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create dynamic settings layout
        View dialogView = createDynamicSettingsView();
        builder.setView(dialogView);

        builder.setTitle("Settings");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private View createDynamicSettingsView() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);

        // Reference UI elements
        SeekBar textSizeSeekBar = dialogView.findViewById(R.id.textSizeSeekBar);
        TextView textSizePreview = dialogView.findViewById(R.id.textSizePreview);
        Switch themeSwitch = dialogView.findViewById(R.id.themeSwitch);
        TextView resetDefaults = dialogView.findViewById(R.id.resetDefaults);

        SharedPreferences preferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Text size setting
        int savedTextSize = preferences.getInt("textSize", 16);
        textSizeSeekBar.setProgress(savedTextSize - 12);
        textSizePreview.setTextSize(savedTextSize);
        textSizePreview.setText("Preview Text Size: " + savedTextSize);

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newSize = 12 + progress;
                textSizePreview.setTextSize(newSize);
                textSizePreview.setText("Preview Text Size: " + newSize);
                editor.putInt("textSize", newSize);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Theme setting
        boolean isDarkMode = preferences.getBoolean("darkMode", false);
        themeSwitch.setChecked(isDarkMode);
        themeSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            editor.putBoolean("darkMode", isChecked);
            editor.apply();
        });

        // Reset to default settings
        resetDefaults.setOnClickListener(v -> {
            editor.clear().apply();
            textSizeSeekBar.setProgress(4);
            textSizePreview.setTextSize(16);
            textSizePreview.setText("Preview Text Size: 16");
            themeSwitch.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
        });

        return dialogView;
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            (sharedPreferences, key) -> {
                if ("textSize".equals(key)) {
                    lesson_adapter.notifyDataSetChanged(); // Notify the adapter to refresh views
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState); outState.putInt("SPINNER_POSITION", QWeeks.getSelectedItemPosition());
        if (day != null && wk_day_manager != null) {
            outState.putParcelable("recycler_state", wk_day_manager.onSaveInstanceState());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
        int position = savedInstanceState.getInt("SPINNER_POSITION", 0); QWeeks.setSelection(position);
        if (day != null && wk_day_manager != null && savedInstanceState.containsKey("recycler_state")) {
            Parcelable recyclerState = savedInstanceState.getParcelable("recycler_state");
            wk_day_manager.onRestoreInstanceState(recyclerState);
        }
    }



}
