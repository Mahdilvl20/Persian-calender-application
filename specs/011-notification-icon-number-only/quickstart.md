# Quickstart Validation Guide

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md) · **Contract**: [contracts/day-icon-contract.md](contracts/day-icon-contract.md)

## Prerequisites

- Android device or emulator, API 24+
- Debug build installed: `./gradlew assembleDebug`, then install
  `app/build/outputs/apk/debug/LumaCalendar-debug.apk`
- Notifications permitted for the app
- Active calendar type set to Jalali so the icon shows Persian digits
- Ideally a device whose Jalali day is **single-digit** for V2 and **double-digit** for V3 —
  or change the device date to force each case

## Test commands

```bash
./gradlew test                 # unit suite incl. the new icon geometry tests
./gradlew clean assembleDebug  # build gate
```

## Validation Scenarios

### V1: Frame is gone (FR-001, SC-001, US1)

1. Force the daily notification to refresh (toggle the daily notification off/on in Settings,
   or wait for the next midnight update).
2. Open the notification shade and enlarge the daily notification if the launcher allows.
3. Verify the icon is **only** a day number: no rounded rectangle outline, no horizontal
   header line, no two binder tabs, no square or page shape, no background tile.
4. Repeat for a single-digit and a double-digit day — neither shows any container.

### V2: Single-digit day is large and centered (FR-003/FR-004/FR-005, TR-001, TR-003)

1. Set the device date so the Jalali day is single-digit (e.g. ۹).
2. Refresh the daily notification.
3. Verify the digit fills most of the icon, sits dead-center horizontally **and** vertically
   (not low, not offset), and is clearly larger than before this change.
4. Confirm there is no gap or drift caused by the missing second digit.

### V3: Double-digit day is scaled, centered, unclipped (FR-006, TR-002, TR-004)

1. Set the device date so the Jalali day is double-digit (e.g. ۳۱).
2. Refresh the daily notification.
3. Verify **both** digits are fully visible — no stroke cut at the left, right, top, or
   bottom, and nothing eaten by the round mask the system applies.
4. Verify the pair is still centered on both axes and still dominates the icon.

### V4: Legible at actual notification size (FR-015, SC-006, TR-005)

1. Look at the notification shade at normal viewing distance, without zooming.
2. Verify the day number reads on first glance as a solid, unbroken glyph — no gaps, no
   notches, no partial digits.
3. Compare against the pre-change build side by side; the number must be the larger of the two.

### V5: Correct real local Jalali day (FR-007, SC-007)

1. Note today's Jalali date, then read the number in the daily notification.
2. Verify they match, and that the glyph style follows the active calendar type (Persian
   digits while Jalali is active).
3. Confirm the number is never blank, never a placeholder, and never a substitute value.

### V6: Updates on date change (FR-008, SC-007)

1. Advance the device date across midnight (or use a time-shift tool).
2. Verify the daily notification's number advances to the new Jalali day without reinstalling
   or force-stopping the app.

### V7: Survives reboot / timezone change / restart (FR-009, SC-007)

1. Reboot the device → verify the daily notification shows the correct day.
2. Change the device timezone → verify it still shows the correct local Jalali day.
3. Force-stop and relaunch the app → verify the daily notification still shows the correct day.

```bash
adb shell am force-stop com.aistudio.lumacalendar.vtxk
adb shell am start -n com.aistudio.lumacalendar.vtxk/.MainActivity
```

### V8: Event Reminder icon unchanged (FR-011, SC-008)

1. Create an event with a near-term reminder.
2. When the reminder fires, verify its icon is the same as before this change — **not** a bare
   day number.

### V9: Launcher icon unchanged (FR-012, SC-008)

1. Inspect the app icon on the launcher, including any day-based variant.
2. Verify it is identical to the pre-change build.

### V10: Notification metadata and non-regression (FR-013, SC-008, SC-009)

1. Compare the daily notification's title, text, channel, and actions against the pre-change
   build → identical.
2. Confirm the notification still appears on schedule and still refreshes at midnight.

```bash
./gradlew test
./gradlew clean assembleDebug
```
Expected: new geometry tests (single-digit, double-digit, centering, no clipping, fill ratio,
opacity at real size, absence of frame) pass; every pre-existing test still passes; build
succeeds (SC-009).
