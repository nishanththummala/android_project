package com.group25.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.*;
import java.util.List;

public class StorageUtil {
    private static final String FILE_NAME = "albums.ser";

    public static void saveAlbums(Context context, List<Album> albums) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE))) {
            oos.writeObject(albums);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Album> loadAlbums(Context context) {
        try (ObjectInputStream ois = new ObjectInputStream(
                context.openFileInput(FILE_NAME))) {
            return (List<Album>) ois.readObject();
        } catch (Exception e) {
            // File not found or error, return null
            return null;
        }
    }

    // Calculates the optimal inSampleSize for loading a scaled down bitmap
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than or equal to the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Decodes a bitmap from a Uri efficiently to fit within reqWidth x reqHeight
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        InputStream inputStream = null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            inputStream = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) { try { inputStream.close(); } catch (IOException ignored) {} }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            return bitmap; // No finally block needed for stream close if we return here

        } catch (Exception e) { // Catch generic exceptions like FileNotFound, SecurityException, OOM
            e.printStackTrace();
            return null;
        } finally {
             if (inputStream != null) {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    }
} 