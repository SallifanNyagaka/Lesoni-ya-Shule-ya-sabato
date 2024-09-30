package com.sal.leseniyashuleyasabato;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Bible_Read extends AppCompatActivity implements RecyclerViewClicks{
    private  List<Books> bible_verses;

    RecyclerView verses;
    RecyclerView.LayoutManager bibleVersesManager;
    ReadAdapter Adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_read);

        Toolbar toolbar = findViewById(R.id.verseToolbar);
        setSupportActionBar(toolbar);
        TextView verseTextBox = findViewById(R.id.verseTextBox);
        Intent get = getIntent();
        verseTextBox.setText(get.getStringExtra("chapter"));
        int startVerse = get.getIntExtra("startVerse", 0);
        int stopVerse = get.getIntExtra("stopVerse", 0);
        int Sverse = get.getIntExtra("verse", 0);
        int to = get.getIntExtra("to", Sverse);
        String bookName = get.getStringExtra("bookName");
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

        initVerses();
        // Add ItemDecoration to highlight verses 4-6
        VerseHighlightDecoration highlightDecoration = new VerseHighlightDecoration(Sverse, to);
        verses.addItemDecoration(highlightDecoration);

    }

    private void initVerses() {
        this.Adapter = new ReadAdapter(bible_verses, this);
        this.verses = findViewById(R.id.readBibleRecyc);
        this.bibleVersesManager = new LinearLayoutManager(getApplicationContext());
        this.verses.setAdapter(this.Adapter);
        this.verses.setLayoutManager(bibleVersesManager);
        this.Adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {

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