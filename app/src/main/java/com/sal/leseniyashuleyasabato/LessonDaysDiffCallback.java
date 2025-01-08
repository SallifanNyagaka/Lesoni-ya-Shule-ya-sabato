package com.sal.leseniyashuleyasabato;

import androidx.annotation.NonNull;
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
        // Compare contents for equality
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}

