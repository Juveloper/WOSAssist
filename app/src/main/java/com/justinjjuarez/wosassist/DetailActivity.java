package com.justinjjuarez.wosassist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;

public class DetailActivity extends AppCompatActivity {

    private ViewPager2 imageSlider;
    private TextView titleTextView, descriptionTextView, locationTextView, dateTextView, priceTextView;
    private Button chatButton;
    private FirebaseFirestore db;
    private String itemId;
    private Item currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageSlider = findViewById(R.id.detail_image_slider);
        titleTextView = findViewById(R.id.detail_title);
        descriptionTextView = findViewById(R.id.detail_description);
        locationTextView = findViewById(R.id.detail_location);
        dateTextView = findViewById(R.id.detail_date);
        priceTextView = findViewById(R.id.detail_price); // NEU
        chatButton = findViewById(R.id.chat_button);

        db = FirebaseFirestore.getInstance();
        itemId = getIntent().getStringExtra("ITEM_ID");

        if (itemId != null) {
            loadItemDetails(itemId);
        }

        chatButton.setOnClickListener(v -> {
            // Öffne ChatActivity (noch nicht implementiert)
            Toast.makeText(this, "Chat-Feature bald verfügbar", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadItemDetails(String itemId) {
        db.collection("Auftraege").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentItem = documentSnapshot.toObject(Item.class);
                    if (currentItem != null) {
                        titleTextView.setText(currentItem.getTitle());
                        descriptionTextView.setText(currentItem.getDescription());
                        locationTextView.setText(currentItem.getLocation());
                        dateTextView.setText(currentItem.getDate());

                        String price = currentItem.getPrice();
                        if (price == null || price.isEmpty()) {
                            priceTextView.setText("0 €");
                        } else {
                            priceTextView.setText(price + " €");
                        }

                        if (currentItem.getOrderPicture() != null && !currentItem.getOrderPicture().isEmpty()) {
                            OrderImageSliderAdapter sliderAdapter = new OrderImageSliderAdapter(this, currentItem.getOrderPicture());
                            imageSlider.setAdapter(sliderAdapter);
                        }
                    }
                });
    }

}
