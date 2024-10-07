package com.sal.leseniyashuleyasabato;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.CropImageView;
import java.text.SimpleDateFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.sal.leseniyashuleyasabato.admin;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class admin extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;
    private static final int REQUEST_IMAGE_SELECT_SATURDAY = 3; //request code for Saturday image
    private static final int REQUEST_IMAGE_SELECT_QUARTER = 4;
    private static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    
    private boolean isSaturday;
    private boolean isquarterImage;
    private boolean isContent;
    TextInputEditText contentInput;
    
    private TextInputEditText contentEditText;
    private TextInputEditText QuarterIntroduction;
    private TextInputEditText quarterTitle;
    private Spinner quarterSpinner;
    private TextInputEditText questionEditText;
    private TextInputEditText titleEditText;
    private String currentDateString;
    private String currentDateStringEnglish;
    private String weekContentRange;
    private Integer selectedDay;
    private String selectedFriday;
    private Integer selectedMonth;
    private String selectedQuarter;
    private String selectedSaturday;
    private Spinner weekSpinner;
    private String selectedWeek;
    private Integer selectedWeekDay;
    private Integer selectedYear;
    private Calendar calender = Calendar.getInstance();
    
    private ImageView saturdayImageView, quarter_image_view;
    private Button selectImageButton, select_QuarterImage_button, upload_teacher;
    
    private Uri imageUri, QURI;
    private FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
    private final String FIELD_TITLE = "title";
    private final String FIELD_DATE = "date";
    private final String FIELD_URI = "image_url";
    private final String FIELD_CONTENT = FirebaseAnalytics.Param.CONTENT;
    private final String FIELD_QUESTION = "question";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        Button datePickerButton = findViewById(R.id.date_picker);
        Button uploadButton = findViewById(R.id.upload);
        final TextView uploadStatusTextView = findViewById(R.id.uploader_status);
        this.titleEditText = findViewById(R.id.title);
        this.QuarterIntroduction = findViewById(R.id.QuarterIntroduction);
        this.quarterTitle = findViewById(R.id.QuarterTitle);
        this.contentEditText = findViewById(R.id.content);
        this.questionEditText = findViewById(R.id.day_question);
        this.contentInput = findViewById(R.id.content);
        this.quarterSpinner = findViewById(R.id.quarter);
        this.weekSpinner = findViewById(R.id.week);
        this.saturdayImageView = findViewById(R.id.saturday_image_view);
        this.quarter_image_view = findViewById(R.id.quarter_image_view);
        this.selectImageButton = findViewById(R.id.select_image_button);
        this.select_QuarterImage_button = findViewById(R.id.select_QuarterImage_button);
        this.upload_teacher = findViewById(R.id.upload_teacher);
        Button quarterTitleUpload = findViewById(R.id.quarterTitleupload);
        
        quarterTitleUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final DocumentReference quarterRef = firestoreDatabase.collection("quarters_"+calender.get(Calendar.YEAR))
                .document(selectedQuarter);
                final Map<String, Object> qter = new HashMap<>();
                String quarterTitleInput = quarterTitle.getText().toString(); 
                if(quarterTitleInput == null || QuarterIntroduction.getText().toString() == null){
                    Toast.makeText(getApplicationContext(), "Please fill in  quarter Title Introduction and select Image first...", Toast.LENGTH_SHORT).show();
                }else{
                    qter.put("QuarterTitle", quarterTitleInput);
                    qter.put("quarterIntroduction", QuarterIntroduction.getText().toString());
                    uploadSaturdayImage(quarterRef, uploadStatusTextView, QURI);        
                    quarterRef.set(qter).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Quarter Title upload success", Toast.LENGTH_LONG).show();
                        }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Quarter Title upload Fail", Toast.LENGTH_LONG).show();
                    }
                });
                }       
                
            } 
        });
        
    
        
        ArrayAdapter<CharSequence> quartersAdapter = ArrayAdapter.createFromResource(this, R.array.quarter, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        quartersAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> weeksAdapter = ArrayAdapter.createFromResource(this, R.array.week, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        weeksAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        this.quarterSpinner.setAdapter(quartersAdapter);
        this.quarterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                admin.this.selectedQuarter = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        this.weekSpinner.setAdapter(weeksAdapter);
        this.weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                admin.this.selectedWeek = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        
        
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                admin.this.uploadContent(uploadStatusTextView);
            }
        });
        
        upload_teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadTeacherContent(uploadStatusTextView);
            }
        });

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                admin.this.showDatePicker(view);
            }
        });
        
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSaturday = true;
                isContent = false;
                isquarterImage = false;        
                showImageSourceDialog();
            }
        });
        
        contentInput.setOnTouchListener((v, event) -> {
            Drawable drawableEnd = contentInput.getCompoundDrawables()[2];
            if (drawableEnd != null && event.getAction() == event.ACTION_UP) {
                if (event.getRawX() >= (contentInput.getRight() - drawableEnd.getBounds().width())) {
                    isSaturday = false; 
                    isContent = true;
                    isquarterImage = false;
                    showImageSourceDialog();
                    return true;
                }
            }
            return false;
        });
        
        select_QuarterImage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSaturday = false;  
                isContent = false;
                isquarterImage = true;        
                showImageSourceDialog();
            }
        });
        
    
    }
    
   /* private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT_SATURDAY);
    }*/

    
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");

        String[] options = {"Gallery", "Camera"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // User chose Gallery
                pickImageFromGallery();
            } else if (which == 1) {
                // User chose Camera
                takePhotoWithCamera();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void pickImageFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_SELECT);
    }

    private void takePhotoWithCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle the error
                Toast.makeText(this, "Error creating file: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageUri = Uri.fromFile(image);
        return image;
    }

    @Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
        if (requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            QURI = resultUri;    
            imageUri = resultUri;
            if (resultUri != null) {
                try {
                    Bitmap croppedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    // Recognize text only for content images
                    if (isContent) {
                        recognizeTextFromImage(croppedBitmap);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_IMAGE_SELECT && data != null) {
            Uri selectedImageUri = data.getData();
            imageUri = selectedImageUri;    
            QURI = selectedImageUri;    
            if (selectedImageUri != null) {
                // Decide whether to crop or directly load the image
                if (isContent) {
                    startCrop(selectedImageUri);
                } else {
                    loadImageWithoutCropping(selectedImageUri);
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (imageUri != null) {
                // Decide whether to crop or directly load the image
                if (isContent) {
                    startCrop(imageUri);
                } else {
                    loadImageWithoutCropping(imageUri);
                }
            }
        }
    } else if (resultCode == UCrop.RESULT_ERROR) {
        final Throwable cropError = UCrop.getError(data);
        if (cropError != null) {
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
    
 
 private void loadImageWithoutCropping(Uri uri) {
    try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

        if (isSaturday) {
            saturdayImageView.setVisibility(View.VISIBLE);
            saturdayImageView.setImageBitmap(bitmap);
        } else if (isquarterImage) {
            quarter_image_view.setImageBitmap(bitmap);
        }
    } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
    }
}   
    
    
private void startCrop(Uri uri) {
    UCrop.Options options = new UCrop.Options();
    options.setFreeStyleCropEnabled(true);    
    options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));

    UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg")))
            .withOptions(options)
            .withAspectRatio(1, 1)
            .start(this);
}
    
    
    private void recognizeTextFromImage(Bitmap imageBitmap) {
    InputImage image = InputImage.fromBitmap(imageBitmap, 0);
    TextRecognizerOptions options = new TextRecognizerOptions.Builder().build();    
    TextRecognizer recognizer = TextRecognition.getClient(options);

    recognizer.process(image)
        .addOnSuccessListener(result -> {
            StringBuilder resultText = new StringBuilder();
            for (Text.TextBlock block : result.getTextBlocks()) {
                resultText.append(block.getText()).append("\n");
            }
            contentInput.setText(resultText.toString());
        })
        .addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Text recognition failed", Toast.LENGTH_SHORT).show();
            Log.e("TextRecognition", "Error: ", e);
        });
}
    
 
 private void uploadContent(final TextView statusTextView) {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String question = questionEditText.getText().toString();
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date);

        if (selectedQuarter == null || selectedWeek == null || currentDateString == null) {
            statusTextView.setText("Please select quarter, week, and date.");
            return;
        }

        if (title.isEmpty() || content.isEmpty() || question.isEmpty()) {
            statusTextView.setText("Please fill in all fields.");
            return;
        }
        
        final Map<String, Object> dayContent = new HashMap<>();
        dayContent.put(FIELD_TITLE, title);
        dayContent.put(FIELD_DATE, currentDateString);
        dayContent.put(FIELD_CONTENT, content);
        dayContent.put(FIELD_QUESTION, question);
        dayContent.put(FIELD_URI, "none");
        dayContent.put("timeStamp", timestamp);
        dayContent.put("dateEng", currentDateStringEnglish);
        dayContent.put("weekDateRange", weekContentRange);

        final DocumentReference weekRef = firestoreDatabase.collection("quarters_"+calender.get(Calendar.YEAR))
                .document(selectedQuarter).collection("weeks").document(selectedWeek);
        
        if(calender.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            final Map<String, Object> wk_Title = new HashMap<>();
            wk_Title.put("week_Title", title);
            wk_Title.put("timeStamp", timestamp);
            weekRef.set(wk_Title);
        }
        
        
        weekRef.collection("days").document(currentDateString)
                .set(dayContent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        statusTextView.setText("Content uploaded successfully.");
                        Toast.makeText(getApplicationContext(), "content upload success", Toast.LENGTH_SHORT).show();
                        // If it's Saturday, upload the image as well
                        if (imageUri != null && calender.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            uploadSaturdayImage(weekRef.collection("days").document(currentDateString), statusTextView, imageUri);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "content upload failed", Toast.LENGTH_SHORT).show();
                        statusTextView.setText("Failed to upload content: " + e.getMessage());
                    }
                });
    }
    
    
    private void uploadTeacherContent(final TextView statusTextView) {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String question = questionEditText.getText().toString();
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date);

        if (selectedQuarter == null || selectedWeek == null || currentDateString == null) {
            statusTextView.setText("Please select quarter, week, and date.");
            return;
        }

        if (title.isEmpty() || content.isEmpty() || question.isEmpty()) {
            statusTextView.setText("Please fill in all fields.");
            return;
        }
        
        final Map<String, Object> dayContent = new HashMap<>();
        dayContent.put(FIELD_TITLE, title);
        dayContent.put(FIELD_DATE, currentDateString);
        dayContent.put(FIELD_CONTENT, content);
        dayContent.put(FIELD_QUESTION, question);
        dayContent.put(FIELD_URI, "none");
        dayContent.put("timeStamp", timestamp);
        dayContent.put("dateEng", currentDateStringEnglish);
        dayContent.put("weekDateRange", weekContentRange);

        final DocumentReference weekRef = firestoreDatabase.collection("quarters_"+calender.get(Calendar.YEAR))
                .document(selectedQuarter).collection("weeks").document(selectedWeek);
        
        weekRef.collection("teacher").document("Teacher_Comments")
                .set(dayContent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        statusTextView.setText("Content uploaded successfully.");
                        Toast.makeText(getApplicationContext(), "content upload success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "content upload failed", Toast.LENGTH_SHORT).show();
                        statusTextView.setText("Failed to upload content: " + e.getMessage());
                    }
                });
    }

    private void uploadSaturdayImage(DocumentReference dayRef, final TextView statusTextView, Uri imgUri) {
    if (imgUri != null) {
        // Assuming you have Spinner references for quarter, week, and year
        String quarter = quarterSpinner.getSelectedItem().toString();
        String week = weekSpinner.getSelectedItem().toString();
        int year = calender.get(Calendar.YEAR);
        String newFileName = "";   

        // Get the original file extension
        String originalFileName = imgUri.getLastPathSegment();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Construct the new file name with the extension
        if(quarter != null && week != null) {
        	 newFileName = quarter + "_" + year + "_" + week + fileExtension;
        }else{
            newFileName = quarter + fileExtension;
        } 
        
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageRef = storageReference.child("saturday_images/" + newFileName);
        imageRef.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dayRef.update("image_url", uri.toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                statusTextView.setText("Image uploaded successfully.");
                                                Toast.makeText(getApplicationContext(), "Image upload success", Toast.LENGTH_SHORT).show();
                                                imageUri = null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                statusTextView.setText("Failed to update image URL: " + e.getMessage());
                                                Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "image upload failed", Toast.LENGTH_SHORT).show();
                        statusTextView.setText("Failed to upload image: " + e.getMessage());
                    }
                });
    }
}
    
