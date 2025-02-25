package com.justinjjuarez.wosassist;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirestoreHelper {
    private static final String COLLECTION_NAME = "Auftraege"; // Firestore-Sammlung
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void addItem(Item item) {
        db.collection(COLLECTION_NAME).add(item);
    }

    public void getAllItems(Consumer<List<Item>> callback) {
        db.collection(COLLECTION_NAME).get()
                .addOnCompleteListener(task -> {
                    List<Item> itemList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Item item = document.toObject(Item.class);
                            itemList.add(item);
                        }
                    }
                    callback.accept(itemList);
                });
    }
}
