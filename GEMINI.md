# GEMINI.md

## Project Overview

This is a native Android application written in Kotlin. The application is a secure, peer-to-peer messaging application that uses LoRa (Long Range) communication via ESP32 modules for encrypted messaging with GPS location sharing capabilities, as described in the `README.md` file.

- **Package Name:** `com.example.lolnk`
- **Target SDK:** 34
- **Build Tool:** Gradle
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room

## Features

- **Contact List:** The app displays a list of contacts.
- **Add Contact:** Users can add new contacts by providing a name and a node ID.

## Progress

- **Database:** The Room database is set up with `Message` and `Contact` entities, and their corresponding DAOs.
- **Repository:** `ContactRepository` and `MessageRepository` are created to manage data operations.
- **ViewModel:** `ContactViewModel` is implemented to expose contact data to the UI.
- **UI:** The basic UI for the contact list screen is implemented using Jetpack Compose. A dialog for adding new contacts is also implemented.
- **Application:** A custom `Application` class (`LolnkApplication`) is set up to manage application-wide instances of the database and repositories.

## Building and Running

### Building

To build the project from the command line, you can use the Gradle wrapper:

```bash
./gradlew build
```

Alternatively, you can use the build options within Android Studio.

### Running

To run the application, you can either use the "Run" button in Android Studio, which will prompt you to select a device or emulator, or you can install the debug APK from the command line:

```bash
./gradlew installDebug
```

## Development Conventions

*   **Language:** The project is set up to use Kotlin.
*   **Dependencies:** Project dependencies are managed in the `app/build.gradle.kts` file. A version catalog is used in `gradle/libs.versions.toml` to manage dependency versions.
*   **Project Structure:** The project follows the directory structure described in the `README.md` file (`data`, `network`, `repository`, `ui`, `viewmodel`, `util`).
*   **Testing:**
    *   Unit tests are located in `app/src/test/java`.
    *   Instrumented tests are in `app/src/androidTest/java`.
