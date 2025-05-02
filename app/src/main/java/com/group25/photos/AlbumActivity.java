package com.group25.photos;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements PhotoAdapter.PhotoActionListener {
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
        setTitle(albumName != null ? albumName : "Album");

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        FloatingActionButton addPhotoFab = findViewById(R.id.addPhotoFab);

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
            Toast.makeText(this, "Error: Album not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        photos = currentAlbum.getPhotos();
        photoAdapter = new PhotoAdapter(this, photos, this);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photosRecyclerView.setAdapter(photoAdapter);

        addPhotoFab.setOnClickListener(v -> pickPhoto());

        checkIfEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only reload to check if current album still exists or was renamed externally
        List<Album> potentiallyUpdatedAlbums = StorageUtil.loadAlbums(this);
        if (potentiallyUpdatedAlbums == null) potentiallyUpdatedAlbums = new ArrayList<>();

        Album foundAlbum = null;
        // Use the original albumName passed via intent to find it again
        // Ensure albumName is not null before comparing
        if (albumName != null) {
            for (Album a : potentiallyUpdatedAlbums) {
                if (albumName.equals(a.getName())) { // Safe comparison
                    foundAlbum = a;
                    break;
                }
            }
        }

        if (foundAlbum == null) {
            // The album we were viewing is gone or albumName was null initially
            Toast.makeText(this, "Album '" + albumName + "' no longer exists or could not be loaded.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        } else {
            // Album still exists. Update our master list reference.
            this.allAlbums = potentiallyUpdatedAlbums;
            this.currentAlbum = foundAlbum;
            
            // Ensure the 'photos' list reference is the one from the current album instance
            // This handles cases where the album object might have been replaced during load
            this.photos = this.currentAlbum.getPhotos();

            // Update the adapter ONLY if its internal list is not the same instance
            // OR just use notifyDataSetChanged to be safe and simple.
            // Let's stick to notifyDataSetChanged for simplicity.
            if (photoAdapter != null) {
                // We need to update the adapter's internal list reference in case 'photos' was reassigned.
                // A simple way is to create a new adapter, but better is to update the existing one if possible.
                // Since PhotoAdapter doesn't have a method to update its list, we'll recreate it here.
                // Alternatively, add an updateList method to PhotoAdapter.
                 photoAdapter = new PhotoAdapter(this, this.photos, this);
                 photosRecyclerView.setAdapter(photoAdapter);
                // If PhotoAdapter had an updateList(List<Photo> newPhotos) method:
                // photoAdapter.updateList(this.photos);
                // photoAdapter.notifyDataSetChanged();
            }
            
            checkIfEmpty(); // Check empty state after potential refresh
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            
            // Check if photo already exists in *this specific* album
            boolean existsInCurrentAlbum = false;
            for (Photo p : currentAlbum.getPhotos()) {
                 if (p.getUri().equals(uri.toString())) {
                     existsInCurrentAlbum = true;
                     break;
                 }
            }
            if (existsInCurrentAlbum) {
                 Toast.makeText(this, "This photo already exists in this album.", Toast.LENGTH_SHORT).show();
                 return; // Only prevent adding if it exists in the *current* album
            }

            // Persist URI permission
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error accessing photo. Please select from a different location.", Toast.LENGTH_LONG).show();
                return; // Don't proceed if permission fails
            }
            // Create a new Photo object for this album instance
            Photo newPhoto = new Photo(uri.toString()); 
            currentAlbum.addPhoto(newPhoto);
            photoAdapter.notifyItemInserted(photos.size() - 1); // photos list is already updated via currentAlbum
            StorageUtil.saveAlbums(this, allAlbums);
            checkIfEmpty();
            Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPhotoClick(int position) {
         if (position >= 0 && position < photos.size()) {
             Photo photo = photos.get(position);
             Intent intent = new Intent(this, PhotoActivity.class);
             intent.putExtra("photo_uri", photo.getUri());
             intent.putExtra("album_name", albumName);
             startActivity(intent);
         }
    }

    @Override
    public void onRemovePhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Photo")
                    .setMessage("Are you sure you want to remove this photo from the album?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        photos.remove(position);
                        photoAdapter.notifyItemRemoved(position);
                        photoAdapter.notifyItemRangeChanged(position, photos.size());
                        StorageUtil.saveAlbums(this, allAlbums);
                        checkIfEmpty();
                        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void onMovePhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            Photo photoToMove = photos.get(position);
            showMovePhotoDialog(photoToMove, position);
        }
    }

    private void showMovePhotoDialog(Photo photo, int position) {
        List<String> otherAlbumNames = new ArrayList<>();
        for (Album a : allAlbums) {
            if (!a.getName().equals(albumName)) {
                otherAlbumNames.add(a.getName());
            }
        }

        if (otherAlbumNames.isEmpty()) {
            Toast.makeText(this, "No other albums exist to move this photo to.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] namesArray = otherAlbumNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Move Photo To Album")
                .setItems(namesArray, (dialog, which) -> {
                    String destinationAlbumName = namesArray[which];
                    Album destinationAlbum = null;
                    for (Album a : allAlbums) {
                        if (a.getName().equals(destinationAlbumName)) {
                            destinationAlbum = a;
                            break;
                        }
                    }

                    if (destinationAlbum != null) {
                        boolean existsInDest = false;
                        for (Photo p : destinationAlbum.getPhotos()) {
                            if (p.getUri().equals(photo.getUri())) {
                                existsInDest = true;
                                break;
                            }
                        }

                        if (existsInDest) {
                            Toast.makeText(this, "Photo already exists in '" + destinationAlbumName + "'", Toast.LENGTH_SHORT).show();
                        } else {
                            destinationAlbum.addPhoto(new Photo(photo)); 
                            photos.remove(position); // photos is linked to currentAlbum.getPhotos()
                            photoAdapter.notifyItemRemoved(position);
                            photoAdapter.notifyItemRangeChanged(position, photos.size());
                            StorageUtil.saveAlbums(this, allAlbums);
                            checkIfEmpty();
                            Toast.makeText(this, "Photo moved to '" + destinationAlbumName + "'", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: Destination album not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkIfEmpty() {
        TextView emptyView = findViewById(R.id.emptyAlbumTextView);
        if (photos.isEmpty()) {
            photosRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            photosRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    public String getAlbumName() {
        return albumName;
    }

    // Handle Up button press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to parent activity (MainActivity as defined in Manifest)
            finish(); // Simple finish() is usually sufficient if parent is standard back stack
            // Or use NavUtils if more complex navigation is needed:
            // Intent upIntent = NavUtils.getParentActivityIntent(this);
            // NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}