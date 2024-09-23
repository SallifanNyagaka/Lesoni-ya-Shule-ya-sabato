package com.sal.leseniyashuleyasabato;
import java.util.Locale;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private final List<LessonModels> days;
    private Context context;
    private TextToSpeech textToSpeech;
    String tray = "gjjhggdzxjj";

    public Adapter(List<LessonModels> days, Context context) {
        this.days = days;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View day = LayoutInflater.from(context).inflate(R.layout.wk_day_recyc, parent, false);
        return new ViewHolder(day, context); // Pass the context to the ViewHolder
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LessonModels lessonModel = days.get(position);
        holder.setData(lessonModel);
        
        String date = holder.date.getText().toString();
        String weekRange = holder.weekDateRange.getText().toString();
        String title = holder.day_title.getText().toString();
        String content = holder.day_content.getText().toString();
        String question = holder.day_question.getText().toString();
        
        
        holder.share_image.setOnClickListener(v ->{
            String toShare = date + "\n" + weekRange + "\n" + title + "\n" + content + "\n" + question;
                
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, toShare);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));                
        });
        
        holder.ic_tts.setOnClickListener(v -> {
            String textToRead = title + ". " + content + ". " + question;

            // Initialize TextToSpeech and start reading
            textToSpeech = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("sw"));
                    textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });                     
        });
    }

    @Override
    public int getItemCount() {
        return this.days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final TextView date;
        private final TextView weekDateRange;
        private final TextView day_content;
        private final TextView day_question;
        private final TextView day_title;
        private final ImageView share_image;
        private final ImageView saturday_image, ic_tts;

        public ViewHolder(View itemView, Context context) { // Constructor receives the context
            super(itemView);
            this.context = context;
            this.date = itemView.findViewById(R.id.date);
            this.weekDateRange = itemView.findViewById(R.id.weekRange);
            this.day_title = itemView.findViewById(R.id.day_title);
            this.day_content = itemView.findViewById(R.id.day_content);
            this.day_question = itemView.findViewById(R.id.day_question);
            this.share_image = itemView.findViewById(R.id.day_share);
            this.ic_tts = itemView.findViewById(R.id.ic_tts);
            this.saturday_image = itemView.findViewById(R.id.saturday_image);
        }

        public void setData(LessonModels lessonModel) {
            this.date.setText(lessonModel.getDate());
            this.weekDateRange.setText("Wiki hii: " + lessonModel.getWeekRange());
            this.day_title.setText(lessonModel.getDay_title());
            spannableTextViewInitializers(this.day_content, lessonModel.getDay_content());
            spannableTextViewInitializers(this.day_question, lessonModel.getDay_question());
            this.share_image.setImageResource(lessonModel.getShare_image());

            // Check if it's Saturday and set the Saturday image visibility
            if (lessonModel.getDateEng().toLowerCase().contains("saturday")) {
                this.saturday_image.setVisibility(View.VISIBLE);
                Glide.with(context) // Use the passed context here
                        .load(Uri.parse(lessonModel.getSaturday_image_uri()))
                        .into(this.saturday_image);
            } else {
                this.saturday_image.setVisibility(View.GONE);
            }
        }
        
        private void spannableTextViewInitializers(TextView Tv, String spannableText) {
        	SpannableString highlightedText = spannableBibleText(spannableText);
            Tv.setText(highlightedText);
            Tv.setMovementMethod(LinkMovementMethod.getInstance());
        }

        public ArrayList<Integer> bible_Chapters(String fileName, int chaptr) {
            ArrayList<Integer> chapters = new ArrayList<>();
            Integer prevline = 0;
            ArrayList<BibleMemory> chapterNlineNo = new ArrayList<>();
            ArrayList<Integer> preVerse = new ArrayList<>();

            try {
                InputStream stream = context.getAssets().open(fileName); // Use context here
                BufferedReader read = new BufferedReader(new InputStreamReader(stream));
                StringBuffer result = new StringBuffer();
                String buffer;
                chapterNlineNo = new ArrayList<>();
                Integer Chapt = 0;
                Integer ChaptLine = 0;

                while ((buffer = read.readLine()) != null) {
                    result.append(buffer).append("\n");
                    ChaptLine += 1;
                    prevline = ChaptLine;

                    if (!buffer.startsWith(" ")) {
                        Chapt += 1;
                        chapterNlineNo.add(new BibleMemory(Chapt, ChaptLine));
                    }
                }

            } catch (Exception err) {
                Toast.makeText(context, "Kifungu hakikupatikana.\n Inafungua Biblia...", Toast.LENGTH_LONG).show(); // Use context
                cannotOpen();
            }
            try {
                for (BibleMemory memory : chapterNlineNo) {
                    int chapter = memory.getLineIncrement();
                    Integer chapterNo = memory.getChapterLine();
                    if (chapterNo > 1) {
                        preVerse.add(chapterNo);
                    }

                    if (chapter == chaptr) {
                        chapters.add(chapterNo);
                    }
                }
                preVerse.add(prevline + 1);
                int stop = preVerse.get(chaptr - 1);
                chapters.add(stop);

            } catch (IndexOutOfBoundsException err) {
                Toast.makeText(context, "Samahani Kifungu Hiki Hakikupatikana Katika Biblia. Inafungua Biblia...", Toast.LENGTH_LONG).show();
                cannotOpen();
            }

            return chapters;
        }

        // Define a regex pattern to match all Swahili book names and verse numbers
        public SpannableString spannableBibleText(String mafungu) {
            SpannableString spannableString = new SpannableString(mafungu);

            // Define the regex pattern to match simple verse structures
            Pattern versePattern = Pattern.compile(
                    "(Mwanzo|Kutoka|Walawi|Hesabu|Kumbukumbu la Torati|Yoshua|Waamuzi|Ruthu|1 Samweli|2 Samweli|1 Wafalme|2 Wafalme|1 Mambo ya Nyakati|2 Mambo ya Nyakati|Ezra|Nehemia|Esta|Ayubu|Zaburi|Mithali|Mhubiri|Wimbo Ulio Bora|Isaya|Yeremia|Maombolezo|Ezekieli|Danieli|Hosea|Yoeli|Amosi|Obadia|Yona|Mika|Nahumu|Habakuki|Sefania|Hagai|Zekaria|Malaki|Mathayo|Marko|Luka|Yohana|Matendo ya Mitume|Warumi|1 Wakorintho|2 Wakorintho|Wagalatia|Waefeso|Wafilipi|Wakolosai|1 Wathesalonike|2 Wathesalonike|1 Timotheo|2 Timotheo|Tito|Filemoni|Waebrania|Yakobo|1 Petro|2 Petro|1 Yohana|2 Yohana|3 Yohana|Yuda|Ufunuo wa Yohana)\\s\\d+(:\\d+(-\\d+)?(,\\s*\\d+(:\\d+(-\\d+)?)*|,\\s*\\d+)*(\\s*,\\s*\\d+(:\\d+(-\\d+)?)*?)?)?\\s*;"
            );

            Matcher matcher = versePattern.matcher(mafungu);

            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String compVerse = mafungu.substring(start, end);

                // Split the verse into segments based on commas and semicolons
                String[] verseSegments = compVerse.split(";");

                // Iterate over each segment to apply spans
                for (String segment : verseSegments) {
                    segment = segment.trim();
                    if (!segment.isEmpty()) {
                        String[] parts = segment.split(",\\s*");

                        for (String part : parts) {
                            part = part.trim();
                            int partStart = mafungu.indexOf(part, start);
                            int partEnd = partStart + part.length();
                            String fullVerse = segment.trim();

                            // Apply color and underline spans
                            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), // Use context
                                    partStart, partEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new UnderlineSpan(), partStart, partEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Apply clickable span
                            final String verse = fullVerse; // Make the verse effectively final for the inner class
                            spannableString.setSpan(new ClickableSpan() {
                                 @Override
                        public void onClick(@NonNull View widget) {
                            Toast.makeText(context, verse, Toast.LENGTH_SHORT).show();       
                            handleVerseClick(verse, partStart, partEnd);
                        }
                    }, partStart, partEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    return spannableString;
}

private void handleVerseClick(String compVerse, int startIndex, int stopIndex) {
    // Define default values
    String book = "books/mwanzo.txt";
    Integer chapter = 1;
    Integer verse = 1;
    Integer to = 1;

    // Remove duplicated book names
    Pattern duplicatePattern = Pattern.compile("^(\\w+)(\\s\\1)+");
    Matcher duplicateMatcher = duplicatePattern.matcher(compVerse);
    if (duplicateMatcher.find()) {
        compVerse = duplicateMatcher.replaceAll("$1");
    }

    // Trim spaces
    compVerse = compVerse.trim();

    // Split the input into segments based on commas
    String[] segments = compVerse.split("\\s*,\\s*");

    // Attach ":1" to segments without a verse
    for (int i = 0; i < segments.length; i++) {
        String segment = segments[i].trim();
        if (!segment.contains(":") && segment.matches(".*\\d+")) {
            segments[i] = segment + ":1";
        }
    }

    // Join the segments back into the compVerse string
    compVerse = String.join(", ", segments);

    // Initialize regex pattern for verse segments
    Pattern pattern = Pattern.compile("^(.*?\\d*\\s*\\w+)(\\s(\\d+):(\\d+)(?:-(\\d+))?)?$");

    try {
        for (String segment : segments) {
            Matcher matcher = pattern.matcher(segment.trim());

            if (matcher.find()) {
                String currentBook = matcher.group(1).toLowerCase().trim();
                Integer currentChapter = matcher.group(3) != null ? Integer.parseInt(matcher.group(3).trim()) : chapter;
                Integer currentVerse = matcher.group(4) != null ? Integer.parseInt(matcher.group(4).trim()) : 1;
                Integer currentTo = matcher.group(5) != null ? Integer.parseInt(matcher.group(5).trim()) : currentVerse;

                // Check for comma-separated verses
                if (segment.contains(",")) {
                    String[] parts = segment.split(",");
                    for (String part : parts) {
                        Matcher partMatcher = pattern.matcher(part.trim());
                        if (partMatcher.find()) {
                            Integer partTo = partMatcher.group(5) != null ? Integer.parseInt(partMatcher.group(5).trim()) : null;
                            if (partTo != null) {
                                currentTo = Math.max(currentTo, partTo); // Update currentTo with the maximum value
                            }
                        }
                    }
                }

                // Update chapter, verse, and to with the current values
                chapter = currentChapter;
                verse = currentVerse;
                to = currentTo;

                // Handle the case where the book name ends with a number but isn't supposed to
                if (!currentBook.isEmpty() && currentBook.matches(".*\\d$") && !currentBook.matches("^\\d.*")) {
                    currentBook = currentBook.replaceAll("\\d+$", "").trim();
                }

                // Update the book if it's still the default
                if (book.equals("books/mwanzo.txt") && !currentBook.isEmpty()) {
                    book = "books/" + currentBook + ".txt";
                }

                // Construct the full verse reference
                String fullVerse = segment.trim();
                if (!fullVerse.startsWith(currentBook)) {
                    fullVerse = currentBook + " " + fullVerse;
                }

                // Display the extracted information for debugging
                Toast.makeText(context, "Book: " + book + "\nChapter: " + chapter + "\nVerse: " + verse + "\nTo: " + to, Toast.LENGTH_LONG).show();

                // Handle each segment (e.g., create spans, store ranges, etc.)
                // You can add your logic here to process each chapter and verse
            } else {
                // Handle the case where the format does not match
                Toast.makeText(context, "Error parsing verse: " + segment, Toast.LENGTH_SHORT).show();
            }
        }
    } catch (Exception e) {
        Toast.makeText(context, "Error parsing verse: " + compVerse, Toast.LENGTH_LONG).show();
    }

    // Start the Bible_Read activity with the extracted information
    Intent bible = new Intent(context, Bible_Read.class);
    bible.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);        
    ArrayList<Integer> StartStop = bible_Chapters(book, chapter);
    bible.putExtra("chapter", book + " " + chapter);
    bible.putExtra("startVerse", StartStop.get(0));
    bible.putExtra("stopVerse", StartStop.get(1));
    bible.putExtra("bookName", book);
    context.startActivity(bible);
}
 
 private void cannotOpen() {
 	Intent intent = new Intent(context, bible.class);
     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);       
     context.startActivity(intent);   
 }    
    
    
}
}