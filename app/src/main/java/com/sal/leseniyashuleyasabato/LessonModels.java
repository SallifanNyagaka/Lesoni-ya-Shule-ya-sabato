package com.sal.leseniyashuleyasabato;

import java.util.Objects;

public class LessonModels {
    private final String date;
    private final String dateEng;
    private final String weekRange;
    private final String day_content;
    private final String day_question;
    private final String day_title;
    private final int share_image;
    private final String saturday_image_uri;

    // Add this constructor so Gson or Firestore can instantiate the class
    public LessonModels() {
        this.date = null;
        this.dateEng = null;
        this.weekRange = null;
        this.share_image = 0;
        this.day_title = null;
        this.day_content = null;
        this.day_question = null;
        this.saturday_image_uri = null;
    }



    public LessonModels(String date, String dateEng, String weekRange, int share_image, String day_title, String day_content, String day_question, String saturday_image_uri) {
        this.date = date;
        this.dateEng = dateEng;
        this.share_image = share_image;
        this.day_title = day_title;
        this.day_content = day_content;
        this.day_question = day_question;
        this.saturday_image_uri = saturday_image_uri;
        this.weekRange = weekRange;
    }

    public String getDate() {
        return this.date;
    }
    
    public String getWeekRange() {
        return this.weekRange;
    }
    
    
    public String getDateEng() {
        return this.dateEng;
    }

    public String getDay_title() {
        return this.day_title;
    }

    public String getDay_content() {
        return this.day_content;
    }

    public String getDay_question() {
        return this.day_question;
    }

    public int getShare_image() {
        return this.share_image;
    }

    public String getSaturday_image_uri() {
        return this.saturday_image_uri;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LessonModels that = (LessonModels) obj;
        return share_image == that.share_image &&
                date.equals(that.date) &&
                dateEng.equals(that.dateEng) &&
                weekRange.equals(that.weekRange) &&
                day_title.equals(that.day_title) &&
                day_content.equals(that.day_content) &&
                day_question.equals(that.day_question) &&
                ((saturday_image_uri == null && that.saturday_image_uri == null) ||
                        (saturday_image_uri != null && saturday_image_uri.equals(that.saturday_image_uri)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, dateEng, weekRange, day_title, day_content, day_question, share_image, saturday_image_uri);
    }


}