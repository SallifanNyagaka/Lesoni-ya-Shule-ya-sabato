package com.sal.leseniyashuleyasabato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReadAdapter extends RecyclerView.Adapter<ReadAdapter.viewHolder> {

    private final List<Books> verses;
    private final RecyclerViewClicks recyclerViewClicks;

    public ReadAdapter(List<Books> verses, RecyclerViewClicks recyclerViewClicks) {
        this.verses = verses;
        this.recyclerViewClicks = recyclerViewClicks;
    }


    @NonNull
    @Override
    public ReadAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View verse = LayoutInflater.from(parent.getContext()).inflate(R.layout.read_bible, parent, false);
        return new viewHolder(verse, recyclerViewClicks);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadAdapter.viewHolder holder, int position) {
        String verseNo = this.verses.get(position).getBook_number();
        String verse = this.verses.get(position).getBook();
        holder.setData(verseNo, verse);
    }

    @Override
    public int getItemCount() {
        return verses.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView verseNumber, Verse;
        public viewHolder(@NonNull View itemView, RecyclerViewClicks recyclerViewClicks) {
            super(itemView);
            this.verseNumber = itemView.findViewById(R.id.verseNo);
            this.Verse = itemView.findViewById(R.id.verse);
            itemView.setOnClickListener(view -> {
                if(recyclerViewClicks != null){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        recyclerViewClicks.onItemClick(pos);
                    }
                }
            });
        }

        public void setData(String verseNo, String verse) {
            this.verseNumber.setText(verseNo);
            this.Verse.setText(verse);
        }
    }
}
