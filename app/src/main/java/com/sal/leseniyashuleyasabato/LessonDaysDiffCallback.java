package com.sal.leseniyashuleyasabato;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class LessonDaysDiffCallback extends DiffUtil.Callback {

    private final List<LessonModels> oldList;
    private final List<LessonModels> newList;

    public LessonDaysDiffCallback(List<LessonModels> oldList, List<LessonModels> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Compare unique identifiers (e.g., IDs)
        return oldList.get(oldItemPosition).getDate().equals(newList.get(newItemPosition).getDate());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        LessonModels oldItem = oldList.get(oldItemPosition);
        LessonModels newItem = newList.get(newItemPosition);

        return
                (oldItem.getDate() != null ? oldItem.getDate().equals(newItem.getDate()) : newItem.getDate() == null) &&
                        (oldItem.getDay_title() != null ? oldItem.getDay_title().equals(newItem.getDay_title()) : newItem.getDay_title() == null) &&
                        (oldItem.getDay_content() != null ? oldItem.getDay_content().equals(newItem.getDay_content()) : newItem.getDay_content() == null) &&
                        (oldItem.getDay_question() != null ? oldItem.getDay_question().equals(newItem.getDay_question()) : newItem.getDay_question() == null);
    }

}

