# Android Photos App

This is the Android port of the JavaFX Photos project for CS213 Assignment 4.  
**Group 25**: Nishanth Thummala, Hanandi Nunna

## How to Import and Run

1. Open **Android Studio**.
2. Select **Clone Repository** and enter the URL: `https://github.com/nishanththummala/android_project`.
3. Let Gradle sync and build the project (this may take a few minutes on the first run).
4. Set up an emulator (recommended: API 34, 1080x2400 resolution, Pixel 6) or connect a real device.
5. Click **Run** to build and launch the app.

## Features

- **Home Screen**:
  - Persistent data across multiple sessions.
  - Create, open, delete, and rename albums.
- **Album/Photo Management**:
  - Add and remove photos.
  - Move photos between albums.
- **Photo Viewer**:
  - Click on photos to display them.
  - Navigate through photos using "Previous" and "Next" buttons for slideshow functionality.
  - Add tags to photos (e.g., location or person).
- **Search**:
  - Search photos by tag-value pairs.
  - Supports conjunction and disjunction operations with autocomplete.

## Notes

- All UI components are built using Android XML layouts.
- No third-party image libraries (e.g., Picasso, Glide) are used.
