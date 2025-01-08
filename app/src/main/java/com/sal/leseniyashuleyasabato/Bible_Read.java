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

    RecyclerView verses;
    RecyclerView.LayoutManager bibleVersesManager;
    ReadAdapter Adapter;

    int bookId;

    int startVerse;
    int stopVerse;

    private GestureDetector gestureDetector;

    FloatingActionButton right, left;


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
        TextView verseTextBox = findViewById(R.id.verseTextBox);
        Intent get = getIntent();
        verseTextBox.setText(get.getStringExtra("chapter"));
        int Sverse = get.getIntExtra("verse", 0);
        int to = get.getIntExtra("to", Sverse);
        startVerse = get.getIntExtra("startVerse", 0);
        stopVerse = get.getIntExtra("stopVerse", 0);
        String bookName = get.getStringExtra("bookName");
        bookId = bookPosition(bookName);

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
                        break;
                    }
                }
            }

            // If no next chapter in the current book, load the first chapter of the next book
            if (!foundNextChapter) {
                bookId++;
                if (bookId >= new BibleStuff().books.length) bookId = 0; // Wrap around
                startVerse = 1;
                stopVerse = Integer.MAX_VALUE; // Until the end of the file
                loadNextDataset(); // Recursive call for the next book
                return;
            }

            // Populate remaining verses for the next chapter
            while ((buffer = versesReader.readLine()) != null) {
                lineCount++;
                if (Character.isLetter(buffer.charAt(0))) break; // End of this chapter
                bible_verses.add(new Books(Integer.toString(++verse), buffer));
            }
            stopVerse = lineCount;

            initVerses();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPreviousDataset() {
        try {
            // Open the file in read mode
            InputStream stream = getAssets().open(getBookName(bookId));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            List<String> allLines = new ArrayList<>();
            String line;

            // Read all lines into memory
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }

            // Close the reader
            reader.close();

            // Start from the startVerse and read backwards
            int startPointer = startVerse - 1; // Adjust to 0-based index
            int stopPointer = startPointer;
            boolean foundStart = false;
            List<String> previousChapterLines = new ArrayList<>();

            // Loop backward through the lines
            for (int i = startPointer - 1; i >= 0; i--) { // Start from the previous line (one before startVerse)
                line = allLines.get(i);

                // If the line starts with an alphabet and we've already found the start of the previous chapter
                if (Character.isLetter(line.charAt(0)) && foundStart) {
                    stopPointer = i; // Stop at this line (include this line)
                    break;
                }

                // Add line to previous chapter lines
                previousChapterLines.add(line); // Add normally, no need to reverse
                foundStart = true;
            }

            // If a previous chapter is found, load it
            if (foundStart) {
                int verse = 0;
                bible_verses.clear();

                // Add the line starting with an alphabet (first line of the previous chapter) as the first line
                String alphabetLine = allLines.get(stopPointer);
                verse++;
                bible_verses.add(new Books(Integer.toString(verse), alphabetLine));

                // Add the other previous chapter lines in reverse order
                Collections.reverse(previousChapterLines);
                for (String verseLine : previousChapterLines) {
                    verse++;
                    bible_verses.add(new Books(Integer.toString(verse), verseLine));
                }

                // Update the startVerse and stopVerse correctly
                startVerse = stopPointer + 1; // The new startVerse is one after the stop pointer
                stopVerse = startPointer; // The stopVerse is where we ended

                initVerses(); // Initialize the verses for display
            } else {
                // If no previous chapter is found, show a Toast
                Toast.makeText(this, "No previous chapter found!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            // Handle the exception and show a toast
            Toast.makeText(this, "Error loading previous dataset", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }







    private String getBookName(int bookId) {
        BibleStuff stuff = new BibleStuff();
        return "books/" + stuff.books[bookId].toLowerCase().trim() + ".txt";
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
            if (bookName.contains(stuff.books[i])){
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