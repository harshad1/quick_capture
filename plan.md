# QLog Plan

## Goal
Build a thin native Android app called `QLog` that lets the user:

- Capture a photo via a system camera intent and save it into a configured SAF folder.
- Record audio via a delegated system recorder intent and save it into a configured SAF folder.
- Import shared images and audio into those same folders with minimal UI and a confirmation toast.
- Configure photo downsampling and target folders from a simple settings screen.

## Product Decisions

- Platform: Kotlin, native Android, Jetpack Compose, Material 3.
- Android baseline: Android 10+.
- Main UI: two large rounded buttons; portrait uses a vertical stack, landscape uses a two-column layout.
- Settings: single simple screen with photo scale, photo folder picker, and audio folder picker.
- Storage: Storage Access Framework with persisted URI permissions.
- Photo scale presets: `1x`, `0.5x`, `0.25x`.
- Audio capture: delegate with `MediaStore.Audio.Media.RECORD_SOUND_ACTION`; preserve returned format.
- Share handling: single share target supporting both single and multiple image/audio items.
- Missing configuration behavior: show a minimal error/toast and do not launch folder selection inline.
- Naming: ISO-style timestamp filenames with `_img` and `_rec` postfixes; add numeric suffix on collision.
- Persistence: local app storage only.

## Architecture

- `MainActivity`
  - Hosts the main screen.
  - Receives standard launches.
- `ShareImportActivity`
  - Thin entry activity for `ACTION_SEND` and `ACTION_SEND_MULTIPLE`.
  - Imports supported URIs immediately, shows a toast, and finishes.
- `SettingsActivity`
  - Hosts simple settings UI.
- `AppPreferences`
  - Stores photo scale and persisted SAF folder URIs in `SharedPreferences`.
- `CaptureCoordinator`
  - Prepares temp/output URIs and orchestrates capture results.
- `ImportRepository`
  - Copies incoming content URIs into the selected SAF destination.
- `ImageDownsampler`
  - Reads captured/shared images and writes downsampled output when scale is not `1x`.
- `SafWriter`
  - Creates destination documents, resolves collisions, and streams bytes.

## Implementation Phases

### Phase 1
- Scaffold Gradle Android app and Compose setup.
- Add app theme and main screen UI.
- Add settings screen shell and preference storage.

### Phase 2
- Implement photo capture with system intent and temp file handling.
- Implement downsampling and SAF save flow.
- Implement audio recording delegation and SAF import flow.

### Phase 3
- Implement share-intent import for single and multiple items.
- Add MIME filtering for image/audio only.
- Add result toasts and configuration guards.

### Phase 4
- Verify manifest, file provider, persisted SAF permissions, and orientation behavior.
- Run available Gradle checks if environment permits.

## Expected Tradeoffs

- Audio format consistency is not enforced because the app delegates recording to external apps.
- Camera and recorder UX may vary by device because the app intentionally stays thin.
- Share import remains intentionally minimal and non-interactive.
