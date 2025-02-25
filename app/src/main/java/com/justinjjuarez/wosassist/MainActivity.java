package com.justinjjuarez.wosassist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        loadItemsFromFirestore();

        // Suchfunktion
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterItems(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void loadItemsFromFirestore() {
        FirebaseFirestore.getInstance().collection("Auftraege")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Keine Aufträge gefunden.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Item item = document.toObject(Item.class);
                            if (item != null) {
                                item.setId(document.getId()); // Firestore-ID setzen
                                itemList.add(item);
                            }
                        }
                    }
                    itemAdapter.updateItemList(itemList); // RecyclerView aktualisieren
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler beim Laden der Aufträge: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void filterItems(String query) {
        List<Item> filteredList = new ArrayList<>();
        for (Item item : itemList) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        itemAdapter.updateItemList(filteredList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem loginLogoutItem = menu.findItem(R.id.action_login_logout);
        loginLogoutItem.setTitle(auth.getCurrentUser() != null ? "Abmelden" : "Anmelden");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId(); // Speichert die ID des geklickten Elements

        if (itemId == R.id.action_login_logout) { // Direkter Vergleich mit der ID
            if (auth.getCurrentUser() != null) {
                auth.signOut();
                Toast.makeText(this, "Abgemeldet", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            recreate(); // Aktualisiert die Activity
            return true;
        }

        if (itemId == R.id.action_create_job) {
            startActivity(new Intent(this, CreateItemActivity.class));
            return true;
        }

        if (itemId == R.id.action_view_jobs) {
            startActivity(new Intent(this, ViewItemsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
