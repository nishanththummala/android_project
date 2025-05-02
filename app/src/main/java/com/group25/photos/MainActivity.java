package com.group25.photos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlbumAdapter.AlbumActionListener {
    private RecyclerView albumsRecyclerView;
    private AlbumAdapter albumAdapter;
    private List<Album> albums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView);
        FloatingActionButton addAlbumFab = findViewById(R.id.addAlbumFab);
        FloatingActionButton searchFab = findViewById(R.id.searchFab);

        // Setup RecyclerView
        albums = new ArrayList<>();
        albumAdapter = new AlbumAdapter(this, albums, this);
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        albumsRecyclerView.setAdapter(albumAdapter);

        // Load saved albums
        loadAlbums();

        // Setup FAB click listeners
        addAlbumFab.setOnClickListener(v -> showAddAlbumDialog());
        searchFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlbums(); // Reload albums and update the adapter
    }

    private void loadAlbums() {
        List<Album> loaded = StorageUtil.loadAlbums(this);
        if (loaded != null) {
            albums.clear();
            albums.addAll(loaded);
            albumAdapter.notifyDataSetChanged();
        }
    }

    private void showAddAlbumDialog() {
        EditText input = new EditText(this);
        input.setHint("Album Name");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create New Album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String albumName = input.getText().toString().trim();
                    if (!albumName.isEmpty()) {
                        boolean nameExists = false;
                        for (Album album : albums) {
                            if (album.getName().equalsIgnoreCase(albumName)) {
                                nameExists = true;
                                break;
                            }
                        }
                        if (nameExists) {
                            Toast.makeText(this, "Album name already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            createNewAlbum(albumName);
                        }
                    } else {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createNewAlbum(String albumName) {
        Album newAlbum = new Album(albumName);
        albums.add(newAlbum);
        if (albums.size() == 1) {
            albumAdapter.notifyDataSetChanged();
        } else {
            albumAdapter.notifyItemInserted(albums.size() - 1);
        }
        StorageUtil.saveAlbums(this, albums);
        Toast.makeText(this, "Album '" + albumName + "' created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRenameAlbum(int position, String newName) {
        if (position >= 0 && position < albums.size()) {
            albums.get(position).setName(newName);
            albumAdapter.notifyItemChanged(position);
            StorageUtil.saveAlbums(this, albums);
            Toast.makeText(this, "Album renamed to '" + newName + "'", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteAlbum(int position) {
        if (position >= 0 && position < albums.size()) {
            String deletedAlbumName = albums.get(position).getName();
            albums.remove(position);
            albumAdapter.notifyItemRemoved(position);
            albumAdapter.notifyItemRangeChanged(position, albums.size() - position);
            StorageUtil.saveAlbums(this, albums);
            Toast.makeText(this, "Album '" + deletedAlbumName + "' deleted", Toast.LENGTH_SHORT).show();
        }
    }
}