# Quickstart Validation Guide

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md) · **Contract**: [contracts/appearance-contract.md](contracts/appearance-contract.md)

## Prerequisites

- Android device/emulator API 24+
- Debug build installed: `./gradlew assembleDebug` then install
  `app/build/outputs/apk/debug/LumaCalendar-debug.apk`
- App opened at least once (so defaults exist)
- A couple of events on today's date, so accent/selection states are visible on Calendar

## Test commands

```bash
./gradlew test                 # unit suite incl. new appearance tests
./gradlew clean assembleDebug  # build gate
```

## Validation Scenarios

### V1: Theme mode applies (US1, FR-001/FR-002/FR-003/FR-004, SC-001)

1. Settings → Appearance & Theme → note the current backdrop (layered gradient, pronounced
   ambient glow = Liquid Glass).
2. Tap **OLED Deep**. Verify immediately, without restart or navigating away: backdrop goes
   near-black/flat, ambient glow visibly reduced, cards/sheets read darker.
3. Verify exactly one chip shows the selected indicator and it is **OLED Deep**.
4. Tap **Liquid Glass**; verify the gradient + stronger glow return and the indicator moves.
5. Alternate rapidly 5×; verify the last tap wins, indicator matches, no flicker beyond normal
   redraw, no crash.

### V2: Accent color applies app-wide (US2, FR-006/FR-007/FR-008/FR-009, SC-002/SC-005)

1. Settings → Accent Color → tap circle 1 (Royal Violet).
2. On the **same screen**, verify immediately: section icon tints, first-day/week-number icon
   tints, selected chip/border colors, and the selected circle ring all follow the new accent.
3. Verify exactly one circle shows the selected indicator and it is the one tapped.
4. Repeat for all 5 circles — **every circle must respond** (zero inert controls).
5. Tap each circle in turn and confirm the rendered accent differs between at least three of
   them (proves the selection reaches the app, not just the ring).

### V3: Immediate propagation without navigation (US3, FR-012, SC-004)

1. Settings → select a distinctive accent (e.g. Radiant Coral).
2. Switch **directly** to Calendar (do not return through Settings). Verify month/day
   selection, "+ Add Event", header icons, and FAB use the new accent.
3. Switch **directly** to Search. Verify search field/icon and category chip selection follow.
4. Open Add Event sheet and the time picker; verify their accents follow.
5. Open the Clear-all confirmation dialog; verify its accent follows while it is open.

### V4: Persistence across force-stop (US1/US2, FR-005/FR-010, SC-003)

```bash
adb shell am force-stop com.aistudio.lumacalendar.vtxk
adb shell am start -n com.aistudio.lumacalendar.vtxk/.MainActivity
adb shell run-as com.aistudio.lumacalendar.vtxk \
  cat shared_prefs/notification_preferences.xml
```

1. Set theme = OLED Deep, accent = index 2 (Neon Cyan).
2. Force-stop and relaunch → verify OLED Deep + Neon Cyan are applied with no extra step.
3. Confirm the prefs file still shows `theme_name` and `accent_color_index` and that
   `active_calendar_type`, snooze, category-visibility, `first_day_monday`,
   `show_week_numbers`, and notification keys are **unchanged** (FR-016).

### V5: First-run / unknown values (FR-013/FR-014)

1. Clear app data (or clear only `notification_preferences`), launch → verify default theme
   and first accent apply, no crash, no blank state.
2. Write a bogus `theme_name` (e.g. `NotARealTheme`) and `accent_color_index` = `99`, relaunch
   → verify default theme and a valid accent apply (no crash, no unrendered state).

### V6: Selected-state clarity (FR-003/FR-008, SC-005)

1. Inspect Appearance & Theme with each theme and each accent selected → exactly one of each
   shows the selected indicator, and it always matches what is rendered.

### V7: Non-regression — other settings (US4, FR-016, SC-006)

1. Record: calendar type, snooze duration, three category-visibility toggles, first-day,
   week-numbers, notification toggles.
2. Change theme and accent several times.
3. Re-inspect all recorded values → identical.

### V8: Non-regression — RTL/localization (US4, FR-017, SC-007)

1. In a Jalali session (RTL, Persian), change theme and accent → layout stays right-to-left,
   all strings stay Persian, digits stay Persian.
2. Switch to Gregorian (LTR, English), repeat → layout stays left-to-right, strings English.
3. Compare labels/strings before and after → identical, no new or missing text.

### V9: Non-regression — core behavior (US4, FR-018, SC-009)

1. Calendar month/week/day navigation, Today button, selecting a non-today date and returning
   → identical behavior; Today-vs-selected distinction intact.
2. Create, edit, and delete an event → identical behavior.
3. Verify a scheduled reminder and the daily notification are unaffected.

```bash
./gradlew test
./gradlew clean assembleDebug
```
Expected: new appearance tests (theme switching, accent switching, restart persistence,
immediate propagation structure, existing-settings isolation) pass; all pre-existing tests
still pass; build succeeds (SC-008).

### V10: Baseline non-regression for default users (FR-013, SC-006)

1. On a fresh install with **no** appearance change, compare Calendar / Search / Settings to
   the pre-change build → Liquid Glass + first accent, visually identical (index 0 must
   reproduce the old hardcoded colors exactly).
