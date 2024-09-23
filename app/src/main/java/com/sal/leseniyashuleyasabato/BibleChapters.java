package com.sal.leseniyashuleyasabato;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
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

public class BibleChapters extends AppCompatActivity implements RecyclerViewClicks{

    RecyclerView chapters;
    RecyclerView.LayoutManager chaptersManager;
    List<ChapterModel> chapterModelList;
    chapterAdapter Adapter;

    Integer prevLine;

    ArrayList<BibleMemory> chapterList;

    String file_Name, chapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_chapters);

        Toolbar toolbar = findViewById(R.id.chapterToolbar);
        setSupportActionBar(toolbar);
        Intent get = getIntent();
        chapter = get.getStringExtra("book");
        TextView chaptersTextbox = findViewById(R.id.chapterTextBox);
        chaptersTextbox.setText(chapter);
        BufferedReader booksReader;
        InputStream stream;
        String bookName = get.getStringExtra("bookName");
        file_Name = "books/"+bookName;

        try {
            stream = getAssets().open(file_Name);
            booksReader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer result = new StringBuffer();
            String buffer;
            chapterList = new ArrayList<>();
            this.chapterModelList = new ArrayList<>();
            Integer chap = 0;
            Integer lineNo = 0;


            while((buffer = booksReader.readLine()) != null){
                result.append(buffer).append("\n");
                lineNo +=1 ;
                prevLine = lineNo;
                if (!buffer.startsWith(" ")){
                    chap += 1;
                    chapterList.add(new BibleMemory(chap, lineNo));
                    this.chapterModelList.add(new ChapterModel(chap.toString()));
                }


            }

            initChapters();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initChapters() {
        this.Adapter = new chapterAdapter(this.chapterModelList, this);
        this.chapters = findViewById(R.id.chapterRecycler);
        this.chaptersManager = new GridLayoutManager(getApplicationContext(), 5);
        this.chapters.setAdapter(this.Adapter);
        this.chapters.setLayoutManager(this.chaptersManager);
        this.Adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getApplicationContext(), Bible_Read.class);
        int rd = position+1;
        intent.putExtra("chapter", chapter + " " + rd);

        ArrayList<Integer> preVerse = new ArrayList<>();
        for (BibleMemory memory: chapterList) {
            int chapter = memory.getLineIncrement();
            Integer chapterNo = memory.getChapterLine();

            if(chapterNo > 1) {
                preVerse.add(chapterNo);

            }
            if (chapter == rd){
                intent.putExtra("startVerse", chapterNo);
            }
        }
        preVerse.add(prevLine+1);
        int root = preVerse.get(position);
        intent.putExtra("stopVerse", root);
        intent.putExtra("bookName", file_Name);

        startActivity(intent);
    }
}