package com.group25.photos;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {
    private Photo photo;
    private ArrayAdapter<String> tagAdapter;
    private ArrayList<String> tagStrings;
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

        imageView = findViewById(R.id.fullPhotoImageView);
        tagsTextView = findViewById(R.id.photoTagsTextView);
        Button prevButton = findViewById(R.id.prevPhotoButton);
        Button nextButton = findViewById(R.id.nextPhotoButton);

        String uri = getIntent().getStringExtra("photo_uri");
        albumName = getIntent().getStringExtra("album_name");
        allAlbums = StorageUtil.loadAlbums(this);
        albumPhotos = new ArrayList<>();
        currentIndex = 0;

        // Find the album and photo list
        if (allAlbums != null && albumName != null) {
            for (Album a : allAlbums) {
                if (a.getName().equals(albumName)) {
                    albumPhotos = a.getPhotos();
                    break;
                }
            }
        }
        
        // Find the index of the current photo
        for (int i = 0; i < albumPhotos.size(); i++) {
            if (albumPhotos.get(i).getUri().equals(uri)) {
                currentIndex = i;
                break;
            }
        }
        
        if (albumPhotos.isEmpty()) {
            photo = new Photo(uri);
            albumPhotos.add(photo);
            currentIndex = 0;
        } else {
            photo = albumPhotos.get(currentIndex);
        }

        // Update navigation button states
        updateNavigationButtons(prevButton, nextButton);

        // Helper to update UI for current photo
        Runnable updatePhotoUI = () -> {
            imageView.setImageURI(android.net.Uri.parse(photo.getUri()));
            updateTagsDisplay();
            updateNavigationButtons(prevButton, nextButton);
            // Show current photo position
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

        tagStrings = new ArrayList<>();
        for (Photo.Tag tag : photo.getTags()) {
            tagStrings.add(tag.getType() + ": " + tag.getValue());
        }
        tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tagStrings);
        tagsListView.setAdapter(tagAdapter);

        addPersonTagButton.setOnClickListener(v -> {
            String value = tagValueEditText.getText().toString().trim();
            if (!value.isEmpty()) {
                Photo.Tag tag = new Photo.Tag(Photo.Tag.Type.PERSON, value);
                if (!photo.getTags().contains(tag)) {
                    photo.addTag(tag);
                    updateTagsDisplay();
                    saveTagChange();
                    tagValueEditText.setText("");
                    Toast.makeText(this, "Person tag added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Tag already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addLocationTagButton.setOnClickListener(v -> {
            String value = tagValueEditText.getText().toString().trim();
            if (!value.isEmpty()) {
                Photo.Tag tag = new Photo.Tag(Photo.Tag.Type.LOCATION, value);
                if (!photo.getTags().contains(tag)) {
                    photo.addTag(tag);
                    updateTagsDisplay();
                    saveTagChange();
                    tagValueEditText.setText("");
                    Toast.makeText(this, "Location tag added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Tag already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tagsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            photo.getTags().remove(position);
            updateTagsDisplay();
            saveTagChange();
            Toast.makeText(this, "Tag removed", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Initial UI update
        updatePhotoUI.run();
    }

    private void updateNavigationButtons(Button prevButton, Button nextButton) {
        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < albumPhotos.size() - 1);
    }

    private void updateTagsDisplay() {
        // Update the tags TextView
        StringBuilder sb = new StringBuilder();
        sb.append("Tags: ");
        for (Photo.Tag tag : photo.getTags()) {
            sb.append(tag.getType()).append(": ").append(tag.getValue()).append("; ");
        }
        tagsTextView.setText(sb.toString());

        // Update the tags ListView
        tagStrings.clear();
        for (Photo.Tag tag : photo.getTags()) {
            tagStrings.add(tag.getType() + ": " + tag.getValue());
        }
        tagAdapter.notifyDataSetChanged();
    }

    private void saveTagChange() {
        // Save the updated photo tags to storage
        if (allAlbums != null) {
            boolean found = false;
            for (Album a : allAlbums) {
                if (a.getName().equals(albumName)) {
                    List<Photo> albumPhotos = a.getPhotos();
                    for (int i = 0; i < albumPhotos.size(); i++) {
                        Photo p = albumPhotos.get(i);
                        if (p.getUri().equals(photo.getUri())) {
                            // Replace the entire photo object to ensure all changes are saved
                            albumPhotos.set(i, photo);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // If photo wasn't found in the album (shouldn't happen), add it
                        a.addPhoto(photo);
                    }
                    break;
                }
            }
            // Save changes to storage
            StorageUtil.saveAlbums(this, allAlbums);
            Toast.makeText(this, "Tags saved", Toast.LENGTH_SHORT).show();
        }
    }
} 