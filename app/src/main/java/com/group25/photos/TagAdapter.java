package com.group25.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class TagAdapter extends ArrayAdapter<Photo.Tag> {

    private Context context;
    private List<Photo.Tag> tags;
    private OnTagDeleteListener deleteListener;

    // Listener interface for delete button clicks
    public interface OnTagDeleteListener {
        void onTagDelete(int position);
    }

    public TagAdapter(@NonNull Context context, @NonNull List<Photo.Tag> tags, OnTagDeleteListener listener) {
        super(context, R.layout.item_tag, tags);
        this.context = context;
        this.tags = tags;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false);
        }

        Photo.Tag currentTag = tags.get(position);

        TextView tagTextView = listItem.findViewById(R.id.tagTextView);
        ImageButton deleteButton = listItem.findViewById(R.id.deleteTagButton);

        // Set the tag text
        tagTextView.setText(currentTag.getType() + ": " + currentTag.getValue());

        // Set listener for the delete button
        deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTagDelete(position);
            }
        });
        
        // Make button focusable to ensure it captures clicks properly
        deleteButton.setFocusable(false);
        deleteButton.setFocusableInTouchMode(false);


        return listItem;
    }
} 