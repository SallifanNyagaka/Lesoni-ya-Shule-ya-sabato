package com.sal.leseniyashuleyasabato;

public class BibleMemory {
    private final Integer ChapterLine;
    private final Integer lineIncrement;


    public BibleMemory(int lineIncrement, int chapterLine) {
        this.ChapterLine = chapterLine;
        this.lineIncrement = lineIncrement;
    }

    public Integer getChapterLine() {
        return ChapterLine;
    }

    public Integer getLineIncrement() {
        return lineIncrement;
    }

}
