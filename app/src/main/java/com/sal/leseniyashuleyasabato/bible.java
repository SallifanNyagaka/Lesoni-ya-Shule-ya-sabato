package com.sal.leseniyashuleyasabato;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;

import com.sal.leseniyashuleyasabato.bible;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class bible extends AppCompatActivity implements RecyclerViewClicks {
    RecyclerView bibleBooks;
    RecyclerView.LayoutManager bibleBooksManager;
    List<Books> bookTitles;
    bibleAdapter Adapter;

    String []files;

    BibleStuff stuff = new BibleStuff();
    String []books = stuff.books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible);

        Toolbar myToolbar = findViewById(R.id.bible_toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        try {
            fillBooks();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initBooks();
    }
    
    @Override
public void onBackPressed() {
    // Create an Intent to specify the target activity
        super.onBackPressed();
        Intent intent = new Intent(bible.this, MainActivity.class);
    
    // Start the target activity
    startActivity(intent);
    
    // Call finish() if you want to close the current activity
    finish();
}

    private void fillBooks() throws IOException {
        this.bookTitles = new ArrayList<>();
        AssetManager assetManager = getApplicationContext().getAssets();
        files = assetManager.list("books");

        for (int i = 0; i < books.length; i ++){
            bookTitles.add(new Books(Integer.toString(i+1), books[i]));
        }

    }

    private void initBooks() {
        this.Adapter = new bibleAdapter(this.bookTitles, this);
        this.bibleBooks = findViewById(R.id.bible_books);
        this.bibleBooksManager = new LinearLayoutManager(getApplicationContext());
        this.bibleBooks.setAdapter(this.Adapter);
        this.bibleBooks.setLayoutManager(this.bibleBooksManager);
        this.Adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(bible.this, BibleChapters.class);

        String positions;
        positions = books[position].toLowerCase();
        intent.putExtra("book", positions);

        for (String book: files) {
            if (positions.equals(book.replace(".txt", "").trim())) {
                intent.putExtra("bookName", book);
                intent.putExtra("position", position);
            }
        }

        startActivity(intent);
    }
}