package com.group25.photos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private Context context;
    private List<Album> albums;

    public AlbumAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.albumName.setText(album.getName());
        holder.photoCount.setText(album.getPhotoCount() + " photos");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AlbumActivity.class);
            intent.putExtra("album_name", album.getName());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Album Options")
                .setItems(new String[]{"Rename", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        // Rename
                        android.widget.EditText input = new android.widget.EditText(context);
                        input.setText(album.getName());
                        new android.app.AlertDialog.Builder(context)
                            .setTitle("Rename Album")
                            .setView(input)
                            .setPositiveButton("Rename", (d, w) -> {
                                String newName = input.getText().toString().trim();
                                if (!newName.isEmpty()) {
                                    album.setName(newName);
                                    notifyItemChanged(position);
                                    StorageUtil.saveAlbums(context, albums);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    } else if (which == 1) {
                        // Delete
                        new android.app.AlertDialog.Builder(context)
                            .setTitle("Delete Album")
                            .setMessage("Are you sure you want to delete this album?")
                            .setPositiveButton("Delete", (d, w) -> {
                                albums.remove(position);
                                notifyItemRemoved(position);
                                StorageUtil.saveAlbums(context, albums);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    }
                })
                .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumName;
        TextView photoCount;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.albumName);
            photoCount = itemView.findViewById(R.id.photoCount);
        }
    }
} 