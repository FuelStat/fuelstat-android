# Fuelstat Android – notes for Claude and other agents

Native Android app for Fuelstat (Kotlin, Jetpack Compose). The backend and the
web app live in [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat), the
iOS app in [FuelStat/fuelstat-ios](https://github.com/FuelStat/fuelstat-ios).

**Status: early development (0.1.0, skeleton).** The project builds; sign-in
and the vehicle list work, everything else is still to come. The stack is
described in the README.

## Scope and goal

- **Feature reference:** the iOS feature list
  [FuelStat/fuelstat-ios#15](https://github.com/FuelStat/fuelstat-ios/issues/15)
  describes everything the web app does. The Android app aims for the same
  scope, with native Android extras where they make sense. When development
  starts, create an Android feature reference issue here and link it.
- **Planning:** the Android app is tracked in
  [FuelStat/fuelstat#147](https://github.com/FuelStat/fuelstat/issues/147)
  (milestone "Later"). Once development is scheduled: one milestone per app
  version in this repo (like iOS), backend prerequisites in dated backend
  milestones in FuelStat/fuelstat that are due **before** the app milestone
  needing them, named in the app issue ("Benötigt: FuelStat/fuelstat#…").

## Rules

- **The API is the source of truth.** Consumption, costs, CO₂, due dates and
  plan limits come from the API. Do not re-implement calculations in the app.
  If the API lacks something, open an issue in FuelStat/fuelstat instead.
- **Plan rules are enforced by the API only** (FuelStat/fuelstat#102). The app
  shows limits and API messages, it never decides on its own.
- **External services only through the API** (Tankerkönig, Open Charge Map …),
  no API keys in the app (FuelStat/fuelstat#123).
- API docs: `docs/api.md` and `docs/openapi.1.0.0.yaml` in FuelStat/fuelstat.
  Base URL `https://tanken.1emc.de/api/v1`; self-hosted servers are entered on
  the sign-in screen. Only use `/api/v1`, which stays backward compatible.
- Image URLs from the API are signed and change daily; cache by file name.
- UI language is German; all user-facing strings go into
  `res/values/strings.xml` (no hard-coded text). Code, comments, commit
  messages and this repo's docs are in English.
- Accessibility is part of every feature: TalkBack content descriptions,
  scalable text (sp), sufficient contrast, dark theme, touch targets ≥ 48 dp.
- **Design: follow the shared design document
  [FuelStat/fuelstat `docs/design.md`](https://github.com/FuelStat/fuelstat/blob/main/docs/design.md)**
  (single source of truth for colour, type, spacing and radius tokens,
  components, platform guardrails, accessibility) **and
  [`docs/design.md`](docs/design.md)** here for the Material 3/Compose
  specifics. Token values are changed in the shared document first, never only
  here.
- Security: token only in Android Keystore–backed storage, never in logs;
  HTTPS only (no cleartext traffic, `networkSecurityConfig`); exported
  components only where needed; `android:allowBackup` reviewed.
- Never commit secrets (API keys, tokens, keystores, `keystore.properties`,
  `local.properties`, `google-services.json`).
- No tracking, analytics, ads or third-party SDKs that send data elsewhere.

## Project layout

- `app/` – Android app (package `org.fuelstat.android`): `data/` (session,
  Keystore-encrypted token), `design/` (theme, colour and spacing tokens,
  shared components), `ui/` (screens and ViewModels, one package per area)
- `core/` – plain Kotlin/JVM module (package `org.fuelstat.core`): API client
  (`api/FuelstatApi.kt`, OkHttp + kotlinx.serialization), API models, server
  addresses, image cache keys. Put pure logic here, with unit tests.
- `gradle/libs.versions.toml` – all dependency versions (Dependabot updates them)

## Build and test

Requires JDK 17+ and the Android SDK (`ANDROID_HOME` or `local.properties`).

```sh
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Put pure logic (parsing, formatting, things that are not the API's job) in
`core/` so it can be unit-tested without an emulator. Run tests and
lint before every PR; CI (`.github/workflows/ci.yml`) runs the same.

**Cloud sessions run on Linux** without an emulator. The SDK can be installed
there (`commandlinetools` from dl.google.com, then `platforms;android-37.0`,
`build-tools;37.0.0`), so `assembleDebug`, `test` and `lint` run; UI changes
still need a check on an emulator or device – say so in the PR. If Maven
Central answers 429, retry with `--max-workers=1`.

## Release process

The app has its own version, independent of the web app/API version and of
the iOS app.

- `versionName` (Semantic Versioning, `0.x` during the beta) is what users see;
  `versionCode` must increase with every build that leaves a developer's
  machine (Play testing tracks, Play Store). Both are in `app/build.gradle.kts`.
- **App releases do not bump the backend.** If a release needs a new API
  feature, the backend release (FuelStat/fuelstat, `docs/betrieb.md`) must be
  tagged and deployed first – check `GET /api/v1/health`.
- Tag the merged `main` (`git tag -a vX.Y.Z -m "Fuelstat Android X.Y.Z"`) and
  create the GitHub release; only when the maintainer explicitly asks.
- Signing keys never go into the repository.

## Checklist for every PR

- Issue in the milestone of the app version it ships with.
- README "Status"/"Current state" matches what the app can do.
- API gaps have an issue in FuelStat/fuelstat (no workaround calculations).
- New user-facing text in `strings.xml` (German).
- New screens follow the shared design document (FuelStat/fuelstat
  `docs/design.md`) and `docs/design.md` here.
- New logic has unit tests; CI is green.

## Workflow

- Branch per feature, PR into `main`; reference the issue (`Fixes #…`) and,
  for API work, the FuelStat/fuelstat issue it depends on.
- PRs follow `.github/pull_request_template.md`.
