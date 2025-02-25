package com.justinjjuarez.wosassist;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewItemsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        db = FirebaseFirestore.getInstance();
        loadItemsFromFirestore();
    }

    private void loadItemsFromFirestore() {
        db.collection("Auftraege")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = document.toObject(Item.class);
                        if (item != null) {
                            item.setId(document.getId()); // Firestore-ID setzen
                            itemList.add(item);
                        }
                    }
                    itemAdapter.updateItemList(itemList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler beim Laden der Aufträge!", Toast.LENGTH_SHORT).show();
                });
    }
}
