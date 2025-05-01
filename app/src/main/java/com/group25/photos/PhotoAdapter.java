package com.group25.photos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private Context context;
    private List<Photo> photos;
    private OnPhotoLongClickListener longClickListener;

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(int position);
    }

    public PhotoAdapter(Context context, List<Photo> photos, OnPhotoLongClickListener longClickListener) {
        this.context = context;
        this.photos = photos;
        this.longClickListener = longClickListener;
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
        holder.imageView.setImageURI(Uri.parse(photo.getUri()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("photo_uri", photo.getUri());
            if (context instanceof AlbumActivity) {
                String albumName = ((AlbumActivity) context).getAlbumName();
                intent.putExtra("album_name", albumName);
            }
            context.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onPhotoLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
        }
    }
} 