# Vista

A home-screen launcher for Android TV and Google TV, written in Kotlin with Jetpack Compose for TV.

Vista replaces the stock launcher with a faster, ad-free home screen that adapts its theme to the
room and surfaces content from the apps already installed on the device. It was written for a
projector that is unreadable under the stock dark-only interface in daylight.

![Home](docs/screenshots/home.png)

## Overview

Vista is built around three goals:

- **Readability.** The theme follows the system day/night state — light during the day for projector
  use, dark at night.
- **Relevant content.** Continue Watching, per-app content rows, and a Movies/Shows library are read
  directly from the device's TV provider, with no recommendation feed and no advertising.
- **No external dependencies.** Vista reads only local state. It requires no account and sends
  nothing off the device.

Targets API 34 and runs on a 1 GB-RAM Google TV stick.

## Features

- Tabbed navigation — For you, Movies, Shows, Apps, Library — with a segmented selector and
  integrated search.
- An immersive hero that follows focus, showing artwork, synopsis, season/episode, and remaining
  runtime.
- Continue Watching and per-provider content rows sourced from `TvContractCompat`, each launched
  through the publishing app's own deep link.
- A fixed focus indicator: the selection stays at a fixed position while the row scrolls beneath it,
  and follows the card once the row reaches its end.
- Recently launched apps, pinned favourites, and a full application grid that includes sideloaded
  apps the stock launcher omits.
- A quick-settings panel on the MENU button: theme control, system shortcuts, a MediaRouter
  audio-output selector, and live notifications.
- Notification overlays presented over foreground apps.

## Screenshots

| Home | Movies | Quick settings |
| --- | --- | --- |
| ![Home](docs/screenshots/home.png) | ![Movies](docs/screenshots/movies.png) | ![Quick settings](docs/screenshots/quick-settings.png) |

## Implementation notes

- **Content rows.** Reading another application's published channels and programs requires
  `android.permission.READ_TV_LISTINGS`. The permission is `dangerous` rather than signature-level,
  so it can be granted with `pm grant`. Vista queries `WatchNextPrograms` and `PreviewPrograms` and
  renders posters using each entry's `COLUMN_INTENT_URI`. The system-aggregated watch-next table
  requires `ACCESS_ALL_EPG_DATA` (signature/privileged) and is not used.
- **Recently used.** Google TV does not return `UsageStatsManager` data to third-party launchers, so
  "Jump back in" is derived from Vista's own launch history, persisted with DataStore.
- **Icons.** Application icons are loaded at `DENSITY_XXXHIGH` to stay sharp at large and circular
  sizes.

## Building

Requires JDK 17, the Android SDK (platform 34, build-tools 34.0.0) and `adb`.

```bash
git clone https://github.com/kierandrewett/vista.git
cd vista
echo "sdk.dir=$HOME/Android/Sdk" > local.properties   # or set ANDROID_HOME
./gradlew :app:assembleRelease
```

The release variant is non-debuggable and noticeably smoother than debug on low-memory hardware. It
is signed with the debug key so it installs without additional configuration. The APK is produced at
`app/build/outputs/apk/release/app-release.apk`, and is also built and uploaded as an artifact by
GitHub Actions on every push.

## Installation

Enable Wireless debugging on the device, then:

```bash
adb connect <device-ip>:<port>
adb install -r app/build/outputs/apk/release/app-release.apk
```

Several capabilities depend on permissions a launcher cannot request at runtime. Grant them once
over adb:

```bash
PKG=dev.drewett.vista
adb shell pm grant $PKG android.permission.READ_TV_LISTINGS                  # content rows
adb shell appops set $PKG SYSTEM_ALERT_WINDOW allow                          # notification overlays
adb shell cmd notification allow_listener $PKG/$PKG.service.VistaNotificationListenerService
adb shell cmd role add-role-holder android.app.role.HOME $PKG               # set as home screen
```

Pressing Home then opens Vista. To restore the previous launcher, reassign the `HOME` role.

## Architecture

A single Gradle module under `dev.drewett.vista`:

- `data/` — repositories over PackageManager/LauncherApps, the TV provider, usage and launch
  history, favourites, settings, and MediaRouter.
- `domain/` — immutable UI models.
- `ui/` — Compose for TV: the tab shell and immersive home, content grids, search, the
  quick-settings panel, and shared components.
- `service/` — the notification listener and over-app overlay.

## Limitations

- The aggregated system "Watch Next" row is privileged; Vista reconstructs it from per-app channels,
  so coverage depends on which apps publish them.
- Account avatars are not exposed to third-party applications, so none is shown.
- Audio-output switching relies on MediaRouter; only routes the system exposes are selectable.
- adb-granted permissions are per-device. Without them, the corresponding tiles fall back to opening
  system Settings.

## License

[MIT](LICENSE).
