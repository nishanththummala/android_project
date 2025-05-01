package com.group25.photos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoLongClickListener {
    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;
    private List<Photo> photos;
    private String albumName;
    private List<Album> allAlbums;
    private Album currentAlbum;
    private static final int PICK_PHOTO_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        albumName = getIntent().getStringExtra("album_name");
        setTitle(albumName);

        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        FloatingActionButton addPhotoFab = findViewById(R.id.addPhotoFab);
        FloatingActionButton movePhotoFab = findViewById(R.id.movePhotoFab);

        // Load albums and current album
        allAlbums = StorageUtil.loadAlbums(this);
        if (allAlbums == null) allAlbums = new ArrayList<>();
        currentAlbum = null;
        for (Album a : allAlbums) {
            if (a.getName().equals(albumName)) {
                currentAlbum = a;
                break;
            }
        }
        if (currentAlbum == null) {
            currentAlbum = new Album(albumName);
            allAlbums.add(currentAlbum);
        }
        photos = currentAlbum.getPhotos();
        photoAdapter = new PhotoAdapter(this, photos, this);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photosRecyclerView.setAdapter(photoAdapter);

        addPhotoFab.setOnClickListener(v -> pickPhoto());
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            String uri = data.getData().toString();
            Photo newPhoto = new Photo(uri);
            photos.add(newPhoto);
            photoAdapter.notifyItemInserted(photos.size() - 1);
            StorageUtil.saveAlbums(this, allAlbums);
        }
    }

    @Override
    public void onPhotoLongClick(int position) {
        // Remove or move photo
        Photo photo = photos.get(position);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Photo Options")
            .setItems(new String[]{"Remove", "Move to another album"}, (dialog, which) -> {
                if (which == 0) {
                    // Remove
                    photos.remove(position);
                    photoAdapter.notifyItemRemoved(position);
                    StorageUtil.saveAlbums(this, allAlbums);
                } else if (which == 1) {
                    // Move
                    showMovePhotoDialog(photo, position);
                }
            })
            .show();
    }

    private void showMovePhotoDialog(Photo photo, int position) {
        List<String> albumNames = new ArrayList<>();
        for (Album a : allAlbums) {
            if (!a.getName().equals(albumName)) {
                albumNames.add(a.getName());
            }
        }
        if (albumNames.isEmpty()) {
            Toast.makeText(this, "No other albums to move to", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] namesArr = albumNames.toArray(new String[0]);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Move Photo To:")
            .setItems(namesArr, (dialog, which) -> {
                String destAlbumName = namesArr[which];
                for (Album a : allAlbums) {
                    if (a.getName().equals(destAlbumName)) {
                        a.addPhoto(photo);
                        break;
                    }
                }
                photos.remove(position);
                photoAdapter.notifyItemRemoved(position);
                StorageUtil.saveAlbums(this, allAlbums);
            })
            .show();
    }
} 