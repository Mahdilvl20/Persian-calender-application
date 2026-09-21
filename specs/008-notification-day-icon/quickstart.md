# Quickstart Validation Guide

**Date**: 2026-09-21
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device/emulator API 24+ with notification permission granted
- Daily notification enabled, active calendar = Jalali
- Device timezone Asia/Tehran

## Validation Scenarios

### V1: Brand Icon Replaces Bell (SC-001, US1)

1. Enable daily notification, open shade + status bar
2. Verify small icon is a compact calendar mark with a day number
3. Verify NO bell icon anywhere in the daily notification
4. Check on both light and dark system UI — icon stays legible

### V2: Current Jalali Day (SC-002, US2)

1. Note today's real Jalali day from the app's primary date
2. Verify the small icon shows the same day number
3. Navigate the calendar to another date, select events elsewhere
4. Verify the icon still shows the real current Jalali day (not selected)

### V3: Single & Double Digit (SC-004, US2/US4)

1. Set device date to a single-digit Jalali day (e.g., ۹)
2. Verify ۹ renders fully, centered, no clipping
3. Set device date to a double-digit day (e.g., ۳۱)
4. Verify both digits fit within the icon bounds

### V4: Localized Digits (SC-005, US4)

1. In Jalali mode, verify day number uses Persian digits (۲۶)
2. Switch to Gregorian/English, verify Latin digits (26)

### V5: Auto Refresh at Day Change (SC-003, US3)

1. With notification active, advance device date across midnight
2. Verify icon shows new Jalali day, no user action
3. Verify only ONE notification exists (ID 1001, no duplicate)
4. Reboot device → verify notification restored with current day icon
5. Change timezone (if it changes local date) → verify icon updates

### V6: Event Reminders Untouched (FR-015)

1. Create an event with a reminder, trigger it
2. Verify the event reminder still uses the original bell small icon
3. Verify the daily notification's day-number icon is unaffected

### V7: Build & Test (SC-006)

```bash
./gradlew clean assembleDebug
./gradlew test
```

Expected: build succeeds, existing tests pass, day-number derivation
unit test passes.
