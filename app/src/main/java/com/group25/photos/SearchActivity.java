package com.group25.photos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView searchValue1;
    private AutoCompleteTextView searchValue2;
    private Spinner tagType1Spinner;
    private Spinner tagType2Spinner;
    private ArrayAdapter<String> resultsAdapter;
    private Set<String> existingPersonTags;
    private Set<String> existingLocationTags;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize tag value sets
        existingPersonTags = new HashSet<>();
        existingLocationTags = new HashSet<>();
        loadExistingTags();

        // Setup UI components
        tagType1Spinner = findViewById(R.id.tagType1Spinner);
        tagType2Spinner = findViewById(R.id.tagType2Spinner);
        searchValue1 = findViewById(R.id.searchValue1);
        searchValue2 = findViewById(R.id.searchValue2);
        Button searchAndButton = findViewById(R.id.searchAndButton);
        Button searchOrButton = findViewById(R.id.searchOrButton);
        ListView searchResultsList = findViewById(R.id.searchResultsList);

        // Setup spinners
        ArrayAdapter<CharSequence> tagTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.tag_types, android.R.layout.simple_spinner_item);
        tagTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagType1Spinner.setAdapter(tagTypeAdapter);
        tagType2Spinner.setAdapter(tagTypeAdapter);

        // Setup auto-complete adapters
        setupAutoComplete(searchValue1, tagType1Spinner);
        setupAutoComplete(searchValue2, tagType2Spinner);

        resultsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        searchResultsList.setAdapter(resultsAdapter);

        searchAndButton.setOnClickListener(v -> performSearch(true));
        searchOrButton.setOnClickListener(v -> performSearch(false));

        // Make search results clickable to view the photo
        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            String uri = resultsAdapter.getItem(position);
            if (uri != null) {
                // Launch PhotoActivity with the selected photo
                launchPhotoView(uri);
            }
        });
    }

    private void setupAutoComplete(AutoCompleteTextView textView, Spinner typeSpinner) {
        textView.setThreshold(1); // Start suggesting after first character

        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateAutoCompleteAdapter(textView, position == 0 ? existingPersonTags : existingLocationTags);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String prefix = s.toString().toLowerCase();
                Set<String> currentTags = typeSpinner.getSelectedItemPosition() == 0 ? existingPersonTags : existingLocationTags;
                List<String> suggestions = new ArrayList<>();
                for (String tag : currentTags) {
                    if (tag.toLowerCase().startsWith(prefix)) {
                        suggestions.add(tag);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this,
                        android.R.layout.simple_dropdown_item_1line, suggestions);
                textView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateAutoCompleteAdapter(AutoCompleteTextView textView, Set<String> tags) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(tags));
        textView.setAdapter(adapter);
    }

    private void loadExistingTags() {
        List<Album> allAlbums = StorageUtil.loadAlbums(this);
        if (allAlbums != null) {
            for (Album album : allAlbums) {
                for (Photo photo : album.getPhotos()) {
                    for (Photo.Tag tag : photo.getTags()) {
                        if (tag.getType() == Photo.Tag.Type.PERSON) {
                            existingPersonTags.add(tag.getValue());
                        } else {
                            existingLocationTags.add(tag.getValue());
                        }
                    }
                }
            }
        }
    }

    private void performSearch(boolean isAnd) {
        String type1 = tagType1Spinner.getSelectedItem().toString().toUpperCase();
        String value1 = searchValue1.getText().toString().trim();
        String type2 = tagType2Spinner.getSelectedItem().toString().toUpperCase();
        String value2 = searchValue2.getText().toString().trim();

        if (value1.isEmpty() && value2.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search value", Toast.LENGTH_SHORT).show();
            return;
        }

        resultsAdapter.clear();
        List<Photo> results = searchPhotos(isAnd, type1, value1, type2, value2);
        if (results.isEmpty()) {
            Toast.makeText(this, "No matches found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a custom adapter to show photo thumbnails instead of URIs
        PhotoListAdapter photoListAdapter = new PhotoListAdapter(this, results);
        ListView searchResultsList = findViewById(R.id.searchResultsList);
        searchResultsList.setAdapter(photoListAdapter);
        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            Photo photo = results.get(position);
            launchPhotoView(photo.getUri());
        });
    }

    private List<Photo> searchPhotos(boolean and, String type1, String value1, String type2, String value2) {
        List<Album> allAlbums = StorageUtil.loadAlbums(this);
        Set<Photo> resultSet = new HashSet<>();
        
        if (allAlbums != null) {
            for (Album album : allAlbums) {
                for (Photo photo : album.getPhotos()) {
                    boolean match1 = value1.isEmpty() || matchesTag(photo, type1, value1);
                    boolean match2 = value2.isEmpty() || matchesTag(photo, type2, value2);
                    
                    if (and && match1 && match2) {
                        resultSet.add(photo);
                    } else if (!and && (match1 || match2)) {
                        resultSet.add(photo);
                    }
                }
            }
        }
        return new ArrayList<>(resultSet);
    }

    private boolean matchesTag(Photo photo, String type, String value) {
        if (value.isEmpty()) return false;
        
        for (Photo.Tag tag : photo.getTags()) {
            if (tag.getType().toString().equals(type) &&
                tag.getValue().toLowerCase().startsWith(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void launchPhotoView(String uri) {
        // Find which album contains this photo
        List<Album> allAlbums = StorageUtil.loadAlbums(this);
        if (allAlbums != null) {
            for (Album album : allAlbums) {
                for (Photo photo : album.getPhotos()) {
                    if (photo.getUri().equals(uri)) {
                        // Launch PhotoActivity with both the photo URI and album name
                        android.content.Intent intent = new android.content.Intent(this, PhotoActivity.class);
                        intent.putExtra("photo_uri", uri);
                        intent.putExtra("album_name", album.getName());
                        startActivity(intent);
                        return;
                    }
                }
            }
        }
    }
} 