package com.justinjjuarez.wosassist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        holder.dateTextView.setText(item.getDate());

        // 🔹 Bilder in ViewPager2 laden oder ausblenden, falls keine Bilder vorhanden sind
        if (item.getOrderPicture() != null && !item.getOrderPicture().isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(context, item.getOrderPicture());
            holder.imageSlider.setAdapter(adapter);
            holder.imageSlider.setVisibility(View.VISIBLE);
        } else {
            holder.imageSlider.setVisibility(View.GONE);
        }

        // 🔹 Aktuellen Nutzer abrufen
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 🔹 Wenn der aktuelle Nutzer der Ersteller ist, Löschen-Button anzeigen
        if (currentUser != null && item.getUserID().equals(currentUser.getUid())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> deleteItem(item.getId(), holder.getAdapterPosition()));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // 🔹 Klick auf Auftrag öffnet Detailansicht
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            v.getContext().startActivity(intent);
        });
    }

    // 🔹 Methode zum Löschen eines Auftrags
    private void deleteItem(String itemId, int position) {
        FirebaseFirestore.getInstance().collection("Auftraege").document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Auftrag gelöscht", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Fehler beim Löschen", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // 🔹 Methode zur Aktualisierung der Liste, um Daten neu zu laden
    public void updateItemList(List<Item> newItemList) {
        itemList.clear();
        itemList.addAll(newItemList);
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView dateTextView;
        public ViewPager2 imageSlider;
        public Button deleteButton;

        public ItemViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.title_text_view);
            descriptionTextView = view.findViewById(R.id.description_text_view);
            dateTextView = view.findViewById(R.id.date_text_view);
            imageSlider = view.findViewById(R.id.image_slider);
            deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
