# Implementation Plan: Fix Appearance & Theme Controls

**Branch**: `010-fix-appearance-theme` | **Date**: 2026-09-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/010-fix-appearance-theme/spec.md`

## Summary

The Appearance & Theme controls already persist correctly (theme name and accent index are
stored in `NotificationPreferences` and restored on startup), but the selection never reaches
what the app renders: `LumaCalendarTheme` supplies one **static** color scheme, `themeName`
feeds only a subtle ambient-glow alpha, and the accent feeds only one background orb while
**~138 accent references** and **~60 surface/border references** across 10 UI files read fixed
tokens straight from `Color.kt`.

Fix: introduce a single appearance source that resolves the existing `themeName` +
`accentColorIndex` into themed tokens (accent primary/secondary, canvas, glass surfaces,
borders, glow intensity), provide it from the existing `LumaCalendarTheme` call site, and
point every accent-bearing and theme-bearing render site at it. No new state, no new storage,
no new ViewModel, no second persistence mechanism.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Jetpack Compose (BOM 2024.09.00), Material 3

**Primary Dependencies**: Compose runtime/ui, existing `AccentPresets` + `Color.kt` token set;
`NotificationPreferences` (SharedPreferences, file `notification_preferences`) reused as-is

**Storage**: Existing SharedPreferences keys `theme_name` (String, default
`"Liquid Glass (Dark)"`) and `accent_color_index` (Int, default `0`) — already added and
verified in feature 009. No new keys, no new mechanism, no migration.

**Testing**: JUnit 4 + Robolectric (`app/src/test/`); **note**: `MaterialTheme.colorScheme` is
currently read **0 times** and no Roborazzi golden tests exist, so verification rests on pure
resolution logic + preference round-trip, not screenshots.

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose, single `LumaViewModel`)

**Performance Goals**: Appearance resolution must be cheap enough to run on every
recomposition of the root; no measurable UI latency change.

**Constraints**: Preserve RTL/LTR and localization (FR-017); preserve all other stored
settings (FR-016); no new database/storage/migration (FR-015); no second theme or accent
state (FR-011); Settings layout and labels unchanged (FR-018); calendar, notification,
reminder, event behavior unchanged.

**Scale/Scope**: ~13 files touched — `Color.kt`/`Theme.kt` (+ one new appearance file),
`MainActivity.kt`, `GlassComponents.kt` (13 shared primitives), and the 8 screens/sheets that
inline accent/surface tokens: `SettingsScreen`, `CalendarScreen`, `SearchScreen`,
`AddEditEventSheet`, `EventDetailSheet`, `ConfirmActionDialog`, `LiquidGlassTimePickerDialog`,
`ManualDateInputDialog`, plus `Type.kt`. ~138 accent + ~60 surface/border call sites are
mechanical replacements. Tests: 1 new file.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | No date/time source changes; Today-vs-selected untouched (FR-018, US4) |
| II. Calendar Conversion Correctness | PASS | No conversion changes |
| III. Liquid Glass Design Language | PASS | Accent values come from the existing `AccentPresets`; themed values reuse existing `GlassSurface*`/`GlassBorder*`/`Canvas*` tokens rather than inventing new translucency |
| IV. RTL-Aware Localization | PASS | No new strings, no layout-direction changes (FR-017) |
| V. Offline-First Reliability | PASS | Purely local rendering; no network or storage added |
| VI. Single-ViewModel Simplicity | PASS | Reuses existing `themeName` / `accentColorIndex` StateFlows; no new ViewModel or state holder |
| VII. Testable Date Logic | PASS | No date logic touched; appearance resolution is a pure function covered by new unit tests |

No violations.

### Post-Design Re-Check (after Phase 1)

Re-ran every gate against the finished design artifacts
([research.md](research.md), [data-model.md](data-model.md),
[contracts/appearance-contract.md](contracts/appearance-contract.md),
[quickstart.md](quickstart.md)):

| Principle | Status | Post-design note |
|-----------|--------|------------------|
| I. Device-Time Authority | PASS | Design introduces no date/time reads; quickstart V9 re-verifies Today-vs-selected |
| II. Calendar Conversion Correctness | PASS | No conversion touched |
| III. Liquid Glass Design Language | PASS | Accent values come only from `AccentPresets`; OLED Deep adjusts the **existing** `Canvas*`/`GlassSurface*`/`GlassBorder*` token names rather than introducing new token names or a parallel palette |
| IV. RTL-Aware Localization | PASS | Contract §6 forbids new strings/direction changes; quickstart V8 verifies |
| V. Offline-First Reliability | PASS | Resolution is pure and local; no network, no new storage |
| VI. Single-ViewModel Simplicity | PASS | Appearance **state** stays in `LumaViewModel` (`themeName`, `accentColorIndex`); the CompositionLocal only distributes a derived read — it is not a second state holder and holds no authoritative value |
| VII. Testable Date Logic | PASS | No date logic; `resolveAppearance` is a pure function with dedicated unit tests (research Decision 6) |

Still no violations → Complexity Tracking remains empty.

## Project Structure

### Documentation (this feature)

```text
specs/010-fix-appearance-theme/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── appearance-contract.md
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to modify)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/
├── MainActivity.kt                    # feed themeName + accentColorIndex into LumaCalendarTheme
├── ui/theme/
│   ├── Appearance.kt                  # NEW: themed token sets + pure resolveAppearance()
│   │                                  #   + CompositionLocal provider
│   ├── Theme.kt                       # LumaCalendarTheme takes appearance, provides the local
│   ├── Color.kt                       # unchanged token definitions (source palette)
│   └── Type.kt                        # accent-bearing type styles point at themed accent
├── ui/components/
│   ├── GlassComponents.kt             # 13 primitives: AmbientBackground, GlassCard,
│   │                                  #   GlassButton, GlassIconButton, FAB, CalendarCell,
│   │                                  #   EventCard, HolidayCard, GlassInput, CategoryChip,
│   │                                  #   GlassTabBar, GlassToggle, SectionHeader
│   ├── AddEditEventSheet.kt           # accent + surface sites
│   ├── EventDetailSheet.kt            # accent + surface sites (category colors kept)
│   ├── ConfirmActionDialog.kt         # accent + surface sites (destructive color kept)
│   ├── LiquidGlassTimePickerDialog.kt # accent + surface sites
│   └── ManualDateInputDialog.kt       # accent + surface sites
└── ui/screens/
    ├── SettingsScreen.kt              # accent + surface sites; strengthen accent selected ring
    ├── CalendarScreen.kt              # accent + surface/border sites
    └── SearchScreen.kt                # accent + surface sites

app/src/test/java/com/aistudio/lumacalendar/vtxk/
└── AppearanceTest.kt                  # NEW: resolution, round-trip, defaults, isolation
```

**Structure Decision**: Single-module app. Appearance is resolved once, by a pure function,
from the two values the app already stores — `resolveAppearance(themeName, accentIndex)` in
`ui/theme/Appearance.kt` — and handed to the whole tree by a CompositionLocal provided inside
the existing `LumaCalendarTheme`, which MainActivity already calls. Consumers read themed
tokens instead of the fixed `Color.kt` constants; `Color.kt` stays the definition palette and
is not mutated. `AccentPresets` remains the only source of accent values.

Rejected alternatives are recorded in [research.md](research.md).

## Complexity Tracking

No constitution violations requiring justification.
