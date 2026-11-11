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

- Navigation bar with exactly the four shared destinations **Start ·
  Einträge · Statistik · Profil** – no "+" inside the bar – plus a floating
  action button "+" (`Brand`, content description "Eintrag erfassen") for
  quick entry on Start and Einträge. One "+" per screen, never both.
- Number inputs (odometer, quantity, price, amount) use `NumberField`;
  `CodeField` is for the 2FA code only.
- Large screens and foldables (≥ 600 dp): navigation rail and list–detail
  layouts.
- Vehicle switcher at the top of Start/Einträge/Statistik; the choice is
  remembered.
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
