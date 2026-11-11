# Fuelstat Android – design

How the app should look and feel. The iOS app's
[`docs/design.md`](https://github.com/FuelStat/fuelstat-ios/blob/main/docs/design.md)
defines the product direction; this file translates it to Android. Where both
differ, follow the platform: the app should feel like an Android app, not an
iOS port.

## Direction

**Calm, card-based, photo-forward, one strong colour** – same principles as
iOS:

1. **The vehicle is the hero.** Its photo and name anchor every vehicle screen.
2. **Numbers first.** Consumption, cost per km and the next due date are shown
   big and early, without tapping.
3. **One tap to log.** Logging a fill-up or charging session is always one tap
   away.
4. **Native, not a web page.** Material 3 components, Material Symbols, system
   font, edge-to-edge, predictive back.

## Navigation

- Navigation bar with the same four destinations as iOS: **Start · Einträge ·
  Statistik · Profil**, plus a floating action button "+" for quick entry on
  Start and Einträge.
- Vehicle switcher at the top of Start/Einträge/Statistik; the choice is
  remembered.
- Entry forms as full-screen dialogs or modal bottom sheets; destructive
  actions with confirmation or undo snackbar.
- Large screens and foldables: navigation rail and list–detail layouts.

## Colour and theme

- Material 3 colour scheme derived from the Fuelstat brand colour (dark navy
  anchor, one strong accent), light and dark.
- Dynamic colour (Android 12+) optional and off by default, so the brand stays
  recognisable; to be decided when development starts.
- Purpose colours (fuel, charging, expenses, private/business/commute trips)
  match the web app and iOS.

## Typography, spacing, components

- Material type scale in `sp`, respects the system font size; no fixed heights
  for text containers.
- Spacing in multiples of 4 dp; cards with large corner radius, consistent
  with iOS.
- Shared components (vehicle hero, key figure tile, entry row, due-date chip,
  empty state) live in one design package and are reused everywhere.

## Accessibility

- Every icon-only control has a content description.
- Touch targets at least 48 × 48 dp.
- Contrast checked in light and dark theme.
- Charts have a text alternative (summary or table).
