package com.group25.photos;

import android.content.Context;
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
} 