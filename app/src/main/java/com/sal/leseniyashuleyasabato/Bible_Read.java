package com.sal.leseniyashuleyasabato;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bible_Read extends AppCompatActivity implements RecyclerViewClicks{
    private  List<Books> bible_verses;

    TextView verseTextBox;
    RecyclerView verses;
    RecyclerView.LayoutManager bibleVersesManager;
    ReadAdapter Adapter;

    Integer bookId;

    int startVerse, chapter;
    int stopVerse;

    String bookNChapter;

    private GestureDetector gestureDetector;

    FloatingActionButton right, left;

    BibleStuff stuff = new BibleStuff();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_read);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false; // Ignore invalid gestures
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) { // Horizontal swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            loadPreviousDataset(); // Swipe right
                        } else {
                            loadNextDataset(); // Swipe left
                        }
                        return true;
                    }
                }
                return false;
            }

        });

        right = findViewById(R.id.scrollRightBible);
        left = findViewById(R.id.scrollLeftBible);
        Toolbar toolbar = findViewById(R.id.verseToolbar);
        setSupportActionBar(toolbar);
        verseTextBox = findViewById(R.id.verseTextBox);
        Intent get = getIntent();
        int Sverse = get.getIntExtra("verse", 0);
        int to = get.getIntExtra("to", Sverse);
        startVerse = get.getIntExtra("startVerse", 0);
        stopVerse = get.getIntExtra("stopVerse", 0);
        String bookName = get.getStringExtra("bookName");
        bookId = get.getIntExtra("bookPosition", 1);
        chapter = get.getIntExtra("Chapters", 1);
        bookNChapter = getBookNameWithoutTXT(bookId) + " " +chapter;
        verseTextBox.setText(bookNChapter);

        Toast.makeText(this, bookId.toString(), Toast.LENGTH_SHORT ).show();
        BufferedReader versesReader;
        InputStream stream;

        try {
            assert bookName != null;
            stream = getAssets().open(bookName);
            versesReader = new BufferedReader(new InputStreamReader(stream));
            String buffer;
            int count = 0;
            int verse = 0;
            bible_verses = new ArrayList<>();

            while ((buffer = versesReader.readLine()) != null){
                count += 1;
                if (count >= startVerse && count < stopVerse){
                    verse += 1;
                    bible_verses.add(new Books(Integer.toString(verse), buffer));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        right.setOnClickListener(v -> {
            loadNextDataset();
        });

        left.setOnClickListener(v -> {
            loadPreviousDataset();
        });

        initVerses();
        // Add ItemDecoration to highlight verses 4-6
        VerseHighlightDecoration highlightDecoration = new VerseHighlightDecoration(Sverse, to);
        verses.addItemDecoration(highlightDecoration);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    private void loadNextDataset() {
        try {
            BufferedReader versesReader;
            InputStream stream;
            stream = getAssets().open(getBookName(bookId));
            versesReader = new BufferedReader(new InputStreamReader(stream));

            String buffer;
            int lineCount = 0;
            boolean foundNextChapter = false;

            bible_verses.clear();
            int verse = 0;

            // Find the next chapter
            while ((buffer = versesReader.readLine()) != null) {
                lineCount++;
                if (lineCount >= stopVerse) { // Look for the next chapter
                    if (Character.isLetter(buffer.charAt(0))) {
                        // Found the next chapter
                        startVerse = lineCount;
                        verse = 0;
                        bible_verses.add(new Books(Integer.toString(++verse), buffer));
                        foundNextChapter = true;
                        chapter = chapter + 1;
                        bookNChapter = getBookNameWithoutTXT(bookId) + " " + chapter;
                        verseTextBox.setText(bookNChapter);
                        break;
                    }
                }
            }

            // If no next chapter in the current book, load the first chapter of the next book
            if (!foundNextChapter) {
                chapter = 0; // Reset chapter to 1 for the next book
                bookId++;
                if (bookId >= new BibleStuff().books.length) {
                    // Wrap around to the first book
                    bookId = 0;
                    chapter = 0;
                }

                // Update the book and chapter display
                bookNChapter = getBookNameWithoutTXT(bookId) + " " + chapter;
                verseTextBox.setText(bookNChapter);

                // Reset start and stop verses
                startVerse = 1;
                stopVerse = 0;

                // Recursive call for the next book
                loadNextDataset();
                return;
            }

            // Populate remaining verses for the next chapter
            while ((buffer = versesReader.readLine()) != null) {
                lineCount++;
                if (Character.isLetter(buffer.charAt(0))) break; // End of this chapter
                bible_verses.add(new Books(Integer.toString(++verse), buffer));
            }
            stopVerse = lineCount;

            // Initialize verses for display
            initVerses();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void loadPreviousDataset() {
        try {
            // Check if we're at the first chapter of the current book
            if (chapter == 1) {
                // Move to the previous book
                bookId--;
                if (bookId < 0) {
                    // No more books to navigate to
                    Toast.makeText(this, "Hamna vitabu vingine!", Toast.LENGTH_SHORT).show();
                    bookId = 0;
                    return;
                }

                // Open the previous book file
                InputStream stream = getAssets().open(getBookName(bookId));
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                List<String> allLines = new ArrayList<>();
                String line;

                // Read all lines from the previous book into memory
                while ((line = reader.readLine()) != null) {
                    allLines.add(line);
                }

                reader.close();

                // Find the start of the first chapter in the previous book
                int firstChapterStart = -1;
                for (int i = 0; i < allLines.size(); i++) {
                    if (Character.isLetter(allLines.get(i).charAt(0))) {
                        firstChapterStart = i;
                        break;
                    }
                }

                if (firstChapterStart != -1) {
                    // Load the first chapter of the previous book
                    bible_verses.clear();
                    int verseCount = 0;

                    for (int i = firstChapterStart; i < allLines.size(); i++) {
                        String currentLine = allLines.get(i);

                        // Stop when the next chapter begins
                        if (i > firstChapterStart && Character.isLetter(currentLine.charAt(0))) {
                            break;
                        }

                        verseCount++;
                        bible_verses.add(new Books(Integer.toString(verseCount), currentLine));
                    }

                    // Update the chapter and verse variables
                    chapter = 1; // First chapter of the previous book
                    startVerse = firstChapterStart + 1;
                    stopVerse = firstChapterStart + verseCount;

                    // Update the book and chapter display
                    bookNChapter = getBookNameWithoutTXT(bookId) + " " + chapter;
                    verseTextBox.setText(bookNChapter);

                    // Initialize the verses for display
                    initVerses();
                } else {
                    Toast.makeText(this, "Error loading first chapter of the previous book", Toast.LENGTH_SHORT).show();
                }
            } else {
                // We're not at the first chapter; navigate to the previous chapter within the same book
                InputStream stream = getAssets().open(getBookName(bookId));
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                List<String> allLines = new ArrayList<>();
                String line;

                // Read all lines from the current book into memory
                while ((line = reader.readLine()) != null) {
                    allLines.add(line);
                }

                reader.close();

                // Find the start and end of the previous chapter
                int startPointer = startVerse - 1; // Adjust to 0-based index
                int stopPointer = startPointer;
                boolean foundStart = false;
                List<String> previousChapterLines = new ArrayList<>();

                for (int i = startPointer - 1; i >= 0; i--) {
                    line = allLines.get(i);

                    // If the line starts with a letter and we've already found the start of the previous chapter
                    if (Character.isLetter(line.charAt(0)) && foundStart) {
                        stopPointer = i;
                        break;
                    }

                    previousChapterLines.add(line);
                    foundStart = true;
                }

                if (foundStart) {
                    // Found a previous chapter within the current book
                    int verse = 0;
                    bible_verses.clear();

                    chapter -= 1; // Move to the previous chapter

                    // Add the line starting with a letter (chapter header) as the first line
                    String chapterHeader = allLines.get(stopPointer);
                    verse++;
                    bible_verses.add(new Books(Integer.toString(verse), chapterHeader));

                    // Add the rest of the previous chapter lines in reverse order
                    Collections.reverse(previousChapterLines);
                    for (String verseLine : previousChapterLines) {
                        verse++;
                        bible_verses.add(new Books(Integer.toString(verse), verseLine));
                    }

                    // Update the startVerse and stopVerse
                    startVerse = stopPointer + 1;
                    stopVerse = startPointer;

                    // Update the book and chapter display
                    bookNChapter = getBookNameWithoutTXT(bookId) + " " + chapter;
                    verseTextBox.setText(bookNChapter);

                    // Initialize the verses for display
                    initVerses();
                } else {
                    Toast.makeText(this, "Error loading previous chapter", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            // Handle exceptions
            Toast.makeText(this, "Error loading dataset", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    private String getBookName(int bookId) {
        return "books/" + stuff.books[bookId].toLowerCase().trim() + ".txt";
    }

    private String getBookNameWithoutTXT(int bookId){
        return stuff.books[bookId].trim();
    }


    private void initVerses() {
        this.Adapter = new ReadAdapter(bible_verses, this, Bible_Read.this);
        this.verses = findViewById(R.id.readBibleRecyc);
        verses.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        this.bibleVersesManager = new LinearLayoutManager(getApplicationContext());
        this.verses.setAdapter(this.Adapter);
        this.verses.setLayoutManager(bibleVersesManager);
        this.Adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {

    }

    public int bookPosition(String bookName){
        BibleStuff stuff = new BibleStuff();
        int position = 0;

        for (int i = 0; i<stuff.books.length; i++){
            if (bookName.equals("books/" + stuff.books[i] + ".txt")){
                position = i;
                break;
            }
        }

        return position;
    }



}
class VerseHighlightDecoration extends RecyclerView.ItemDecoration {
    private final int startVerse;
    private final int endVerse;
    private final Paint highlightPaint;

    public VerseHighlightDecoration(int startVerse, int endVerse) {
        this.startVerse = startVerse;
        this.endVerse = endVerse;
        this.highlightPaint = new Paint();
        highlightPaint.setColor(0x80808080);  // Set highlight color
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        for (int i = startVerse - 1; i < endVerse; i++) {
            View child = parent.getLayoutManager().findViewByPosition(i);
            if (child != null) {
                canvas.drawRect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom(), highlightPaint);
            }
        }
    }
}