package com.sal.leseniyashuleyasabato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class chapterAdapter extends RecyclerView.Adapter<chapterAdapter.viewHolder> {

    private final List<ChapterModel> chapterModels;
    private final RecyclerViewClicks recyclerViewClicks;

    public chapterAdapter(List<ChapterModel> chapterModels, RecyclerViewClicks recyclerViewClicks) {

        this.chapterModels = chapterModels;
        this.recyclerViewClicks = recyclerViewClicks;

    }


    @NonNull
    @Override
    public chapterAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chapter = LayoutInflater.from(parent.getContext()).inflate(R.layout.mlango, parent, false);
        return new viewHolder(chapter, recyclerViewClicks);
    }

    @Override
    public void onBindViewHolder(@NonNull chapterAdapter.viewHolder holder, int position) {
        String chap = this.chapterModels.get(position).getChapter();
        holder.setData(chap);
    }

    @Override
    public int getItemCount() {
        return this.chapterModels.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView chapter;
        public viewHolder(@NonNull View itemView, RecyclerViewClicks recyclerViewClicks) {
            super(itemView);
            this.chapter = itemView.findViewById(R.id.chapter);
            itemView.setOnClickListener(view -> {
                if (recyclerViewClicks != null){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        recyclerViewClicks.onItemClick(pos);
                    }
                }
            });
        }

        public void setData(String chap) {
            this.chapter.setText(chap);
        }
    }
}
