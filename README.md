# WOSAssist

**WOSAssist** is an Android application designed to streamline order management. Users can securely sign in/out, create new orders (with image uploads), search for orders, and view order details. Firebase is used for authentication, Firestore, and Storage.

## Demo Video

A short demonstration of the app is available on YouTube:

👉 **[https://youtube.com/shorts/0iee9Bed-s8?feature=share](https://youtube.com/shorts/0iee9Bed-s8?feature=share)**

---

## Features

* User authentication (sign in / sign out)
* Create orders with images from device gallery
* Search orders with full-text search
* Detailed order pages
* Firebase backend (Auth + Firestore + Storage)

## Screenshots

<img src="docs/images/A1_01_Startseite_und_Suchfeld.jpg" width="300">
<img src="docs/images/A1_02_Auftragserstellung.jpg" width="300">
<img src="docs/images/A1_03_Detailansicht.jpg" width="300">
<img src="docs/images/A1_04_Anmeldeseite.jpg" width="300">

---

## Tech Stack

* Java (Android)
* XML UI Layouts
* Firebase Auth + Firestore + Storage
* Gradle (Kotlin DSL)
* Min / Target SDK customizable

## Build & Run (Developer Setup)

1. Clone the repository.
2. Add your `app/google-services.json` to the `app/` folder (not included in repo).
3. Open the project in Android Studio.
4. Build or run using the Run button or `./gradlew assembleDebug`.

## APK / Demo

A signed or debug APK plus demo recording is available in the GitHub **Releases**.

## Security & Privacy

Sensitive keys like `google-services.json` are **not committed** and must be added manually for testing.
