package com.sal.leseniyashuleyasabato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class bibleAdapter extends RecyclerView.Adapter<bibleAdapter.viewHolder> {

    private final List<Books> bible_books;
    private final RecyclerViewClicks recyclerViewClicks;

    public bibleAdapter(List<Books> bibleBooks, RecyclerViewClicks recyclerViewClicks) {
        bible_books = bibleBooks;
        this.recyclerViewClicks = recyclerViewClicks;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View day = LayoutInflater.from(parent.getContext()).inflate(R.layout.bible_books, parent, false);
        return new viewHolder(day, recyclerViewClicks);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String bookNo = this.bible_books.get(position).getBook_number();
        String books = this.bible_books.get(position).getBook();
        holder.setData(bookNo, books);
    }

    @Override
    public int getItemCount() {
        return this.bible_books.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView bookNumber, BookTitle;
        public viewHolder(@NonNull View itemView, RecyclerViewClicks recyclerViewClicks) {
            super(itemView);
            this.bookNumber = itemView.findViewById(R.id.no_kitabu);
            this.BookTitle = itemView.findViewById(R.id.kitabu);
            itemView.setOnClickListener(view -> {
                if(recyclerViewClicks != null){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        recyclerViewClicks.onItemClick(pos);
                    }
                }
            });

        }

        public void setData(String bookNo, String books) {
            this.bookNumber.setText(bookNo);
            this.BookTitle.setText(books);
        }
    }
}
