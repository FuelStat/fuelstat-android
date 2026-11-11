# Fuelstat Android

Native Android application for Fuelstat.

Fuelstat is an open-source vehicle companion for any powertrain: fuel-ups and
charging sessions, consumption, expenses and total cost, maintenance and due
dates, a trip logbook and sharing vehicles with other accounts.

## Status

**Planning – no app code yet.** This repository holds the groundwork
(documentation, rules, CI scaffolding). Development starts once the iOS app
has reached a stable feature set; the plan is tracked in
[FuelStat/fuelstat#147](https://github.com/FuelStat/fuelstat/issues/147)
(milestone "Later").

## Planned tech stack

A proposal, to be confirmed when development starts:

- Kotlin, Jetpack Compose, Material 3 (dynamic colour, dark theme)
- minSdk 26 (Android 8.0), targetSdk = latest stable API level
- Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`)
- Networking: Ktor client or Retrofit + OkHttp, kotlinx.serialization
- Storage: DataStore for settings, Room for the offline queue,
  the token in Android Keystore–backed encrypted storage
- Architecture: single activity, Compose Navigation, ViewModels,
  coroutines/Flow; pure logic in a separate Kotlin module with unit tests
- Optional later: widgets (Glance), App Shortcuts, Android Auto,
  managed configurations (AppConfig, see
  [FuelStat/fuelstat-ios#47](https://github.com/FuelStat/fuelstat-ios/issues/47))

## Backend

The app uses the Fuelstat REST API – the same API as the web app and the iOS
app. Default server `https://tanken.1emc.de/api/v1`; self-hosted servers can be
entered on the sign-in screen.

- Backend and web app: [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat)
- API documentation: [`docs/api.md`](https://github.com/FuelStat/fuelstat/blob/main/docs/api.md)
  and [`docs/openapi.1.0.0.yaml`](https://github.com/FuelStat/fuelstat/blob/main/docs/openapi.1.0.0.yaml)
- iOS app (feature reference and design):
  [FuelStat/fuelstat-ios](https://github.com/FuelStat/fuelstat-ios)

## Development

Requirements (once the project exists):

- Android Studio (current stable) with the Android SDK
- JDK 17 or later
- A Fuelstat account (hosted or self-hosted)

Build and test:

```sh
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Rules for contributors and AI agents are in [`CLAUDE.md`](CLAUDE.md); the
design direction is in [`docs/design.md`](docs/design.md).

## Security

Please report vulnerabilities privately, see [`SECURITY.md`](SECURITY.md).

## License

[GNU Affero General Public License v3.0](LICENSE), like the rest of Fuelstat.
