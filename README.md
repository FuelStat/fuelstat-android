# Fuelstat Android

Native Android application for Fuelstat.

Fuelstat is an open-source vehicle companion for any powertrain: fuel-ups and
charging sessions, consumption, expenses and total cost, maintenance and due
dates, a trip logbook and sharing vehicles with other accounts.

## Status

**Early development – version 0.1.0 (skeleton).** The plan is tracked in
[FuelStat/fuelstat#147](https://github.com/FuelStat/fuelstat/issues/147); the feature
list is [#7](https://github.com/FuelStat/fuelstat-android/issues/7), see "Roadmap" below.

### Current state

- Sign-in with e-mail and password, two-factor code (TOTP), hosted or
  self-hosted server (HTTPS only)
- Session kept on the device; the token is encrypted with an Android Keystore
  key; expired sessions lead back to the sign-in
- Navigation Start · Einträge · Statistik · Profil (navigation bar on phones,
  rail on large screens); "+" button prepared
- Start: list of the account's vehicles (pull to refresh)
- Profil: account, server, app version, sign out
- Light and dark theme with the Fuelstat colours, German UI

Not yet: entries, fill-ups and charging, statistics, vehicle details and
editing, photos, offline queue, widgets – see the iOS feature reference
[FuelStat/fuelstat-ios#15](https://github.com/FuelStat/fuelstat-ios/issues/15).

## Tech stack

- Kotlin, Jetpack Compose, Material 3 (dynamic colour off, brand colours)
- minSdk 26 (Android 8.0), compileSdk/targetSdk 37
- Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`),
  Android Gradle Plugin 9 (built-in Kotlin)
- Networking: OkHttp + kotlinx.serialization in the plain Kotlin module
  `core` (unit-tested with MockWebServer)
- Storage: DataStore; the token encrypted with an Android Keystore key
- Architecture: single activity, Navigation Compose with type-safe routes,
  ViewModels, coroutines/Flow; manual dependency injection (`AppContainer`)
- Planned: Room for the offline queue, widgets (Glance), App Shortcuts,
  Android Auto, managed configurations (AppConfig, see
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

## Roadmap

Planning uses the [milestones](https://github.com/FuelStat/fuelstat-android/milestones)
of this repository, one per app version. The app version is independent of the
web app/API version and of the iOS app; what the app needs from the API is
planned in [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat/milestones),
before the app version that needs it. The full feature list is
[#7](https://github.com/FuelStat/fuelstat-android/issues/7). The milestones
have no dates yet.

| Version | Focus |
|---|---|
| [0.1.0 - MVP](https://github.com/FuelStat/fuelstat-android/milestone/1) | Everyday core: device session, vehicles, fill-ups, entries, start screen with due dates |
| [0.2.0 - Foundation](https://github.com/FuelStat/fuelstat-android/milestone/2) | Sign-in and account complete (registration, 2FA, devices, delete account), imprint and privacy, test checklist |
| [0.3.0 - Capture](https://github.com/FuelStat/fuelstat-android/milestone/3) | Record everything online: charging, expenses, odometer, logbook, editing, search, archive, App Links |
| [0.4.0 - Insights & Reminders](https://github.com/FuelStat/fuelstat-android/milestone/4) | Statistics, costs, total cost of ownership, service history, local reminders, plan, onboarding, sharing |
| [0.5.0 - Native](https://github.com/FuelStat/fuelstat-android/milestone/5) | More than the web: offline, app lock, shortcuts, Gemini/Assistant, large screens, Google Play decisions |
| [0.6.0 - Glance](https://github.com/FuelStat/fuelstat-android/milestone/6) | Widgets, camera recognition, export and report, Android Auto |
| [1.0.0 - Google Play Store](https://github.com/FuelStat/fuelstat-android/milestone/7) | Parity with the web app: CSV import, push, passkeys, comparison, budget, admin; quality review and Play release |
| [Later](https://github.com/FuelStat/fuelstat-android/milestone/8) | Live trips, Wear OS, Play Billing, AppConfig, Android Automotive OS, UI tests, documents, fleet |

Guideline: first a complete, reliable app for everyday use, then what only a
native app can do. Features that need Google Play Services or the Play
developer account wait for the decision in
[#38](https://github.com/FuelStat/fuelstat-android/issues/38).

## Development

Requirements:

- Android Studio (current stable) with the Android SDK (API 37)
- JDK 17 or later
- A Fuelstat account (hosted or self-hosted)

Open the project folder in Android Studio and run the `app` configuration, or
from the command line (debug builds install as `org.fuelstat.android.debug`,
next to a release build):

```sh
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Rules for contributors and AI agents are in [`CLAUDE.md`](CLAUDE.md); the
shared design is in
[FuelStat/fuelstat `docs/design.md`](https://github.com/FuelStat/fuelstat/blob/main/docs/design.md),
the Android specifics in [`docs/design.md`](docs/design.md).

## Security

Please report vulnerabilities privately, see [`SECURITY.md`](SECURITY.md).

## License

[GNU Affero General Public License v3.0](LICENSE), like the rest of Fuelstat.
