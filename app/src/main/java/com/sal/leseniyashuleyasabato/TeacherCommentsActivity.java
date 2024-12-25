package com.sal.leseniyashuleyasabato;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import android.util.Log;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeacherCommentsActivity extends AppCompatActivity {

    private Spinner spinnerWeekComments;
    private LinearLayout commentsContainer;
    private String path = "quarters_2024/Q4/weeks";
    private Integer wk_comment = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_comments);


        // Initialize UI components
        spinnerWeekComments = findViewById(R.id.spinner_week_comments);
        commentsContainer = findViewById(R.id.comments_container);




        Intent intent = getIntent();
        path = intent.getStringExtra("path_name");
        wk_comment = intent.getIntExtra("wk_comment", 0);


        // Set up Spinner
        setupSpinner(path);

        spinnerWeekComments.post(() -> {
            spinnerWeekComments.setSelection(wk_comment);
            int wk_collection = wk_comment + 1;
            String commentPath = path + "quarters_2024/WK-" + wk_collection + "/teacher/Teacher_Comments";
            loadTeacherComment(commentPath);
        });

    }

    private void setupSpinner(String path) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> weekTitles = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(TeacherCommentsActivity.this, android.R.layout.simple_spinner_item, weekTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeekComments.setAdapter(adapter);

        // Fetch titles from Firestore
        db.collection(path)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getId() + ": " + document.getString("week_Title") + " - " + document.getString("weekDateRange");
                        if (title != null) {
                            weekTitles.add(title);
                        }
                    }
                    adapter.notifyDataSetChanged(); // Refresh spinner data
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching week titles", e));

        // Handle spinner selection
        spinnerWeekComments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWeek = "WK-" + (position + 1); // Adjusted to start from WK-1
                loadTeacherComment(path + "/" + selectedWeek + "/teacher/Teacher_Comments");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });

        // Set default selection to WK-1
        spinnerWeekComments.post(() -> spinnerWeekComments.setSelection(0));
    }

    private void loadTeacherComment(String commentPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.document(commentPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String comment = documentSnapshot.getString("content");
                        if (comment != null) {
                            displayComment(comment); // Display the comment
                        } else {
                            Toast.makeText(TeacherCommentsActivity.this, "Comment field is missing.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "Document does not exist at path: " + commentPath);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching comment", e));
    }

    private void displayComment(String comment) {
        commentsContainer.removeAllViews(); // Clear previous comments

        createTextViews(comment);
        /*TextView textView = new TextView(TeacherCommentsActivity.this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setText( spannableBibleText(comment));
        textView.setTextSize(16);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundColor(Color.parseColor("#F8F8F8"));
        textView.setTextColor(Color.BLACK);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        commentsContainer.addView(textView); // Add the TextView to the container*/
    }


    public ArrayList<Integer> bible_Chapters(String fileName, int chaptr) {

        ArrayList<Integer> chapters = new ArrayList<>();
        Integer prevline = 0;
        ArrayList<BibleMemory> chapterNlineNo = new ArrayList<>();
        ArrayList<Integer> preVerse = new ArrayList<>();

        try {
            InputStream stream = TeacherCommentsActivity.this.getAssets().open(fileName);
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
                            TeacherCommentsActivity.this,
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
                            TeacherCommentsActivity.this,
                            "Samahani Kifungu Hiki Hakikupatikana Katika Biblia. Inafungua Biblia...",
                            Toast.LENGTH_LONG)
                    .show();
            cannotOpen();
        }

        return chapters;
    }

    public void createTextViews(String mafungu) {
        // Map to store title-content pairs
        Map<String, String> titleContentMap = new HashMap<>();

        // Regex pattern to extract title-content pairs based on asterisks
        Pattern pattern = Pattern.compile("\\*(.*?)\\*([^*]+)");  // Matches *title*content
        Matcher matcher = pattern.matcher(mafungu);

        while (matcher.find()) {
            String title = matcher.group(1).trim(); // Title is between asterisks
            String content = matcher.group(2).trim(); // Content is after the title
            titleContentMap.put(title, content);  // Add to map
        }

        // Create a LinearLayout to dynamically add TextViews
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // Iterate over the map to create TextViews for each title-content pair
        for (Map.Entry<String, String> entry : titleContentMap.entrySet()) {
            String title = entry.getKey();
            String content = entry.getValue();

            // Create TextView for title (bold)
            TextView titleTextView = new TextView(this);
            titleTextView.setText(title);
            titleTextView.setTypeface(null, Typeface.BOLD); // Bold the title
            linearLayout.addView(titleTextView); // Add to layout

            // Create SpannableString for content with verse spans
            SpannableString spannableContent = spannableBibleText(content);

            // Create TextView for content and apply the spannable string
            TextView contentTextView = new TextView(this);
            contentTextView.setText(spannableContent);
            contentTextView.setMovementMethod(LinkMovementMethod.getInstance()); // Enable clicks
            linearLayout.addView(contentTextView); // Add to layout
        }

        // Add the LinearLayout to your parent layout
        commentsContainer.addView(linearLayout);
    }


    // Define a regex pattern to match all Swahili book names and verse numbers
    public SpannableString spannableBibleText(String mafungu) {
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
                                    TeacherCommentsActivity.this,
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
                                    TeacherCommentsActivity.this,
                                    "Error parsing verse: " + segment,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(
                            TeacherCommentsActivity.this,
                            "Error parsing verse: " + compVerse,
                            Toast.LENGTH_LONG)
                    .show();
        }

        // Start the Bible_Read activity with the extracted information
        Intent bible = new Intent(TeacherCommentsActivity.this, Bible_Read.class);
        ArrayList<Integer> StartStop = bible_Chapters(book, chapter);
        bible.putExtra("chapter", book + " " + chapter);
        bible.putExtra("startVerse", StartStop.get(0));
        bible.putExtra("stopVerse", StartStop.get(1));
        bible.putExtra("bookName", book);
        startActivity(bible);
    }

    private void cannotOpen() {
        Intent intent = new Intent(TeacherCommentsActivity.this, bible.class);
        startActivity(intent);
    }

}