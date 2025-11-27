package com.justinjjuarez.wosassist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainOrderListAdapter extends RecyclerView.Adapter<MainOrderListAdapter.ViewHolder> {
    private final Context context;
    private final List<Item> itemList;

    public MainOrderListAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = new ArrayList<>(itemList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePreview;
        TextView titleText;
        TextView descriptionText;
        TextView priceText; // NEU

        public ViewHolder(View itemView) {
            super(itemView);
            imagePreview = itemView.findViewById(R.id.image_preview);
            titleText = itemView.findViewById(R.id.title_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            priceText = itemView.findViewById(R.id.price_text); // NEU
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.titleText.setText(item.getTitle());
        holder.descriptionText.setText(item.getDescription());

        String price = item.getPrice();
        if (price == null || price.isEmpty()) {
            holder.priceText.setText("0 €");
        } else {
            holder.priceText.setText(price + " €");
        }

        if (item.getOrderPicture() != null && !item.getOrderPicture().isEmpty()) {
            Glide.with(context)
                    .load(item.getOrderPicture().get(0))
                    .into(holder.imagePreview);
        } else {
            holder.imagePreview.setImageResource(R.drawable.testbild); // Platzhalter
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<Item> updatedList) {
        itemList.clear();
        itemList.addAll(updatedList);
        notifyDataSetChanged();
    }
}