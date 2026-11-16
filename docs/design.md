# Fuelstat Android – design

**The shared design lives in
[FuelStat/fuelstat `docs/design.md`](https://github.com/FuelStat/fuelstat/blob/main/docs/design.md)**
(German): direction, brand, colour tokens with light and dark values, type
roles, spacing grid, radii, shared components, platform guardrails and the
accessibility baseline. It is the single source of truth for Web/PWA, iOS and
Android (FuelStat/fuelstat#153). Never define a token value here; change it
there first, then in the Compose theme.

This file only covers what is specific to Android: how the shared design is
built with Material 3 and Jetpack Compose. Where Material and the shared
document differ in *behaviour*, follow the platform – the app should feel like
an Android app, not an iOS port. Colours, spacing, radii and type roles always
come from the shared document.

## Navigation

**Structure, names and homes of content are defined in the shared document,
[section 7](https://github.com/FuelStat/fuelstat/blob/main/docs/design.md#7-navigation-und-aufbau)**
(FuelStat/fuelstat#206): the five areas (7.1), vehicle choice and the extra
views of a vehicle (7.2), quick entry (7.3), Profil groups (7.4), the glossary
(7.5, source for `res/values/strings.xml`) and the "home of every content"
table (7.6). This file only says how Material 3 and Compose show them. Do not
add destinations, tabs or labels that are not in section 7; put new content in
the table there first.

| Shared (section 7) | Android |
|---|---|
| Leiste: Start · Einträge · ＋ · Statistik · Profil (7.1) | `NavigationBar` with exactly four destinations **Start · Einträge · Statistik · Profil**, no "＋" inside the bar. The "＋" is a floating action button (`Brand`, content description "Eintrag erfassen") on Start and Einträge; one "＋" per screen, never both |
| Large screens (≥ 600 dp) | `NavigationRail` (the FAB sits in the rail header) and list–detail layouts |
| Vehicle switcher (7.2) | `VehicleSwitcher` at the top of Start, Einträge and Statistik, opens a modal bottom sheet with the active vehicles and "Fahrzeuge verwalten" (goes to Profil → Fahrzeuge). The choice lives in memory for the session (until the app is restarted); after launch the default vehicle (first in `sortOrder`) is selected |
| Start with fleet plan (7.2) | With `features.fleet` and two or more active vehicles Start is a list of compact vehicle cards; a tap selects the vehicle and opens its overview. No comparison |
| Extra views Service · Fahrten · Fahrzeugdaten (7.2) | Detail destinations opened from the "Mehr zum Fahrzeug" rows on Start and from Profil → Fahrzeuge → [vehicle]; at ≥ 600 dp list and detail side by side. No tabs, chips or segments for them |
| Berichte und Export (7.1, 7.6) | One overflow menu in the top app bar of Einträge |
| Profil groups (7.4) | Grouped list: Konto, Fahrzeuge, Daten, Benachrichtigungen, Über, Darstellung |
| Archive (7.2) | Profil → Fahrzeuge → Archiv, read-only |

Android specifics that do not change the structure:

- Number inputs (odometer, quantity, price, amount) use `NumberField`;
  `CodeField` is for the 2FA code only.
- Entry forms as full-screen dialogs or modal bottom sheets; destructive
  actions with confirmation or an undo snackbar.
- Edge-to-edge with transparent system bars; predictive back on every screen.

## Theme (Compose)

All values come from the shared token table ("Token-Zuordnung je Plattform").

- **Colour scheme:** `lightColorScheme`/`darkColorScheme` built from the
  tokens: `primary` = `Brand`, `onPrimary` = `OnBrand`, `background`/`surface`
  = `Background`, `surfaceContainerLow` = `Card`, `secondaryContainer` =
  `CardTint`, `onSecondaryContainer` = `BrandDeep`, `onBackground`/`onSurface`
  = `Text`, `onSurfaceVariant` = `TextSecondary`, `outlineVariant` =
  `Separator`, `error` = `StatusOverdue`. Remaining Material roles are derived
  from the nearest token, not picked freely.
- **Extended colours:** everything without a Material role (`BrandDeep`,
  status, energy, purpose and trip colours) in `FuelstatColors`, provided via
  `CompositionLocal`, light and dark.
- **Dynamic colour (Material You): off**, so the brand stays recognisable.
- **Spacing:** `FuelstatSpacing.xxs` … `xl` (4, 8, 12, 16, 24, 32 dp).
- **Shapes:** `shapes.large` = `radiusCard` (20 dp), `shapes.medium` =
  `radiusTile` (14 dp); chips and badges `CircleShape`.
- **Typography:** Material type scale in `sp` with the system font, mapped
  from the shared type roles (`screenTitle` → `headlineMedium`, `figure` →
  `titleLarge`, `body` → `bodyLarge`, `label` → `bodyMedium`, `footnote` →
  `bodySmall` …). Figures with tabular digits (`fontFeatureSettings = "tnum"`).
  No fixed heights for text containers.

## Android specifics

- **Icons:** Material Symbols (Rounded).
- **Feedback:** ripple on every tappable element; haptics sparingly; snackbar
  with "Rückgängig" after deleting.
- **Lists:** swipe or long press for actions.
- **Components:** the shared building blocks (vehicle hero, stat tile, entry
  row, status badge, filter chips, empty state …) live in one design package
  and are reused everywhere; same names as in the shared document.
- **Charts:** a text alternative (summary sentence plus values as a list) for
  TalkBack.

## Accessibility

The shared baseline applies; on Android in particular:

- Every icon-only control has a content description.
- Touch targets at least 48 × 48 dp.
- Text in `sp`, layouts tested at the largest system font size.
- Contrast checked in light and dark theme with the token values.
