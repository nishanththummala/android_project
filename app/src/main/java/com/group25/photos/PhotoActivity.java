package com.group25.photos;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        ImageView imageView = findViewById(R.id.fullPhotoImageView);
        TextView tagsTextView = findViewById(R.id.photoTagsTextView);
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

        // Helper to update UI for current photo
        Runnable updatePhotoUI = () -> {
            imageView.setImageURI(android.net.Uri.parse(photo.getUri()));
            // Show tags as text
            StringBuilder sb = new StringBuilder();
            sb.append("Tags: ");
            for (Photo.Tag tag : photo.getTags()) {
                sb.append(tag.getType()).append(": ").append(tag.getValue()).append("; ");
            }
            tagsTextView.setText(sb.toString());
            // Update tag list
            tagStrings.clear();
            for (Photo.Tag tag : photo.getTags()) {
                tagStrings.add(tag.getType() + ": " + tag.getValue());
            }
            tagAdapter.notifyDataSetChanged();
        };

        prevButton.setOnClickListener(v -> {
            if (albumPhotos.size() > 0) {
                currentIndex = (currentIndex - 1 + albumPhotos.size()) % albumPhotos.size();
                photo = albumPhotos.get(currentIndex);
                updatePhotoUI.run();
            }
        });
        nextButton.setOnClickListener(v -> {
            if (albumPhotos.size() > 0) {
                currentIndex = (currentIndex + 1) % albumPhotos.size();
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
                    tagStrings.add("PERSON: " + value);
                    tagAdapter.notifyDataSetChanged();
                    saveTagChange();
                    updatePhotoUI.run();
                }
            }
        });

        addLocationTagButton.setOnClickListener(v -> {
            String value = tagValueEditText.getText().toString().trim();
            if (!value.isEmpty()) {
                Photo.Tag tag = new Photo.Tag(Photo.Tag.Type.LOCATION, value);
                if (!photo.getTags().contains(tag)) {
                    photo.addTag(tag);
                    tagStrings.add("LOCATION: " + value);
                    tagAdapter.notifyDataSetChanged();
                    saveTagChange();
                    updatePhotoUI.run();
                }
            }
        });

        tagsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            photo.getTags().remove(position);
            tagStrings.remove(position);
            tagAdapter.notifyDataSetChanged();
            saveTagChange();
            updatePhotoUI.run();
            return true;
        });

        // Initial UI update
        updatePhotoUI.run();
    }

    private void saveTagChange() {
        // Save the updated photo tags to storage
        if (allAlbums != null) {
            for (Album a : allAlbums) {
                for (Photo p : a.getPhotos()) {
                    if (p.getUri().equals(photo.getUri())) {
                        p.getTags().clear();
                        p.getTags().addAll(photo.getTags());
                        break;
                    }
                }
            }
            StorageUtil.saveAlbums(this, allAlbums);
        }
    }
} 