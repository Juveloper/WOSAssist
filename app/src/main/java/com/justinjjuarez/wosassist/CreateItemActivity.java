package com.justinjjuarez.wosassist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    private EditText titleEditText, descriptionEditText, locationEditText, dateEditText;
    private Button saveButton, selectImageButton;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImages.clear();
                    if (result.getData().getClipData() != null) {
                        for (int i = 0; i < result.getData().getClipData().getItemCount(); i++) {
                            selectedImages.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedImages.add(result.getData().getData());
                    }
                    imageAdapter.updateImageList(selectedImages);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);

        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);
        dateEditText = findViewById(R.id.date_edit_text);
        saveButton = findViewById(R.id.save_button);
        selectImageButton = findViewById(R.id.select_image_button);
        imageRecyclerView = findViewById(R.id.image_recycler_view);

        // Der Button ist jetzt standardmäßig aktiviert
        saveButton.setEnabled(true);

        imageAdapter = new ImageAdapter(selectedImages, this);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imageAdapter);


        selectImageButton.setOnClickListener(v -> openFileChooser());
        saveButton.setOnClickListener(v -> saveItem());
        dateEditText.setOnClickListener(v -> showDatePickerDialog()); // Datum-Picker öffnen
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Bilder auswählen"));
    }

    private void saveItem() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Bitte Titel eingeben!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Bitte Beschreibung eingeben!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Falls kein Datum eingegeben wurde, speichere "Kein Datum"
        if (date.isEmpty()) {
            date = "Kein Datum";
        }

        // Falls kein Ort eingegeben wurde, speichere "Kein Ort"
        if (location.isEmpty()) {
            location = "Kein Ort";
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Nicht angemeldet!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = auth.getCurrentUser().getUid();

        // Falls keine Bilder vorhanden sind, speichere direkt
        if (selectedImages.isEmpty()) {
            saveItemToFirestore(title, description, location, date, new ArrayList<>(), userID);
        } else {
            uploadImagesToStorage(title, description, location, date, userID);
        }
    }

    private void uploadImagesToStorage(String title, String description, String location, String date, String userID) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("item_images");
        for (Uri imageUri : selectedImages) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadedImageUrls.add(uri.toString());
                        if (uploadedImageUrls.size() == selectedImages.size()) {
                            saveItemToFirestore(title, description, location, date, uploadedImageUrls, userID);
                        }
                    })
            ).addOnFailureListener(e -> Toast.makeText(this, "Fehler beim Hochladen!", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveItemToFirestore(String title, String description, String location, String date, List<String> imageUrls, String userID) {
        FirebaseFirestore.getInstance().collection("Auftraege").add(new Item(title, description, location, date, imageUrls, userID))
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
