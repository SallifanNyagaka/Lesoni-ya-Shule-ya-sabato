package com.sal.leseniyashuleyasabato;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sal.leseniyashuleyasabato.R;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import java.lang.reflect.Type;
import java.util.HashMap;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private final List<LessonModels> days;
    private Context context;
    private TextToSpeech textToSpeech;
    String tray = "gjjhggdzxjj";
    public  String teacher = "days";
    String teacherPathName;
    String lessonPathName;
    
    private Map<Integer, List<String>> commentsMap = new HashMap<>();
    
    public Adapter(List<LessonModels> days, Context context, String teacherPathName, String lessonPathName) {
        this.days = days;
        this.context = context;
        this.teacherPathName = teacherPathName;
        this.lessonPathName = lessonPathName;
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

        holder.date.setText(lessonModel.getDate());
        holder.weekDateRange.setText("Wiki hii: " + lessonModel.getWeekRange());
        holder.day_title.setText(lessonModel.getDay_title());

        // Clear previous dynamic views
        holder.paragraphContainer.removeAllViews();

        // Add TextViews and EditTexts dynamically for day_content
        addParagraphsToContainer(
                holder.paragraphContainer,
                lessonModel.getDay_content(),
                position,
                lessonModel.getWeekRange());

        // Add TextViews and EditTexts dynamically for day_question
        addParagraphsToContainer(
                holder.paragraphContainer,
                lessonModel.getDay_question(),
                position,
                lessonModel.getWeekRange());

        holder.share_image.setImageResource(lessonModel.getShare_image());

        // Check if it's Saturday and set the Saturday image visibility
        if (lessonModel.getDateEng().toLowerCase().contains("saturday")) {
            holder.saturday_image.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(lessonModel.getSaturday_image_uri()))
                    .into(holder.saturday_image);
        } else {
            holder.saturday_image.setVisibility(View.GONE);
        }

        holder.share_image.setOnClickListener(
                v -> {
                    String toShare =
                            lessonModel.getDate()
                                    + "\n"
                                    + lessonModel.getWeekRange()
                                    + "\n"
                                    + lessonModel.getDay_title()
                                    + "\n"
                                    + lessonModel.getDay_content()
                                    + "\n"
                                    + lessonModel.getDay_question();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, toShare);
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"));
                });

        holder.ic_tts.setOnClickListener(
                v -> {
                    String textToRead =
                            lessonModel.getDay_title()
                                    + ". "
                                    + lessonModel.getDay_content()
                                    + ". "
                                    + lessonModel.getDay_question();
                    textToSpeech =
                            new TextToSpeech(
                                    context,
                                    status -> {
                                        if (status == TextToSpeech.SUCCESS) {
                                            textToSpeech.setLanguage(new Locale("sw"));
                                            textToSpeech.speak(
                                                    textToRead,
                                                    TextToSpeech.QUEUE_FLUSH,
                                                    null,
                                                    null);
                                        }
                                    });
                });
        
        
        // Set an OnCheckedChangeListener to handle toggle button state changes
        holder.maongezi_mwalimu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    teacher = "teacher";  // Switch the collection name to "teacher"
                    fetchNewDataFromFirestore(teacherPathName);  // Fetch new data from Firestore
                } else {
                    teacher = "days";
                    fetchNewDataFromFirestore(lessonPathName);
                    holder.maongezi_mwalimu.setText("Rudi kwa Lesoni");        
                }
            }
        });
    }
    

    @Override
    public int getItemCount() {
        return this.days.size();
    }
    
    private void fetchNewDataFromFirestore(String pathName) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference lessonsRef = db.collection(pathName); // Use the predefined pathName

    // Get all documents in the collection
    lessonsRef.get().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            days.clear(); // Clear the existing list before adding new data
            for (QueryDocumentSnapshot document : task.getResult()) {
                // Check if the document exists
                if (document.exists()) {
                    // Retrieve data from the document
                    String day = document.getString("date");
                    String dateEng = document.getString("dateEng");
                    String weekRange = document.getString("weekDateRange");
                    int shareImage = R.drawable.share_today; // Replace with appropriate drawable resource if necessary
                    String day_title = document.getString("title");
                    String day_content = document.getString("content");
                    String day_question = document.getString("question");
                    String image_Uri = document.getString("image_url");

                    // Create a new LessonModels object and add it to the list
                    days.add(new LessonModels(day, dateEng, weekRange, shareImage, day_title, day_content, day_question, image_Uri));
                } else {
                    Log.w("Firestore", "Document does not exist: " + document.getId());
                }
            }
            notifyDataSetChanged(); // Notify the adapter that data has changed
        } else {
            Log.w("Firestore", "Error getting documents: ", task.getException());
        }
    });
}

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final TextView date;
        private final TextView weekDateRange;
        // private final TextView day_content;
        // private final TextView day_question;
        private final LinearLayout paragraphContainer;
        private final TextView day_title;
        private final ImageView share_image;
        private final ImageView saturday_image, ic_tts;
        private final ToggleButton maongezi_mwalimu;

        public ViewHolder(View itemView, Context context) { // Constructor receives the context
            super(itemView);
            this.context = context;
            this.date = itemView.findViewById(R.id.date);
            this.weekDateRange = itemView.findViewById(R.id.weekRange);
            this.day_title = itemView.findViewById(R.id.day_title);
            // this.day_content = itemView.findViewById(R.id.day_content);
            this.paragraphContainer = itemView.findViewById(R.id.paragraph_container);
            // this.day_question = itemView.findViewById(R.id.day_question);
            this.share_image = itemView.findViewById(R.id.day_share);
            this.ic_tts = itemView.findViewById(R.id.ic_tts);
            this.saturday_image = itemView.findViewById(R.id.saturday_image);
            this.maongezi_mwalimu = itemView.findViewById(R.id.maongezi_mwalimu);
        }
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
            Toast.makeText(
                            context,
                            "Kifungu hakikupatikana.\n Inafungua Biblia...",
                            Toast.LENGTH_LONG)
                    .show(); // Use context
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
            Toast.makeText(
                            context,
                            "Samahani Kifungu Hiki Hakikupatikana Katika Biblia. Inafungua Biblia...",
                            Toast.LENGTH_LONG)
                    .show();
            cannotOpen();
        }

        return chapters;
    }

    // Define a regex pattern to match all Swahili book names and verse numbers
    public SpannableString spannableBibleText(String mafungu) {
        SpannableString spannableString = new SpannableString(mafungu);

        // Define the regex pattern to match simple verse structures
        Pattern versePattern =
                Pattern.compile(
                        "(Mwanzo|Kutoka|Mambo ya Walawi|Hesabu|Kumbukumbu la Torati|Yoshua|Waamuzi|Ruthu|1 Samweli|2 Samweli|1 Wafalme|2 Wafalme|1 Mambo ya Nyakati|2 Mambo ya Nyakati|Ezra|Nehemia|Esta|Ayubu|Zaburi|Mithali|Mhubiri|Wimbo Ulio Bora|Isaya|Yeremia|Maombolezo|Ezekieli|Danieli|Hosea|Yoeli|Amosi|Obadia|Yona|Mika|Nahumu|Habakuki|Sefania|Hagai|Zekaria|Malaki|Mathayo|Marko|Luka|Yohana|Matendo ya Mitume|Warumi|1 Wakorintho|2 Wakorintho|Wagalatia|Waefeso|Wafilipi|Wakolosai|1 Wathesalonike|2 Wathesalonike|1 Timotheo|2 Timotheo|Tito|Filemoni|Waebrania|Yakobo|1 Petro|2 Petro|1 Yohana|2 Yohana|3 Yohana|Yuda|Ufunuo wa Yohana)\\s\\d+(:\\d+(-\\d+)?(,\\s*\\d+(:\\d+(-\\d+)?)*|,\\s*\\d+)*(\\s*,\\s*\\d+(:\\d+(-\\d+)?)*?)?)?\\s*;");

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
                        spannableString.setSpan(
                                new ForegroundColorSpan(Color.BLUE), // Use context
                                partStart,
                                partEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(
                                new UnderlineSpan(),
                                partStart,
                                partEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Apply clickable span
                        final String verse =
                                fullVerse; // Make the verse effectively final for the inner class
                        spannableString.setSpan(
                                new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Toast.makeText(context, verse, Toast.LENGTH_SHORT).show();
                                        handleVerseClick(verse, partStart, partEnd);
                                    }
                                },
                                partStart,
                                partEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        return spannableString;
    }
    
    private SpannableString applyBoldTerms(String content) {
    SpannableString spannableString = new SpannableString(content);

    String[] termsToBold = {
        "Sehemu ya III: Matumizi maishani", "Sehemu ya II: Ufafanuzi", "Muhtasari", 
        "Maswali ya Kujadili", "Jifunze zaidi", "Kisa cha ndani", "Somo la Juma Hili",
        "Fungu la Kukariri", "Sabato Mchana", "Soma", "Fungu kuu", "Kiini cha somo"
    };

    // Iterate over each term to bold
    for (String term : termsToBold) {
        Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    return spannableString;
}

    // Generate unique key for saving comments based on week ID, position, and paragraph index
    private String generateCommentKey(String weekId, int position, int paragraphIndex) {
        return "comment_" + weekId + "_" + position + "_" + paragraphIndex;
    }

    private void addParagraphsToContainer(
            LinearLayout container, String content, int position, String weekId) {
        String[] paragraphs = content.split("\\n\\n");
        String userEmail = getUserEmail(); // Method to get user email from SharedPreferences

        for (int i = 0; i < paragraphs.length; i++) {
            final int paragraphIndex = i;
            String paragraph = paragraphs[paragraphIndex];
            
            SpannableString spannableParagraph = applyBoldTerms(paragraph);
        
        // Then, apply the Bible verse spans
        SpannableString finalSpannable = spannableBibleText(spannableParagraph.toString());

            // Create and configure TextView for the paragraph
            TextView tvParagraph = new TextView(container.getContext());
            tvParagraph.setText(spannableBibleText(finalSpannable.toString()));
            tvParagraph.setTextSize(16);
            tvParagraph.setPadding(4, 8, 4, 8);
            tvParagraph.setTextIsSelectable(true); // Allow text selection
            applyUserHighlights(tvParagraph, weekId, position, paragraphIndex);

            // Enable long-press to activate contextual actions
            tvParagraph.setCustomSelectionActionModeCallback(
                    new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            // Inflate the menu with "Highlight" and "Share" actions
                            MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(R.menu.text_selection_menu, menu);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            int min = 0;
                            int max = tvParagraph.getText().length();
                            // Check if the selected text is already highlighted
                            boolean isHighlighted = checkIfHighlighted(tvParagraph, min, max);

                            // Show or hide the "Remove Highlight" option based on the highlight
                            // status
                            MenuItem removeHighlightItem =
                                    menu.findItem(R.id.menu_remove_highlight);
                            if (isHighlighted) {
                                removeHighlightItem.setVisible(true);
                            } else {
                                removeHighlightItem.setVisible(false);
                            }
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            int min = 0;
                            int max = tvParagraph.getText().length();
                            if (tvParagraph.isFocused()) {
                                final int selStart = tvParagraph.getSelectionStart();
                                final int selEnd = tvParagraph.getSelectionEnd();
                                min = Math.max(0, Math.min(selStart, selEnd));
                                max = Math.max(0, Math.max(selStart, selEnd));
                            }

                            final CharSequence selectedText =
                                    tvParagraph.getText().subSequence(min, max);

                            if (item.getItemId() == R.id.menu_highlight) {
                                // Show color selection dialog
                                showColorPickerDialog(
                                        tvParagraph, weekId, position, paragraphIndex, min, max);
                                mode.finish(); // Close action mode
                                return true;
                            } else if (item.getItemId() == R.id.menu_remove_highlight) {
                                // Remove the highlight
                                removeHighlight(weekId, position, paragraphIndex, tvParagraph);
                                mode.finish();
                                return true;
                            } else if (item.getItemId() == R.id.menu_share) {
                                // Share the selected text
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, selectedText.toString());
                                container
                                        .getContext()
                                        .startActivity(
                                                Intent.createChooser(shareIntent, "Share Text"));
                                mode.finish();
                                return true;
                            }

                            return false;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {}
                    });

            // Add the paragraph TextView to the container
            container.addView(tvParagraph);

            // Create and configure EditText for comments
            EditText etComment = new EditText(container.getContext());
            etComment.setHint("Ongeza maoni yako...");
            etComment.setVisibility(View.GONE);
            etComment.setBackgroundColor(Color.TRANSPARENT);
            etComment.setTextColor(Color.parseColor("#9E9E9E"));
            etComment.setTypeface(null, Typeface.ITALIC);

            // Restore and display saved comment if available
            String savedComment = getSavedComment(weekId, position, paragraphIndex);
            if (savedComment != null) {
                etComment.setText(savedComment);
                etComment.setVisibility(View.VISIBLE);
            }

            tvParagraph.setMovementMethod(LinkMovementMethod.getInstance());

            // Add EditText to the container
            container.addView(etComment);

            // Retrieve comments from Firestore for registered users
            retrieveComments(weekId, position, paragraphIndex, etComment);

            // Toggle EditText visibility with animation on TextView click
            tvParagraph.setOnClickListener(
                    v -> {
                        if (etComment.getVisibility() == View.GONE) {
                            etComment.setAlpha(0f);
                            etComment.setVisibility(View.VISIBLE);
                            etComment.animate().alpha(1f).setDuration(300).start();
                        } else {
                            etComment
                                    .animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .withEndAction(
                                            () -> {
                                                etComment.setVisibility(View.GONE);
                                            })
                                    .start();
                        }
                    });

            // Save comment on text change
            etComment.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(
                                CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(
                                CharSequence s, int start, int before, int count) {
                            String comment = s.toString();
                            saveComment(weekId, position, paragraphIndex, comment);

                            // Save to Firestore for registered users if online
                            if (!userEmail.equals("Haipo/ Non Existent")) {
                                if (isConnected()) {
                                    saveCommentToFirestore(
                                            weekId, position, paragraphIndex, userEmail, comment);
                                } else {
                                    saveOfflineComment(
                                            userEmail, weekId, position, paragraphIndex, comment);
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });
        }
    }

    private boolean checkIfHighlighted(TextView textView, int start, int end) {
        BackgroundColorSpan[] spans =
                ((Spannable) textView.getText()).getSpans(start, end, BackgroundColorSpan.class);
        return spans.length > 0;
    }

    // Method to show color picker dialog
    private void showColorPickerDialog(
        TextView tvParagraph,
        String weekId,
        int position,
        int paragraphIndex,
        int start,
        int end) {
    // Define the colors with 50% opacity
    final int[] colors = {
        Color.argb(128, 255, 0, 0), // Red
        Color.argb(128, 0, 255, 0), // Green
        Color.argb(128, 0, 0, 255), // Blue
        Color.argb(128, 255, 255, 0), // Yellow
        Color.argb(128, 255, 165, 0), // Orange
        Color.argb(128, 128, 0, 128), // Purple
        Color.argb(128, 255, 192, 203) // Pink
    };

    // Create an AlertDialog to show color options
    AlertDialog.Builder builder = new AlertDialog.Builder(tvParagraph.getContext());
    builder.setTitle("Choose a highlight color");

    // Create a layout for the dialog
    LinearLayout layout = new LinearLayout(tvParagraph.getContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    // Create the AlertDialog and store a reference
    AlertDialog dialog = builder.create();

    // Create color buttons
    for (int color : colors) {
        View colorView = new View(tvParagraph.getContext());
        colorView.setBackgroundColor(color);
        colorView.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
        colorView.setOnClickListener(
                v -> {
                    // Highlight the selected text with the chosen color
                    Spannable spannable = (Spannable) tvParagraph.getText();
                    spannable.setSpan(
                            new BackgroundColorSpan(color),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Save the highlight with the chosen color
                    saveHighlight(weekId, position, paragraphIndex, start, end, color);

                    if (isConnected()) {
                        saveHighlightToFirestore(
                                weekId,
                                position,
                                paragraphIndex,
                                getUserEmail(),
                                start,
                                end,
                                color);
                    } else {
                        saveOfflineHighlight(
                                getUserEmail(),
                                weekId,
                                position,
                                paragraphIndex,
                                start,
                                end,
                                color);
                    }

                    // Dismiss the dialog after color selection
                    dialog.dismiss();
                });
        layout.addView(colorView);
    }

    dialog.setView(layout);
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, which) -> dialog.dismiss());
    dialog.show();
}
    
private void saveOfflineHighlight(
        String userEmail,
        String weekId,
        int position,
        int paragraphIndex,
        int start,
        int end,
        int color) {
    SharedPreferences prefs = context.getSharedPreferences("OfflineHighlights", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    
    // Remove any existing highlight
    String existingKey = generateHighlightKey(weekId, position, paragraphIndex) + "_" + userEmail;
    editor.remove(existingKey);
    
    // Save new highlight
    String key = existingKey; // Use the same key
    String highlightData = start + "," + end + "," + color;
    editor.putString(key, highlightData);
    editor.apply();
}
    private void saveHighlight(String weekId, int position, int paragraphIndex, int start, int end, int color) {
    SharedPreferences prefs = context.getSharedPreferences("OfflineHighlights", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    
    // Remove any existing highlight
    String key = generateHighlightKey(weekId, position, paragraphIndex);
    editor.remove(key);
    
    // Save new highlight
    editor.putString(key, start + "," + end + "," + color);
    editor.apply();
}
    

    // Generate unique highlight key based on weekId, position, and paragraphIndex
    private String generateHighlightKey(String weekId, int position, int paragraphIndex) {
        return "highlight_" + weekId + "_" + position + "_" + paragraphIndex;
    }

    // Sync offline highlights to Firestore
    public void syncOfflineHighlights() {
        SharedPreferences prefs =
                context.getSharedPreferences("OfflineHighlights", Context.MODE_PRIVATE);
        Map<String, ?> allHighlights = prefs.getAll();
        for (Map.Entry<String, ?> entry : allHighlights.entrySet()) {
            String key = entry.getKey();
            String highlightData = (String) entry.getValue();

            // Extract data from the key and value
            String[] keyParts = key.split("_");
            if (keyParts.length >= 5) {
                String weekId = keyParts[1];
                int position = Integer.parseInt(keyParts[2]);
                int paragraphIndex = Integer.parseInt(keyParts[3]);
                String userEmail = keyParts[4];

                // Extract start, end, and color from highlightData
                String[] dataParts = highlightData.split(",");
                int start = Integer.parseInt(dataParts[0]);
                int end = Integer.parseInt(dataParts[1]);
                int color = Integer.parseInt(dataParts[2]);

                // Save the highlight to Firestore
                saveHighlightToFirestore(
                        weekId, position, paragraphIndex, userEmail, start, end, color);
            }
        }

        //Clear offline highlights after sync but no need
        //prefs.edit().clear().apply();
    }

    public static String getDayOfWeek(int day) {
        switch (day) {
            case 0:
                return "Saturday";
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            default:
                return "Invalid"; // Handle invalid inputs
        }
    }

    // Save highlight to Firestore
    private void saveHighlightToFirestore(
            String weekId,
            int position,
            int paragraphIndex,
            String userEmail,
            int start,
            int end,
            int color) {
        Map<String, Object> highlightData = new HashMap<>();
        highlightData.put("start", start);
        highlightData.put("end", end);
        highlightData.put("color", color);
        highlightData.put("userEmail", userEmail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("highlights")
                .document(weekId)
                .collection(getDayOfWeek(position))
                .document("paragraph_" + paragraphIndex)
                .collection("user_highlights")
                .document(userEmail)
                .set(highlightData, SetOptions.merge());
    }

    private void applyUserHighlights(
            TextView tvParagraph, String weekId, int position, int paragraphIndex) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = getUserEmail(); // Method to get user email from SharedPreferences
        Spannable spannable = (Spannable) tvParagraph.getText();

        if (isConnected()) {
            // Fetch highlights from Firestore
            final Spannable fin = spannable;
            db.collection("highlights")
                    .document(weekId)
                    .collection(getDayOfWeek(position))
                    .document("paragraph_" + paragraphIndex)
                    .collection("user_highlights")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(
                            documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    int start = documentSnapshot.getLong("start").intValue();
                                    int end = documentSnapshot.getLong("end").intValue();
                                    int color = documentSnapshot.getLong("color").intValue();

                                    fin.setSpan(
                                            new BackgroundColorSpan(color),
                                            start,
                                            end,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            });
        } else {
            // Retrieve highlights from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("OfflineHighlights", Context.MODE_PRIVATE);
    String key = generateHighlightKey(weekId, position, paragraphIndex);
    
    // Check if there is a saved highlight in SharedPreferences
    String highlightData = prefs.getString(key, null);
    
    if (highlightData != null) {
        // Parse the saved data (start, end, color)
        String[] parts = highlightData.split(",");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);
        int color = Integer.parseInt(parts[2]);

        // Apply the highlight to the TextView
        spannable = (Spannable) tvParagraph.getText();
        spannable.setSpan(new BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    // Method to remove highlight from a paragraph
    private void removeHighlight(String weekId, int position, int paragraphIndex, TextView tvParagraph) {
    Spannable spannable = (Spannable) tvParagraph.getText();
    BackgroundColorSpan[] spans = spannable.getSpans(0, tvParagraph.getText().length(), BackgroundColorSpan.class);

    // Remove the highlight by removing BackgroundColorSpans
    for (BackgroundColorSpan span : spans) {
        spannable.removeSpan(span);
    }

    if (isConnected()) {
        // Remove the highlight from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = getUserEmail(); // Retrieve user's email from SharedPreferences
        db.collection("highlights")
                .document(weekId)
                .collection(getDayOfWeek(position))
                .document("paragraph_" + paragraphIndex)
                .collection("user_highlights")
                .document(userEmail)
                .delete();
    } else {
        // Store highlight removal for future sync in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("RemovedHighlightsSync", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store the details of the removal for later sync
        String removalKey = generateHighlightKey(weekId, position, paragraphIndex);
        editor.putString(removalKey, weekId + "," + position + "," + paragraphIndex);
        editor.apply();

        // Also remove from offline highlights
        SharedPreferences offlinePrefs = context.getSharedPreferences("OfflineHighlights", Context.MODE_PRIVATE);
        SharedPreferences.Editor offlineEditor = offlinePrefs.edit();
        String offlineKey = generateHighlightKey(weekId, position, paragraphIndex);
        offlineEditor.remove(offlineKey);
        offlineEditor.apply();
    }
}
    
public void syncRemovedHighlights() {
    if (isConnected()) {
        SharedPreferences prefs = context.getSharedPreferences("RemovedHighlightsSync", Context.MODE_PRIVATE);
        Map<String, ?> removals = prefs.getAll();

        if (!removals.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userEmail = getUserEmail();

            for (Map.Entry<String, ?> entry : removals.entrySet()) {
                String[] details = entry.getValue().toString().split(",");
                String weekId = details[0];
                int position = Integer.parseInt(details[1]);
                int paragraphIndex = Integer.parseInt(details[2]);

                // Delete highlight from Firestore
                db.collection("highlights")
                        .document(weekId)
                        .collection(getDayOfWeek(position))
                        .document("paragraph_" + paragraphIndex)
                        .collection("user_highlights")
                        .document(userEmail)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // Remove from shared preferences after successful sync
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove(entry.getKey());
                            editor.apply();
                        });
            }
        }
    }
}
    
    // Method to save offline comments in SharedPreferences when there's no connectivity
    private void saveOfflineComment(
            String userEmail, String weekId, int position, int paragraphIndex, String comment) {
        SharedPreferences prefs =
                context.getSharedPreferences("OfflineComments", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String key = generateCommentKey(weekId, position, paragraphIndex) + "_" + userEmail;
        editor.putString(key, comment);
        editor.apply();
    }

    // Method to synchronize offline comments with Firestore when connectivity is restored
    public void syncOfflineComments() {
        SharedPreferences prefs =
                context.getSharedPreferences("OfflineComments", Context.MODE_PRIVATE);
        Map<String, ?> allComments = prefs.getAll();
        for (Map.Entry<String, ?> entry : allComments.entrySet()) {
            String key = entry.getKey();
            String comment = (String) entry.getValue();

            // Extract data from the key
            String[] keyParts = key.split("_");
            if (keyParts.length >= 5) { // Check that the key contains enough parts
                String weekId = keyParts[1];
                int position = Integer.parseInt(keyParts[2]);
                int paragraphIndex = Integer.parseInt(keyParts[3]);
                String userEmail = keyParts[4];

                // Save the comment to Firestore
                saveCommentToFirestore(weekId, position, paragraphIndex, userEmail, comment);
            }
        }

        // Clear the offline comments after synchronization
        prefs.edit().clear().apply();
    }

    // Check connectivity status
    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Method to get user email from SharedPreferences
    private String getUserEmail() {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return prefs.getString(
                "userEmail", "Haipo/ Non Existent"); // Default value for unregistered users
    }

    // Efficiently save comment data using SharedPreferences
    private void saveComment(String weekId, int position, int paragraphIndex, String comment) {
        try {
            SharedPreferences prefs =
                    context.getSharedPreferences("CommentsPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String key = generateCommentKey(weekId, position, paragraphIndex);
            editor.putString(key, comment);
            editor.apply(); // Use apply() for asynchronous saving
        } catch (Exception e) {
            Toast.makeText(context, "Error saving comment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // Efficiently retrieve saved comments from SharedPreferences
    private String getSavedComment(String weekId, int position, int paragraphIndex) {
        try {
            SharedPreferences prefs =
                    context.getSharedPreferences("CommentsPrefs", Context.MODE_PRIVATE);
            String key = generateCommentKey(weekId, position, paragraphIndex);
            return prefs.getString(key, null);
        } catch (Exception e) {
            Toast.makeText(
                            context,
                            "Error retrieving comment: " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
    }

    // Save comment to Firestore for registered users
    private void saveCommentToFirestore(
            String weekId, int position, int paragraphIndex, String userEmail, String comment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String commentId = generateCommentKey(weekId, position, paragraphIndex);

        // Create a Comment object
        CommentData commentData = new CommentData(userEmail, comment);

        // Save comment under the appropriate document in Firestore
        db.collection("User_Comments") // Adjust to your structure
                .document(weekId) // Use the current week's ID
                .collection(userEmail) // Assuming there's a subcollection for comments
                .document(commentId) // Use the unique comment ID
                .set(commentData)
                .addOnSuccessListener(aVoid -> Log.e("Comment saved successfully", "ok"))
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                                context,
                                                "Error saving comment: " + e.getMessage(),
                                                Toast.LENGTH_SHORT)
                                        .show());
    }

    // Retrieve comments from Firestore for registered users
    private void retrieveComments(
            String weekId, int position, int paragraphIndex, EditText etComment) {
        String userEmail = getUserEmail(); // Get the user's email

        // Check if the user is registered
        if (!userEmail.equals("Haipo/ Non Existent")) {
            // Retrieve comments from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String commentId = generateCommentKey(weekId, position, paragraphIndex);

            db.collection("User_Comments")
                    .document(weekId)
                    .collection(userEmail)
                    .document(commentId)
                    .get()
                    .addOnSuccessListener(
                            documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    CommentData commentData =
                                            documentSnapshot.toObject(CommentData.class);
                                    if (commentData != null) {
                                        // Update UI with the retrieved comment
                                        String comment = commentData.getComment();
                                        etComment.setText(comment);
                                        etComment.setVisibility(View.VISIBLE);
                                        Log.e("comment found", "ok");
                                    }
                                } else {
                                    Log.e("No comment found", "No Comment");
                                }
                            })
                    .addOnFailureListener(
                            e -> {
                                Log.e("Error retrieving comment", "No Comment");
                                ;
                            });
        }
    }

    // CommentData class for storing comment information
    public static class CommentData {
        private String userEmail;
        private String comment;

        // Default constructor required for Firestore
        public CommentData() {}

        // Constructor to initialize comment data
        public CommentData(String userEmail, String comment) {
            this.userEmail = userEmail;
            this.comment = comment;
        }

        // Getters and setters
        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
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
                    Integer currentChapter =
                            matcher.group(3) != null
                                    ? Integer.parseInt(matcher.group(3).trim())
                                    : chapter;
                    Integer currentVerse =
                            matcher.group(4) != null
                                    ? Integer.parseInt(matcher.group(4).trim())
                                    : 1;
                    Integer currentTo =
                            matcher.group(5) != null
                                    ? Integer.parseInt(matcher.group(5).trim())
                                    : currentVerse;

                    // Check for comma-separated verses
                    if (segment.contains(",")) {
                        String[] parts = segment.split(",");
                        for (String part : parts) {
                            Matcher partMatcher = pattern.matcher(part.trim());
                            if (partMatcher.find()) {
                                Integer partTo =
                                        partMatcher.group(5) != null
                                                ? Integer.parseInt(partMatcher.group(5).trim())
                                                : null;
                                if (partTo != null) {
                                    currentTo =
                                            Math.max(
                                                    currentTo,
                                                    partTo); // Update currentTo with the maximum
                                    // value
                                }
                            }
                        }
                    }

                    // Update chapter, verse, and to with the current values
                    chapter = currentChapter;
                    verse = currentVerse;
                    to = currentTo;

                    // Handle the case where the book name ends with a number but isn't supposed to
                    if (!currentBook.isEmpty()
                            && currentBook.matches(".*\\d$")
                            && !currentBook.matches("^\\d.*")) {
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
                    Toast.makeText(
                                    context,
                                    "Book: "
                                            + book
                                            + "\nChapter: "
                                            + chapter
                                            + "\nVerse: "
                                            + verse
                                            + "\nTo: "
                                            + to,
                                    Toast.LENGTH_LONG)
                            .show();

                    // Handle each segment (e.g., create spans, store ranges, etc.)
                    // You can add your logic here to process each chapter and verse
                } else {
                    // Handle the case where the format does not match
                    Toast.makeText(context, "Error parsing verse: " + segment, Toast.LENGTH_SHORT)
                            .show();
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
        bible.putExtra("verse", verse);
        bible.putExtra("to", to);
        bible.putExtra("bookName", book);
        context.startActivity(bible);
    }

    private void cannotOpen() {
        Intent intent = new Intent(context, bible.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
