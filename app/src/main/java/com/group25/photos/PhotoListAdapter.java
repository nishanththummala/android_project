package com.group25.photos;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PhotoListAdapter extends ArrayAdapter<Photo> {
    private Context context;
    private List<Photo> photos;

    public PhotoListAdapter(Context context, List<Photo> photos) {
        super(context, R.layout.photo_list_item, photos);
        this.context = context;
        this.photos = photos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.photo_list_item, parent, false);
        }

        Photo photo = photos.get(position);

        ImageView thumbnailView = convertView.findViewById(R.id.photoThumbnail);
        TextView tagInfoView = convertView.findViewById(R.id.photoTagInfo);

        // Set the image thumbnail
        try {
            thumbnailView.setImageURI(Uri.parse(photo.getUri()));
        } catch (Exception e) {
            thumbnailView.setImageResource(R.drawable.photo_placeholder);
        }

        // Set tag information
        List<Photo.Tag> tags = photo.getTags();
        StringBuilder tagInfo = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            Photo.Tag tag = tags.get(i);
            tagInfo.append(tag.getType().toString())
                    .append(": ")
                    .append(tag.getValue());

            if (i < tags.size() - 1) {
                tagInfo.append(", ");
            }
        }
        tagInfoView.setText(tagInfo.toString());

        return convertView;
    }
}