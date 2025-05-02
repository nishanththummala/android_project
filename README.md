# Android Photos App

This is an Android port of the JavaFX Photos project for CS213 Assignment 4.
Group 25: Nishanth Thummala, Hanandi Nunna

## How to Import and Run

1. Open **Android Studio**.
2. Select **Clone Repository** and enter the URL https://github.com/nishanththummala/android_project.
3. Let Gradle sync and build the project (it may take a few minutes on first run).
4. Set up an emulator (API 34, 1080x2400, Pixel 6 recommended) or connect a real device.
5. Click **Run** to build and launch the app.

##Functionality
- Home screen with persistence over multiple opens.
  - Open, create, delete, and rename albums
- Once an album is open, you can add, remove, and click on the photo to display the photo
- When a photo is displayed:
- You can select the previous or next button to slideshow through the photos in the album
- Add a tag to a photo (location or person)
- You can also move a photo from one album to another album
- Finally, you can search for photos by tag-value pairs (conjunction and disjunction with autocomplete)

##Notes
- All UI is built with Android XML layouts
- No third-party image libraries (Picasso, Glide, etc.) are used
  
