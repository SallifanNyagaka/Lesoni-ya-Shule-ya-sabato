package com.sal.leseniyashuleyasabato;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.core.FirestoreClient;
import com.sal.leseniyashuleyasabato.bible;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

/* loaded from: classes3.dex */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener, TextToSpeech.OnInitListener {
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
    Integer month = currentCal.get(Calendar.MONTH) + 1;  // Calendar.MONTH is zero-based;
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

    
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the SharedPreferences to check registration status
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean("isRegistered", false);
        boolean isLoggedIn = prefs.getBoolean("isLogged", true);

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
        
        this.textToSpeech = new TextToSpeech(this, this);
        this.quarter_image = findViewById(R.id.quarter_image);
        this.verses = findViewById(R.id.verses);
        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        
        try {
        final DocumentReference quarter = db.collection("quarters_"+year.toString()).document(Quarter());       
        quarter.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
           @Override
           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    quarterTitle = document.getString("QuarterTitle");
                    quarterIntroduction = document.getString("quarterIntroduction");
                    imageUrl = document.getString("image_url");
                                            
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(quarterTitle);                                
                    }  
                    
                    if (!MainActivity.this.isDestroyed() && imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(MainActivity.this).load(imageUrl).into(quarter_image);
                    } else {
                            quarter_image.setImageResource(R.drawable.lesson); // Or any default image
                        }
                    if(quarterIntroduction != null) {
                    	SpannableString highlightedText = spannableBibleText(quarterIntroduction);
                        verses.setText(highlightedText);
                        verses.setMovementMethod(LinkMovementMethod.getInstance());  
                    }else{
                        Toast.makeText(getApplicationContext(), "Quarter Intro not loaded", Toast.LENGTH_LONG).show();
                    }            
                          
                                
                    Toast.makeText(getApplicationContext(), quarterTitle, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No such Document", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "failed to load document", Toast.LENGTH_SHORT).show();
                }
            }    
        });
            
        
        
        weekTitle = findViewById(R.id.weekTitle);
        weekTitle.setText(quarterMonths(Quarter()));   
        
        this.QWeeks = findViewById(R.id.quarterWeeks);
        String str = "quarters_"+year.toString()+"/"+Quarter()+"/weeks";
        this.quarterCollectionPath = str;
        this.quarterWeekDocumentPath = "/WK-1/days";
        this.weekT = this.db.collection("quarters_"+year.toString()).document(Quarter()).collection("weeks").document("WK-1");
        this.quarterWeekDays = this.db.collection("quarters"+year.toString());
        this.drawer = findViewById(R.id.drawer_layout);
        btnToggle = findViewById(R.id.btnToggle);    
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        btnToggle.setOnClickListener(new View.OnClickListener() {
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
            
        
        initDays();
        lesson_init();
            
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (lesson_adapter.isConnected()) {
                        // Synchronize offline comments with Firestore when connectivity is restored
                        lesson_adapter.syncOfflineComments(); // Call adapter method to sync comments
                        lesson_adapter.syncOfflineHighlights(); //sync highlights
                        lesson_adapter.syncRemovedHighlights(); //sync removed highlights
                    }
                }
            };
            registerReceiver(networkReceiver, filter);
            
        	
        } catch(Exception err) {
        	Toast.makeText(getApplicationContext(), "onCreate() "+err.toString(), Toast.LENGTH_LONG).show();
        }
       
    }
    
    

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onStart() {
        super.onStart();
        this.weekT.addSnapshotListener(this, new EventListener<DocumentSnapshot>() { // from class: com.sal.leseniyashuleyasabato.MainActivity.1
        static final /* synthetic */ boolean $assertionsDisabled = false;

            @Override // com.google.firebase.firestore.EventListener
            public void onEvent(DocumentSnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Error while loading", Toast.LENGTH_SHORT).show();
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
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Swahili language is not supported!");
                Toast.makeText(getApplicationContext(), "Samahani Lugha ya Kiswahili haikubaliki kwa mtambo huu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("TTS", "Initialization failed!");
            Toast.makeText(getApplicationContext(), "Imeshindwa Kuanzisha msomaji", Toast.LENGTH_SHORT).show();
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
    
    public String quarterMonths(String quarter){
        String months = "Lesoni ya shule ya Sabato";
        if (quarter == "Q1"){
            months = "Januari, Februari, Machi";
        }else if(quarter == "Q2"){
            months = "Aprili, Mei, Juni";
        }else if(quarter == "Q3"){
            months = "Julai, Agosti, Septemba";
        }else if(quarter == "Q4"){
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
        try {
        	this.lesson_days = new ArrayList<>();
            this.lesson_adapter = new Adapter(this.lesson_days, this);
            this.day = findViewById(R.id.wk_day);
            this.wk_day_manager = new LinearLayoutManager(getApplicationContext());
            this.day.setAdapter(this.lesson_adapter);
            this.day.setLayoutManager(this.wk_day_manager);
        } catch(Exception err) {
            Toast.makeText(getApplicationContext(), "initDays() "+err.toString(), Toast.LENGTH_SHORT).show();
        }
        
    }

private void lesson_init() {
        try {
            // Use a Set to ensure unique titles
            HashSet<String> titles = new HashSet<>();

            // Show a progress indicator while fetching data
            // ... (e.g., show a progress bar or disable UI elements)

            db.collection("quarters_" + year)
                .document(Quarter()) // Assuming Quarter() returns the current quarter
                .collection("weeks")
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot weekSnapshot : queryDocumentSnapshots) {
                            String weekDateRange = weekSnapshot.getString("weekDateRange");
                            String title = weekSnapshot.getString("week_Title");
                            if (title != null) {
                                titles.add(title);
                            } else {
                                // Handle cases where the "title" field is missing
                                // Log an error or display a warning message
                                // ...
                            }
                        }

                        // Hide the progress indicator
                        // ...

                        // Populate the spinner with unique titles
                        ArrayAdapter<String> weekTitles = new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(titles));
                        QWeeks.setAdapter(weekTitles);

                        QWeeks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                String selectedTitle = adapterView.getItemAtPosition(position).toString();
                                fetchLessonContent(selectedTitle);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                                // Handle the case where nothing is selected
                                // ...
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hide the progress indicator
                        // ...

                        Toast.makeText(MainActivity.this, "Failed to load quarters: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // Log the error for debugging
                        // ...
                    }
                });
        } catch (Exception err) {
            Toast.makeText(getApplicationContext(), "lessonInit() " + err.toString(), Toast.LENGTH_SHORT).show();
            // Log the error for debugging
            // ...
        }
    }
    
    
private void fetchLessonContent(String selectedTitle) {
        db.collection("quarters_" + year.toString())  // Full path reference to quarters
        .document(Quarter())
        .collection("weeks")
        .get()
        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    if (selectedTitle.equals(snapshot.getString("week_Title"))) {
                        lesson_days.clear();    
                        db.collection("quarters_" + year.toString())  // Full path reference to the specific week
                            .document(Quarter())
                            .collection("weeks")
                            .document(snapshot.getId())
                            .collection("days")
                            .orderBy("timeStamp", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot snapshot2 : queryDocumentSnapshots) {
                                        String days = snapshot2.getString("date");
                                        String day_title = snapshot2.getString("title");
                                        String day_content = snapshot2.getString("content");
                                        String day_question = snapshot2.getString("question");
                                        String image_Uri = snapshot2.getString("image_url");
                                        String dateEng = snapshot2.getString("dateEng");  
                                        String weekRange = snapshot2.getString("weekDateRange");    
                                        lesson_days.add(new LessonModels(days, dateEng, weekRange, R.drawable.share_today, day_title, day_content, day_question, image_Uri));
                                    }
                                    lesson_adapter.notifyDataSetChanged();
                                }
                            });
                    }
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to load Content: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
}    
    
    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.calender) {
            DialogFragment pickDate = new DatePickerFragment();
            pickDate.show(getSupportFragmentManager(), "date picker");
            Toast.makeText(MainActivity.this, "calender selected", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.bible){
            Intent bible = new Intent(MainActivity.this, com.sal.leseniyashuleyasabato.bible.class);
            startActivity(bible);
            return true;

        }
        if(item.getItemId() == R.id.login_register){
            Intent reg = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(reg);
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
    
    public ArrayList<Integer> bible_Chapters(String fileName, int chaptr){
        
        ArrayList<Integer> chapters =new ArrayList<>();
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
            
            while((buffer = read.readLine()) != null) {
            	result.append(buffer).append("\n");
                ChaptLine +=1 ;
                prevline = ChaptLine;
                
                if (!buffer.startsWith(" ")){
                    Chapt += 1;
                    chapterNlineNo.add(new BibleMemory(Chapt, ChaptLine));
                }    
            }
        
        } catch(Exception err) {
        	Toast.makeText(getApplicationContext(), "Kifungu hakikupatikana.\n Inafungua Biblia...", Toast.LENGTH_LONG).show();
            cannotOpen();
        }
        try {
        	for (BibleMemory memory: chapterNlineNo) {
            int chapter = memory.getLineIncrement();
            Integer chapterNo = memory.getChapterLine();
            if(chapterNo > 1) {
                preVerse.add(chapterNo);
            }
                
            if (chapter == chaptr){
                chapters.add(chapterNo);
            }
        }
        preVerse.add(prevline+1);
        int stop = preVerse.get(chaptr-1);
        chapters.add(stop);
            
        } catch(IndexOutOfBoundsException err) {
            Toast.makeText(getApplicationContext(), "Samahani Kifungu Hiki Hakikupatikana Katika Biblia. Inafungua Biblia...", Toast.LENGTH_LONG).show();
            cannotOpen();
        }
        
        
        return chapters;
    }
    
      // Define a regex pattern to match all Swahili book names and verse numbers
    public SpannableString spannableBibleText(String mafungu) {
    SpannableString spannableString = new SpannableString(mafungu);

    // Define the regex pattern to match simple verse structures
    Pattern versePattern = Pattern.compile(
        "(Mwanzo|Kutoka|Walawi|Hesabu|Kumbukumbu la Torati|Yoshua|Waamuzi|Ruthu|1 Samweli|2 Samweli|1 Wafalme|2 Wafalme|1 Mambo ya Nyakati|2 Mambo ya Nyakati|Ezra|Nehemia|Esta|Ayubu|Zaburi|Mithali|Mhubiri|Wimbo Ulio Bora|Isaya|Yeremia|Maombolezo|Ezekieli|Danieli|Hosea|Yoeli|Amosi|Obadia|Yona|Mika|Nahumu|Habakuki|Sefania|Hagai|Zekaria|Malaki|Mathayo|Marko|Luka|Yohana|Matendo ya Mitume|Warumi|1 Wakorintho|2 Wakorintho|Wagalatia|Waefeso|Wafilipi|Wakolosai|1 Wathesalonike|2 Wathesalonike|1 Timotheo|2 Timotheo|Tito|Filemoni|Waebrania|Yakobo|1 Petro|2 Petro|1 Yohana|2 Yohana|3 Yohana|Yuda|Ufunuo wa Yohana)\\s\\d+(:\\d+(-\\d+)?(,\\s*\\d+(:\\d+(-\\d+)?)*|,\\s*\\d+)*(\\s*,\\s*\\d+(:\\d+(-\\d+)?)*?)?)?\\s*;"
    );

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
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), partStart, partEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new UnderlineSpan(), partStart, partEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Apply clickable span
                    final String verse = fullVerse; // Make the verse effectively final for the inner class
                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            Toast.makeText(getApplicationContext(), verse, Toast.LENGTH_SHORT).show();       
                            handleVerseClick(verse, partStart, partEnd);
                        }
                    }, partStart, partEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    return spannableString;
}
 
    
    
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
                Integer currentChapter = matcher.group(3) != null ? Integer.parseInt(matcher.group(3).trim()) : chapter;
                Integer currentVerse = matcher.group(4) != null ? Integer.parseInt(matcher.group(4).trim()) : 1;
                Integer currentTo = matcher.group(5) != null ? Integer.parseInt(matcher.group(5).trim()) : currentVerse;

                // Check for comma-separated verses
                if (segment.contains(",")) {
                    String[] parts = segment.split(",");
                    for (String part : parts) {
                        Matcher partMatcher = pattern.matcher(part.trim());
                        if (partMatcher.find()) {
                            Integer partTo = partMatcher.group(5) != null ? Integer.parseInt(partMatcher.group(5).trim()) : null;
                            if (partTo != null) {
                                currentTo = Math.max(currentTo, partTo); // Update currentTo with the maximum value
                            }
                        }
                    }
                }

                // Update chapter, verse, and to with the current values
                chapter = currentChapter;
                verse = currentVerse;
                to = currentTo;

                // Handle the case where the book name ends with a number but isn't supposed to
                if (!currentBook.isEmpty() && currentBook.matches(".*\\d$") && !currentBook.matches("^\\d.*")) {
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
                Toast.makeText(getApplicationContext(), "Book: " + book + "\nChapter: " + chapter + "\nVerse: " + verse + "\nTo: " + to, Toast.LENGTH_LONG).show();

                // Handle each segment (e.g., create spans, store ranges, etc.)
                // You can add your logic here to process each chapter and verse
            } else {
                // Handle the case where the format does not match
                Toast.makeText(getApplicationContext(), "Error parsing verse: " + segment, Toast.LENGTH_SHORT).show();
            }
        }
    } catch (Exception e) {
        Toast.makeText(getApplicationContext(), "Error parsing verse: " + compVerse, Toast.LENGTH_LONG).show();
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
    
    
}