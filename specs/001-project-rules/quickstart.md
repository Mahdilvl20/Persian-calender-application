# Quickstart Validation Guide

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device or emulator running API 24+
- App installed with notification permission granted
- Device date/time set correctly; timezone set to Asia/Tehran for Jalali testing

## Validation Scenarios

### V1: Calendar Date Accuracy (SC-001, User Story 1)

1. Launch app
2. Verify today's date is highlighted on the calendar grid
3. Switch to Jalali calendar type
4. Verify the same real-world date is shown (no off-by-one)
5. Switch to Gregorian
6. Verify the date matches the device date
7. Change device timezone to a different offset
8. Relaunch app; verify today updates correctly

**Expected**: All three calendar systems show the equivalent real-world
date. No crashes. No off-by-one errors.

### V2: Daily Notification Lifecycle (SC-002, SC-011, User Story 3)

1. Open Settings, enable daily notification
2. Pull down notification shade
3. Verify exactly ONE notification with today's calendar info exists
4. Verify tapping the notification does NOT dismiss it
5. Force-stop the app, relaunch
6. Verify notification 1001 is still present (not duplicated)
7. Disable notification in settings
8. Verify notification 1001 is gone from shade
9. Re-enable notification
10. Verify exactly ONE new notification appears within 2 seconds

**Expected**: Single persistent notification, no duplicates across
restarts, clean cancel/recreate on toggle.

### V3: Event Reminder (SC-003, User Story 4)

1. Create an event for 2 minutes from now with 1-minute-before reminder
2. Wait for the trigger time
3. Verify a notification appears with event title and time
4. Verify the notification is on "event_reminders" channel (not daily)
5. Edit the event, change the time to 5 minutes later
6. Verify the old reminder does NOT fire at original time
7. Verify a new reminder fires at the adjusted time
8. Delete the event
9. Verify no stale notification appears

**Expected**: Reminder fires at exact scheduled time. Edit cancels old,
schedules new. Delete prevents stale notifications.

### V4: Calendar Conversion Round-Trip (SC-001, User Story 1)

1. Open Calendar, select today (2026-09-16)
2. Note the Jalali date (should be 25 Shahrivar 1405)
3. Note the Hijri date (should be 24 Rabi al-Awwal 1448)
4. Switch calendar type to Jalali
5. Verify the displayed date matches the Jalali note from step 2
6. Switch to Hijri
7. Verify the displayed date matches the Hijri note from step 3

**Expected**: All conversions are consistent and correct.

### V5: RTL Layout (SC-005, User Story 5)

1. Switch calendar type to Jalali
2. Verify tab bar icons and text flow right-to-left
3. Verify navigation arrows are mirrored
4. Verify no text appears reversed or garbled
5. Switch to Gregorian
6. Verify layout flips to left-to-right

**Expected**: Layout direction matches calendar type. No reversed text.

### V6: Liquid Glass UI Consistency (SC-006, User Story 2)

1. Navigate to Calendar screen
2. Verify dark background with ambient glow
3. Verify glass surfaces have translucent appearance
4. Navigate to Search screen
5. Verify same visual style
6. Navigate to Settings screen
7. Verify same visual style
8. Verify typography uses Vazirmatn with correct weights

**Expected**: Consistent premium glass design across all screens.

### V7: Error Resilience (SC-004, User Story 6)

1. Disconnect network
2. Launch app, verify calendar grid loads from cache
3. Switch calendar types rapidly (5+ times)
4. Verify no crashes
5. Create event with invalid time (if manual input available)
6. Verify safe fallback, no crash
7. Reconnect network

**Expected**: Zero crashes under all conditions.

### V8: No Duplicate Notifications (SC-008)

1. Enable daily notification
2. Force-stop app 3 times, relaunch each time
3. Verify exactly one notification in shade each time
4. Reboot device
5. Verify exactly one notification restored
6. Create 3 events with reminders at the same time
7. Verify all 3 event notifications coexist with the daily notification

**Expected**: No duplicates under any pattern. All 4 notifications
(1 daily + 3 event) can coexist.

## Build Verification (SC-009)

```bash
./gradlew clean assembleDebug
./gradlew test
```

**Expected**: Build succeeds, all tests pass, no regressions.
