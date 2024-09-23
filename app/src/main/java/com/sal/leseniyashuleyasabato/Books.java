package com.sal.leseniyashuleyasabato;

public class Books {
    private final String book_number;
    private final String book;

    public Books(String book_number, String book) {
        this.book_number = book_number;
        this.book = book;
    }

    public String getBook_number() {
        return book_number;
    }

    public String getBook() {
        return book;
    }
}
