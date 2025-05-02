package com.group25.photos;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class PhotoActivity extends AppCompatActivity implements TagAdapter.OnTagDeleteListener {
    private Photo photo;
    private TagAdapter tagAdapter;
    private List<Photo.Tag> photoTags;
    private List<Album> allAlbums;
    private List<Photo> albumPhotos;
    private int currentIndex;
    private String albumName;
    private ImageView imageView;
    private TextView tagsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageView = findViewById(R.id.fullPhotoImageView);
        tagsTextView = findViewById(R.id.photoTagsTextView);
        Button prevButton = findViewById(R.id.prevPhotoButton);
        Button nextButton = findViewById(R.id.nextPhotoButton);

        String uri = getIntent().getStringExtra("photo_uri");
        albumName = getIntent().getStringExtra("album_name");
        allAlbums = StorageUtil.loadAlbums(this);
        albumPhotos = new ArrayList<>();
        currentIndex = -1;

        // Find the album and photo list
        if (allAlbums != null && albumName != null) {
            for (Album a : allAlbums) {
                if (a.getName().equals(albumName)) {
                    albumPhotos = a.getPhotos();
                    break;
                }
            }
        }
        
        // Find the current photo object in the specific album's list
        if (!albumPhotos.isEmpty() && uri != null) {
             for (int i = 0; i < albumPhotos.size(); i++) {
                if (albumPhotos.get(i).getUri().equals(uri)) {
                    photo = albumPhotos.get(i);
                    currentIndex = i;
                    break;
                }
            }
        }

        // Handle case where photo wasn't found or list is empty
        if (photo == null) {
             Toast.makeText(this, "Error: Photo not found in album.", Toast.LENGTH_LONG).show();
             if (uri != null) {
                 photo = new Photo(uri);
             } else {
                 finish(); 
                 return;
             }
        }
        
        photoTags = photo.getTags();

        // Update navigation button states
        updateNavigationButtons(prevButton, nextButton);

        // Helper to update UI for current photo
        Runnable updatePhotoUI = () -> {
            if (photo == null) return;
            imageView.setImageURI(android.net.Uri.parse(photo.getUri()));
            photoTags = photo.getTags();
            updateTagsDisplay();
            updateNavigationButtons(prevButton, nextButton);
            setTitle(String.format("Photo %d of %d", currentIndex + 1, albumPhotos.size()));
        };

        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                photo = albumPhotos.get(currentIndex);
                updatePhotoUI.run();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < albumPhotos.size() - 1) {
                currentIndex++;
                photo = albumPhotos.get(currentIndex);
                updatePhotoUI.run();
            }
        });

        EditText tagValueEditText = findViewById(R.id.tagValueEditText);
        Button addPersonTagButton = findViewById(R.id.addPersonTagButton);
        Button addLocationTagButton = findViewById(R.id.addLocationTagButton);
        ListView tagsListView = findViewById(R.id.tagsListView);

        tagAdapter = new TagAdapter(this, photoTags, this);
        tagsListView.setAdapter(tagAdapter);

        addPersonTagButton.setOnClickListener(v -> addTag(Photo.Tag.Type.PERSON, tagValueEditText));
        addLocationTagButton.setOnClickListener(v -> addTag(Photo.Tag.Type.LOCATION, tagValueEditText));

        // Initial UI update
        updatePhotoUI.run();
    }

    private void addTag(Photo.Tag.Type type, EditText editText) {
        String value = editText.getText().toString().trim();
        if (!value.isEmpty()) {
            Photo.Tag tag = new Photo.Tag(type, value);
            if (!photoTags.contains(tag)) {
                photo.addTag(tag);
                updateTagsDisplay();
                saveTagChange();
                editText.setText("");
                Toast.makeText(this, type + " tag added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tag already exists", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNavigationButtons(Button prevButton, Button nextButton) {
        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < albumPhotos.size() - 1);
    }

    private void updateTagsDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tags: ");
        for (Photo.Tag tag : photoTags) {
            sb.append(tag.getType()).append(": ").append(tag.getValue()).append("; ");
        }
        tagsTextView.setText(sb.toString());

        tagAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTagDelete(int position) {
        if (position >= 0 && position < photoTags.size()) {
            photoTags.remove(position);
            updateTagsDisplay();
            saveTagChange();
            Toast.makeText(this, "Tag removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTagChange() {
        if (allAlbums != null && currentAlbumIndex() != -1) {
            StorageUtil.saveAlbums(this, allAlbums);
        } else {
             Toast.makeText(this, "Error saving tag changes.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private int currentAlbumIndex() {
        if (allAlbums == null || albumName == null) return -1;
        for (int i = 0; i < allAlbums.size(); i++) {
            if (allAlbums.get(i).getName().equals(albumName)) {
                return i;
            }
        }
        return -1;
    }

    // Handle Up button press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to parent activity (AlbumActivity as defined in Manifest)
            finish(); // Simple finish() is usually sufficient
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 