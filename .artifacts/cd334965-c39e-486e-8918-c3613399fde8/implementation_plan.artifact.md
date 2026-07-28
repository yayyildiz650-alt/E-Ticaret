# Fix Firebase App Check Initialization Error

The current code fails because Firebase App Check initialization logic is placed directly inside a `LazyColumn` DSL block in `MainActivity.kt`. This is not a valid location for initialization logic in Jetpack Compose, and it also lacks the necessary Play Integrity dependency to resolve the classes.

## User Review Required

> [!IMPORTANT]
> I am moving the App Check initialization to `onCreate` in `MainActivity`. This ensures it runs once when the app starts, rather than every time the UI list is composed. I am also adding the missing Play Integrity dependency.

## Proposed Changes

### Dependencies

#### [MODIFY] [build.gradle.kts](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/build.gradle.kts)
- Add `com.google.firebase:firebase-appcheck-playintegrity` dependency.
- (Optional) Clean up Firebase dependencies to use the BOM.

### UI and Initialization

#### [MODIFY] [MainActivity.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/MainActivity.kt)
- Remove the misplaced App Check logic from `A101MainScreenContent`.
- Add a new private function `initFirebaseAppCheck()` to `MainActivity`.
- Call `initFirebaseAppCheck()` inside `onCreate`.
- Properly use `DebugAppCheckProviderFactory` for debug builds and `PlayIntegrityAppCheckProviderFactory` for release builds.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure all classes are resolved and the app builds.

### Manual Verification
- Verify that the app starts without crashing.
- Check Logcat for "App Check" related logs to ensure it's initializing correctly.
