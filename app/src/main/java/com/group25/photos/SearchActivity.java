package com.group25.photos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        EditText searchTagType = findViewById(R.id.searchTagType);
        EditText searchTagValue = findViewById(R.id.searchTagValue);
        EditText searchTagType2 = findViewById(R.id.searchTagType2);
        EditText searchTagValue2 = findViewById(R.id.searchTagValue2);
        Button searchAndButton = findViewById(R.id.searchAndButton);
        Button searchOrButton = findViewById(R.id.searchOrButton);
        ListView searchResultsList = findViewById(R.id.searchResultsList);

        ArrayAdapter<String> resultsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        searchResultsList.setAdapter(resultsAdapter);

        searchAndButton.setOnClickListener(v -> {
            resultsAdapter.clear();
            List<Photo> results = searchPhotos(true, searchTagType, searchTagValue, searchTagType2, searchTagValue2);
            for (Photo p : results) {
                resultsAdapter.add(p.getUri());
            }
            resultsAdapter.notifyDataSetChanged();
        });

        searchOrButton.setOnClickListener(v -> {
            resultsAdapter.clear();
            List<Photo> results = searchPhotos(false, searchTagType, searchTagValue, searchTagType2, searchTagValue2);
            for (Photo p : results) {
                resultsAdapter.add(p.getUri());
            }
            resultsAdapter.notifyDataSetChanged();
        });
    }

    private List<Photo> searchPhotos(boolean and, EditText type1, EditText value1, EditText type2, EditText value2) {
        String t1 = type1.getText().toString().trim().toUpperCase();
        String v1 = value1.getText().toString().trim().toLowerCase();
        String t2 = type2.getText().toString().trim().toUpperCase();
        String v2 = value2.getText().toString().trim().toLowerCase();
        List<Album> allAlbums = StorageUtil.loadAlbums(this);
        Set<Photo> resultSet = new HashSet<>();
        if (allAlbums != null) {
            for (Album a : allAlbums) {
                for (Photo p : a.getPhotos()) {
                    boolean match1 = false, match2 = false;
                    for (Photo.Tag tag : p.getTags()) {
                        if (tag.getType().toString().equals(t1) && tag.getValue().toLowerCase().contains(v1)) match1 = true;
                        if (tag.getType().toString().equals(t2) && tag.getValue().toLowerCase().contains(v2)) match2 = true;
                    }
                    if (and && match1 && match2) resultSet.add(p);
                    if (!and && (match1 || match2)) resultSet.add(p);
                }
            }
        }
        return new ArrayList<>(resultSet);
    }
} 