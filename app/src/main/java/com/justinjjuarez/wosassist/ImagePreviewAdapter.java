package com.justinjjuarez.wosassist;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Uri> imageUris;
    private final Context context;

    public ImageAdapter(List<Uri> imageUris, Context context) {
        this.imageUris = imageUris != null ? imageUris : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        Log.d("WOSAssist", "Bild wird geladen: " + imageUri);

        Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void updateImageList(List<Uri> newImageUris) {
        imageUris.clear();
        if (newImageUris != null) {
            imageUris.addAll(newImageUris);
        }
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
