# Fuelstat Android – notes for Claude and other agents

Native Android app for Fuelstat (Kotlin, Jetpack Compose). The backend and the
web app live in [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat), the
iOS app in [FuelStat/fuelstat-ios](https://github.com/FuelStat/fuelstat-ios).

**Status: early development (0.1.0, skeleton).** The project builds; sign-in
and the vehicle list work, everything else is still to come. The stack is
described in the README, the plan in [#7](https://github.com/FuelStat/fuelstat-android/issues/7).

## Scope and goal

- **Feature reference: [#7](https://github.com/FuelStat/fuelstat-android/issues/7).**
  The app must cover everything the web app does (version 1.0.0), and do more
  where native Android allows it. It mirrors the iOS list
  [FuelStat/fuelstat-ios#15](https://github.com/FuelStat/fuelstat-ios/issues/15).
  Read #7 before starting a feature; tick items off there and link the issue or
  PR when done. If the web app gains a feature, add it to #7 (and to the iOS
  list).
- **Planning: one milestone per app version in this repo** (`0.1.0 - MVP`
  … `1.0.0 - Google Play Store`, plus `Later`), see the README section
  "Roadmap". Every open issue has a milestone; new issues get one when they are
  created. The milestones have no dates yet.
- Backend prerequisites stay in FuelStat/fuelstat, in a dated backend
  milestone that is due **before** the app milestone needing them, and are
  named in the app issue ("Benötigt: FuelStat/fuelstat#…"). The Android app is
  tracked in FuelStat/fuelstat#147 (milestone "Later"); once dates exist, hang
  the app issues there as sub-issues like the iOS ones under #116 and keep the
  schedule current. Open backend items today: FuelStat/fuelstat#193 (push), FuelStat/fuelstat#194
  (`assetlinks.json`).

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
  **Do not add dependencies that need Google Play Services or a Google account
  or project** (Firebase/FCM, Play Billing, ML Kit, Gemini/AICore) **without
  asking first** – they wait for the decision in
  [#38](https://github.com/FuelStat/fuelstat-android/issues/38), and the
  FOSS-friendly build depends on it.
- **Do not rename the `applicationId` (`org.fuelstat.android`) or the app name
  on your own.** The ID cannot change after the first Google Play upload, and the
  name is open (FuelStat/fuelstat#151, see #38).

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
  tagged and deployed first – check `GET /api/v1/health` – and the release
  notes say so. Raising `apps.android.minimum_version` in `/health` is an
  operator setting, not part of an app release.
- Signing keys never go into the repository.

Steps:

1. Feature PRs keep the README section "Current state" up to date. The
   planned versions are the milestones; a release ships its milestone.
2. **Prepare** a release PR: bump `versionName` and `versionCode`, update the
   README, add the version to the in-app release notes (German, written for
   users; created with #16), check `docs/app-privacy.md` against what the app
   sends (created with #16), run `./gradlew assembleDebug test lint` and wait
   for the CI.
3. **Test** the release build by hand: work through `docs/testing.md`
   (created with #17) against the hosted service and a self-hosted server on an
   emulator and a real device, and record the result (API version from
   `/health`, failed steps) in the release PR. Until these files exist, say in
   the PR what was and was not tested. The run needs a device, so an agent
   prepares the result block and the maintainer does the run.
4. **Merge** the PR.
5. **Tag** the merged `main` and create the GitHub release (no workflow yet –
   create it on GitHub from the tag, notes = what changed for users, marked
   as pre-release while `0.x`):

   ```sh
   git tag -a vX.Y.Z -m "Fuelstat Android X.Y.Z"
   git push origin vX.Y.Z
   ```

6. **Distribute:** until the decision in #38, only as a debug or self-signed
   APK from the GitHub release or the CI artifact, installed by hand. Play
   testing tracks and the Play Store need the Google Play developer account
   (new personal accounts must run a closed test with 12 testers for 14 days
   first), see #38 and #49.

**Remind the maintainer** at the end of a task when merged changes are not yet
in a tagged release, when a release PR is merged but its tag is missing, or
when the app depends on a backend change that is merged but not yet released
and deployed. Only create or push tags when the maintainer explicitly asks.

## Checklist for every PR

Check before opening the PR, and remind the maintainer at the end of a task of
anything still missing:

- **Milestone:** the issue (and the PR) is in the milestone of the app
  version it ships with.
- **Feature reference:** items in #7 ticked off (with a link to the PR) or
  added when the web app gained something new.
- **README:** section "Current state" matches what the app can do.
- **API gaps:** anything the app needs but the API does not provide has an
  issue in FuelStat/fuelstat (no workaround calculations in the app); the PR
  names it.
- **Backend dependency:** if the PR relies on a backend change, that change is
  merged, released (tag) and deployed – check `GET /api/v1/health` – before the
  app release that needs it.
- **Strings:** new user-facing text is in `strings.xml` (German).
- **Design:** new screens follow the shared design document
  (FuelStat/fuelstat `docs/design.md`) and `docs/design.md` here.
- **Tests:** new logic in `core/` has tests; CI is green. UI changes were not
  run in the cloud session (no emulator) – say so in the PR instead of
  claiming a device test.
- **Test checklist:** new or changed features have their steps in
  `docs/testing.md` (once it exists, #17).
- **Dependencies:** nothing from the "ask first" rule above was added.

## Workflow

- Branch per feature, PR into `main`; reference the issue (`Fixes #8`) and,
  for API work, the FuelStat/fuelstat issue it depends on. Work on the earliest
  open milestone first.
- Delete the head branch after the merge.
- PRs follow `.github/pull_request_template.md`.
- Keep the README section "Current state" in sync with what the app can do.
