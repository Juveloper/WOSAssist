package com.justinjjuarez.wosassist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    private TextView titleTextView, descriptionTextView, dateTextView;
    private ViewPager2 viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private Button deleteButton;
    private String itemId;
    private String currentUserId;
    private List<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        titleTextView = findViewById(R.id.detail_title);
        descriptionTextView = findViewById(R.id.detail_description);
        dateTextView = findViewById(R.id.detail_date);
        viewPager = findViewById(R.id.view_pager);
        deleteButton = findViewById(R.id.delete_button); // 🆕 Löschen-Button

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (user != null) ? user.getUid() : null;

        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId != null) {
            loadItemDetails(itemId);
        } else {
            Toast.makeText(this, "Fehler: Kein Item gefunden!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 🗑️ Löschen-Button
        deleteButton.setOnClickListener(v -> deleteItem());
    }

    private void loadItemDetails(String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference itemRef = db.collection("Auftraege").document(itemId);

        itemRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Item item = documentSnapshot.toObject(Item.class);
                if (item != null) {
                    titleTextView.setText(item.getTitle());
                    descriptionTextView.setText(item.getDescription());
                    dateTextView.setText(item.getDate());

                    imageUrls = item.getOrderPicture();
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        imagePagerAdapter = new ImagePagerAdapter(this, imageUrls);
                        viewPager.setAdapter(imagePagerAdapter);
                    }

                    // 🛑 Nur der Ersteller darf löschen!
                    if (currentUserId != null && currentUserId.equals(item.getUserID())) {
                        deleteButton.setVisibility(View.VISIBLE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                    }
                }
            } else {
                Toast.makeText(this, "Fehler: Auftrag nicht gefunden!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Fehler beim Laden!", Toast.LENGTH_SHORT).show());
    }

    private void deleteItem() {
        if (itemId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Auftraege").document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteImagesFromStorage();
                    Toast.makeText(this, "Auftrag gelöscht!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler beim Löschen!", Toast.LENGTH_SHORT).show());
    }

    private void deleteImagesFromStorage() {
        if (imageUrls != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            for (String url : imageUrls) {
                StorageReference photoRef = storage.getReferenceFromUrl(url);
                photoRef.delete()
                        .addOnFailureListener(e -> Toast.makeText(this, "Bild konnte nicht gelöscht werden!", Toast.LENGTH_SHORT).show());
            }
        }
    }
}
