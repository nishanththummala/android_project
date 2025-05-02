package com.group25.photos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private Context context;
    private List<Photo> photos;
    private PhotoActionListener actionListener;

    public interface PhotoActionListener {
        void onRemovePhoto(int position);
        void onMovePhoto(int position);
        void onPhotoClick(int position);
    }

    public PhotoAdapter(Context context, List<Photo> photos, PhotoActionListener listener) {
        this.context = context;
        this.photos = photos;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        try {
            holder.imageView.setImageURI(Uri.parse(photo.getUri()));
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.photo_placeholder);
        }

        holder.imageView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onPhotoClick(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(null);
        holder.itemView.setClickable(false);
        holder.imageView.setClickable(true);

        holder.removeButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRemovePhoto(holder.getAdapterPosition());
            }
        });

        holder.moveButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onMovePhoto(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button removeButton;
        Button moveButton;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
            removeButton = itemView.findViewById(R.id.removePhotoButton);
            moveButton = itemView.findViewById(R.id.movePhotoButton);
        }
    }
} 