@Override
public void onDateSet(DatePicker datePicker, int year, int month, int day) {
    calender.set(Calendar.YEAR, year);
    calender.set(Calendar.MONTH, month);
    calender.set(Calendar.DAY_OF_MONTH, day);

    // Map days and months to their Swahili equivalents
    String[] swahiliDays = {"Jumapili", "Jumatatu", "Jumanne", "Jumatano", "Alhamisi", "Ijumaa", "Jumamosi"};
    String[] swahiliMonths = {"Januari", "Februari", "Machi", "Aprili", "Mei", "Juni", "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba"};

    int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK);
    String dayOfWeekSwahili = swahiliDays[dayOfWeek - 1];
    String monthSwahili = swahiliMonths[month];
    this.currentDateStringEnglish = DateFormat.getDateInstance(DateFormat.FULL).format(calender.getTime());
    // Construct the Swahili date string for the selected date
    this.currentDateString = dayOfWeekSwahili + " " + day + " " + monthSwahili + " " + year;

    // Calculate the start date (Saturday)
    Calendar startCal = (Calendar) calender.clone();
    while (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
        startCal.add(Calendar.DAY_OF_MONTH, -1);
    }
    int startDay = startCal.get(Calendar.DAY_OF_MONTH);
    String startMonthSwahili = swahiliMonths[startCal.get(Calendar.MONTH)];

    // Calculate the end date (Friday)
    Calendar endCal = (Calendar) startCal.clone();
    endCal.add(Calendar.DAY_OF_MONTH, 6);
    int endDay = endCal.get(Calendar.DAY_OF_MONTH);
    String endMonthSwahili = swahiliMonths[endCal.get(Calendar.MONTH)];

    // Construct the week range string
    String weekRangeString;
    if (startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
        weekRangeString = startMonthSwahili + " " + startDay + " - " + endDay;
    } else {
        weekRangeString = startMonthSwahili + " " + startDay + " - " + endMonthSwahili + " " + endDay;
    }

    // Store the week range in a variable (for example, weekContentRange)
    weekContentRange = weekRangeString + " ," + year;

    // Update other variables as needed
    this.selectedYear = year;
    this.selectedMonth = month + 1; // month + 1 because Calendar.MONTH is 0-indexed
    this.selectedDay = day;
    this.selectedWeekDay = dayOfWeek;

    if (dayOfWeek == Calendar.SATURDAY) {
        this.selectedSaturday = this.currentDateString;
        saturdayImageView.setVisibility(View.VISIBLE);
        selectImageButton.setVisibility(View.VISIBLE);
    }

    if (dayOfWeek == Calendar.FRIDAY) {
        this.selectedFriday = this.currentDateString;
    }

    // Display the Swahili date string in a TextView
    TextView dateDisplayTextView = findViewById(R.id.date_display);
    dateDisplayTextView.setText("Swahili: "+ this.currentDateString + "/n English: " + this.currentDateStringEnglish);

    // Optionally display the week content range string in another TextView
    TextView weekRangeTextView = findViewById(R.id.week_range_display);
    weekRangeTextView.setText(weekContentRange);
}
     
