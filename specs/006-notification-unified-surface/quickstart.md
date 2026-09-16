# Quickstart Validation Guide

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device or emulator running API 24+
- App installed with notification permission granted
- Device set to Jalali calendar for primary test

## Validation Scenarios

### V1: No System Container (SC-001, SC-002)

1. Enable daily notification
2. Pull down notification shade
3. Visually inspect the notification

**Verify**:
- NO visible Android system container/card around the Luma content
- The Luma content IS the notification surface
- No "card inside card" nesting
- The system-generated header is NOT present (replaced by custom header)
- Custom header shows app icon + "Luma Calendar" + timestamp

**Expected**: One unified Luma Calendar surface.

### V2: Custom Header (SC-008)

1. With notification visible, check the header area
2. Verify app icon is present
3. Verify "Luma Calendar" text appears once
4. Verify timestamp is present
5. Verify "Luma Calendar · Luma Calendar" never appears

**Expected**: Single app identity, no duplication.

### V3: Primary Date Readability (SC-003)

1. In Jalali mode, verify the primary date is fully readable
   (e.g., "چهارشنبه ۲۶ شهریور ۱۴۰۵" — no truncation)
2. Switch to Gregorian, verify primary date is fully readable
   (e.g., "Thursday, September 17, 2026" — no truncation)

**Expected**: Dates are never truncated.

### V4: Secondary Dates (SC-004)

1. Verify secondary calendar dates are visible below primary
2. Verify all three calendars show the same real-world day
3. Verify no off-by-one errors

**Expected**: Three consistent calendar dates.

### V5: Mini Tile Integration (SC-005)

1. Verify the mini calendar tile is present
2. Verify it uses subtle background (#0DFFFFFF)
3. Verify it does NOT look like a separate floating card
4. Verify month name and day number are correct

**Expected**: Tile is visually integrated, not a separate element.

### V6: Actions (SC-006)

1. Expand the notification
2. Verify Today, New Event, Remind Later are present
3. Verify they are compact glass controls
4. Verify they visually belong to the notification surface
5. Tap Today → verify opens today's date
6. Tap New Event → verify opens Add Event
7. Tap Remind Later → verify snooze works

**Expected**: Actions are compact and functional.

### V7: Collapsed State (SC-007)

1. Collapse the notification
2. Verify it shows primary date + secondary date
3. Verify no excessive content
4. Verify no truncation of essential info

**Expected**: Intentionally designed collapsed state.

### V8: RTL (SC-012)

1. In Jalali mode, verify header flows RTL
2. Verify date row flows RTL
3. Verify actions flow RTL
4. Verify no reversed text

**Expected**: Correct RTL layout.

### V9: Build Verification (SC-010)

```bash
./gradlew clean assembleDebug
./gradlew test
```

**Expected**: Build succeeds, all tests pass.

### V10: Non-Regression

1. Verify Daily Notification ID 1001 persists
2. Verify Event Reminders unaffected
3. Verify notification updates at midnight
4. Verify notification updates on reboot
5. Verify notification updates on timezone change

**Expected**: No regressions.
