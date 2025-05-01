# Android Photos App

This is an Android port of the JavaFX Photos project for CS213 Assignment 4.

## How to Import and Run

1. Open **Android Studio**.
2. Select **Open an existing project** and choose the `android` folder in this repository.
3. Let Gradle sync and build the project (it may take a few minutes on first run).
4. Set up an emulator (API 34, 1080x2400, Pixel 6 recommended) or connect a real device.
5. Click **Run** to build and launch the app.

## Project Structure
- `MainActivity`: Home screen, shows albums
- `AlbumActivity`: Shows photos in an album
- `PhotoActivity`: View/edit a single photo and tags
- `SearchActivity`: Search photos by tag-value pairs
- `Album`, `Photo`: Data models
- `AlbumAdapter`, `PhotoAdapter`: RecyclerView adapters

## Notes
- All UI is built with Android XML layouts
- No third-party image libraries (Picasso, Glide, etc.) are used
- Data persistence and photo picking are marked as TODOs

## GenAI Usage
Some code and structure was generated with the help of GenAI (ChatGPT). All code was reviewed and adapted for this project. 