package com.group25.photos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
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
        albumAdapter = new AlbumAdapter(this, albums);
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
                        createNewAlbum(albumName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createNewAlbum(String albumName) {
        Album newAlbum = new Album(albumName);
        albums.add(newAlbum);
        albumAdapter.notifyItemInserted(albums.size() - 1);
        StorageUtil.saveAlbums(this, albums);
    }
}