public void showUploadFailure(Exception exception) {
    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
    Log.w("tag", "ERROR:", exception);
}
    
/*
    public void uploadContent(TextView uploadStatusTextView) {
        String title = Objects.requireNonNull(this.titleEditText.getText()).toString();
        String content = Objects.requireNonNull(this.contentEditText.getText()).toString();
        String question = Objects.requireNonNull(this.questionEditText.getText()).toString();

        Map<String, Object> lessonData = new HashMap<>();
        Map<String, String> weekData = new HashMap<>();
        String collectionName = this.selectedQuarter + "-" + this.selectedYear.toString();
        String weekDocument = this.selectedWeek;
        Calendar.getInstance();

        String dayDocumentName = weekDocument + "_" + this.selectedMonth.toString() + "-" + this.selectedDay.toString();
        lessonData.put(FIELD_DATE, this.currentDateString);
        lessonData.put(FIELD_TITLE, title);
        lessonData.put(FIELD_CONTENT, content);
        lessonData.put(FIELD_QUESTION, question);

        weekData.put("title", title);
        weekData.put("saturday", this.selectedSaturday);
        weekData.put("friday", this.selectedFriday);

        this.firestoreDatabase.collection(collectionName).document(weekDocument).set(weekData);
        this.firestoreDatabase.collection(collectionName).document(weekDocument).collection("week").document(dayDocumentName).set(lessonData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(admin.this.getApplicationContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        admin.this.showUploadFailure(exception);
                    }
                });

        uploadStatusTextView.setText(this.currentDateString + "\n" + title + "\n" + content + "\n" + question + "\n" + this.selectedYear + "\n" + this.selectedQuarter + "\n" + this.selectedWeek);
    }
    
    */


    public void showDatePicker(View view) {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "date picker");
    }

    
    
    /*
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        this.currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        this.selectedYear = year;
        this.selectedMonth = month + 1;
        this.selectedDay = day;
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        this.selectedWeekDay = dayOfWeek;

        if (dayOfWeek == Calendar.SATURDAY) {
            this.selectedSaturday = DateFormat.getDateInstance().format(calendar.getTime());
        }

        if (this.selectedWeekDay == Calendar.FRIDAY) {
            this.selectedFriday = DateFormat.getDateInstance().format(calendar.getTime());
        }

        TextView dateDisplayTextView = findViewById(R.id.date_display);
        dateDisplayTextView.setText(this.currentDateString);
    }
    */
    
}