# AGENTS.md

## Project

- Name: `QLog`
- Platform: native Android
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Min SDK: 29 (Android 10)
- Compile/Target SDK: 35

## Product Behavior

- Main screen has two large actions:
  - `Take Photo`: launches the system camera app and imports the captured photo into the configured SAF folder.
  - `Record Audio`: launches a system/external recorder via `MediaStore.Audio.Media.RECORD_SOUND_ACTION` and imports the returned audio file into the configured SAF folder.
- Settings are minimal:
  - Photo scale: `1x`, `0.5x`, `0.25x`
  - Photo folder via SAF
  - Audio folder via SAF
- Shared content:
  - App accepts `image/*` and `audio/*`
  - Supports both `ACTION_SEND` and `ACTION_SEND_MULTIPLE`
  - Imports immediately, then shows a toast and finishes
- Missing configuration:
  - Do not prompt inline for folders
  - Show `Settings not configured`
- Filenames:
  - Timestamp-first for sortability
  - Photo suffix `_img`
  - Audio suffix `_rec`
  - Numeric suffix appended on collision
  - Current on-disk timestamp format uses `yyyy-MM-dd'T'HH-mm-ss` to avoid path issues with `:`

## Key Files

- [plan.md](/Users/harshad/code/claw_logger/plan.md): build plan and scope
- [app/build.gradle.kts](/Users/harshad/code/claw_logger/app/build.gradle.kts): Android module config and signing config
- [app/src/main/AndroidManifest.xml](/Users/harshad/code/claw_logger/app/src/main/AndroidManifest.xml): activities, share target, file provider
- [app/src/main/java/com/claw/logger/MainActivity.kt](/Users/harshad/code/claw_logger/app/src/main/java/com/claw/logger/MainActivity.kt): main UI and delegated capture flows
- [app/src/main/java/com/claw/logger/SettingsActivity.kt](/Users/harshad/code/claw_logger/app/src/main/java/com/claw/logger/SettingsActivity.kt): settings and SAF folder picking
- [app/src/main/java/com/claw/logger/ShareImportActivity.kt](/Users/harshad/code/claw_logger/app/src/main/java/com/claw/logger/ShareImportActivity.kt): import shared images/audio
- [app/src/main/java/com/claw/logger/importing/ImportRepository.kt](/Users/harshad/code/claw_logger/app/src/main/java/com/claw/logger/importing/ImportRepository.kt): import/downsample logic
- [app/src/main/java/com/claw/logger/storage/SafWriter.kt](/Users/harshad/code/claw_logger/app/src/main/java/com/claw/logger/storage/SafWriter.kt): SAF writes and filename collision handling

## Build Commands

- Debug APK: `./gradlew :app:assembleDebug`
- Release APK: `./gradlew :app:assembleRelease`

## Signing

- Release signing reads from `local.properties`
- Expected local properties keys:
  - `signing.storeFile`
  - `signing.storePassword`
  - `signing.keyAlias`
  - `signing.keyPassword`
- Do not commit `local.properties`

## Constraints

- Keep the app thin. Prefer system intents over in-app camera/audio implementations.
- Use SAF persisted URI permissions for destination folders.
- Do not add backup/sync/cloud behaviors unless explicitly requested.
- Preserve returned audio format rather than transcoding.
- Keep share-import UI minimal.

## Workflow Notes

- Validate builds with Gradle after meaningful Android changes.
- Prefer release-safe changes over cosmetic refactors.
- If changing storage or share handling, test both direct capture and inbound share flows.
