package com.sal.leseniyashuleyasabato;

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