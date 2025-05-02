package com.group25.photos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private Context context;
    private List<Album> albums;

    // Interface for callbacks to MainActivity
    public interface AlbumActionListener {
        void onRenameAlbum(int position, String newName);
        void onDeleteAlbum(int position);
    }
    private AlbumActionListener actionListener;

    public AlbumAdapter(Context context, List<Album> albums, AlbumActionListener listener) {
        this.context = context;
        this.albums = albums;
        this.actionListener = listener; // Assign listener
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
        holder.photoCount.setText(String.format("%d photos", album.getPhotoCount()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AlbumActivity.class);
            intent.putExtra("album_name", album.getName());
            context.startActivity(intent);
        });

        holder.renameButton.setOnClickListener(v -> {
            EditText input = new EditText(context);
            input.setText(album.getName());
            new AlertDialog.Builder(context)
                    .setTitle("Rename Album")
                    .setView(input)
                    .setPositiveButton("Rename", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty() && !newName.equals(album.getName())) {
                            // Check if new name already exists
                            boolean nameExists = false;
                            for(Album existingAlbum : albums) {
                                if (existingAlbum.getName().equalsIgnoreCase(newName)) {
                                    nameExists = true;
                                    break;
                                }
                            }
                            if (nameExists) {
                                android.widget.Toast.makeText(context, "Album name already exists", android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                // Use callback to notify MainActivity
                                if (actionListener != null) {
                                     actionListener.onRenameAlbum(holder.getAdapterPosition(), newName);
                                }
                            }
                        } else if (newName.isEmpty()) {
                             android.widget.Toast.makeText(context, "Album name cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Album")
                    .setMessage("Are you sure you want to delete the album '" + album.getName() + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Use callback to notify MainActivity
                        if (actionListener != null) {
                            actionListener.onDeleteAlbum(holder.getAdapterPosition());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumName;
        TextView photoCount;
        Button renameButton;
        Button deleteButton;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.albumName);
            photoCount = itemView.findViewById(R.id.photoCount);
            renameButton = itemView.findViewById(R.id.renameAlbumButton);
            deleteButton = itemView.findViewById(R.id.deleteAlbumButton);
        }
    }
} 