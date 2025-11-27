package com.justinjjuarez.wosassist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateItemActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText, locationEditText, dateEditText, priceEditText;
    public Button saveButton, selectImageButton;
    public RecyclerView imageRecyclerView;
    private ImagePreviewAdapter imageAdapter;
    private final List<Uri> selectedImages = new ArrayList<>();
    private final List<String> uploadedImageUrls = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImages.clear();
                    Intent data = result.getData();

                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            takeImagePermission(imageUri);
                            selectedImages.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        takeImagePermission(imageUri);
                        selectedImages.add(imageUri);
                    }

                    Log.d("WOSAssist", "Bilder ausgewählt: " + selectedImages.size());

                    imageAdapter.updateImageList(new ArrayList<>(selectedImages));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_create);

        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);
        dateEditText = findViewById(R.id.date_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        saveButton = findViewById(R.id.save_button);
        selectImageButton = findViewById(R.id.select_image_button);
        imageRecyclerView = findViewById(R.id.image_recycler_view);

        imageAdapter = new ImagePreviewAdapter(selectedImages, this);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imageAdapter);

        requestStoragePermission();
        checkAuthStatus();

        selectImageButton.setOnClickListener(v -> openFileChooser());
        saveButton.setOnClickListener(v -> saveItem());
        dateEditText.setOnClickListener(v -> showDatePickerDialog());
    }

    private void checkAuthStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            saveButton.setEnabled(false);
            Toast.makeText(this, "Bitte zuerst anmelden!", Toast.LENGTH_SHORT).show();
        } else {
            saveButton.setEnabled(true);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Bilder auswählen"));
    }

    private void takeImagePermission(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void saveItem() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Bitte Titel eingeben!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Bitte Beschreibung eingeben!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty()) date = "Kein Datum";
        if (location.isEmpty()) location = "Kein Ort";
        if (price.isEmpty()) price = "0.00";

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Nicht angemeldet!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = auth.getCurrentUser().getUid();

        if (selectedImages == null || selectedImages.isEmpty()) {
            Toast.makeText(this, "Es wurde kein Bild ausgewählt!", Toast.LENGTH_SHORT).show();
            saveItemToFirestore(title, description, location, date, price, new ArrayList<>(), userID);
        } else {
            uploadImagesToStorage(title, description, location, date, price, userID);
        }
    }

    private void uploadImagesToStorage(String title, String description, String location, String date, String price, String userID) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("item_images");

        for (Uri imageUri : selectedImages) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadedImageUrls.add(uri.toString());

                        if (uploadedImageUrls.size() == selectedImages.size()) {
                            saveItemToFirestore(title, description, location, date, price, uploadedImageUrls, userID);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("WOSAssist", "Fehler beim Upload: " + e.getMessage(), e);
                        Toast.makeText(this, "Fehler beim Hochladen!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveItemToFirestore(String title, String description, String location, String date, String price, List<String> imageUrls, String userID) {
        FirebaseFirestore.getInstance().collection("Auftraege")
                .add(new Item(title, description, location, date, price, imageUrls, userID))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateItemActivity.this, "Auftrag gespeichert!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(CreateItemActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, yearSelected, monthSelected, daySelected) -> {
                    String selectedDate = daySelected + "." + (monthSelected + 1) + "." + yearSelected;
                    dateEditText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}
