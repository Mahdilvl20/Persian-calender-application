# Quickstart Validation Guide

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device or emulator running API 24+
- App installed with notification permission granted
- Device set to Jalali calendar (primary test scenario)
- Device timezone set to Asia/Tehran

## Validation Scenarios

### V1: Unified Visual Surface (SC-001, User Story 1)

1. Enable daily notification in Settings
2. Pull down notification shade
3. Visually inspect the notification

**Verify**:
- ONE dark surface, no visible "card inside card" nesting
- No inner rounded rectangle with its own border/stroke
- Content sits directly on the system notification surface
- Liquid Glass aesthetic preserved (dark, translucent feel)
- Mini tile is visually integrated (not a separate floating card)

### V2: Date Accuracy & Calendar Switching (SC-002, SC-003, User Story 2)

1. With Jalali active, note the primary date in notification
2. Verify it matches the device date in Jalali
3. Check secondary dates show Gregorian and Hijri
4. Verify all three represent the same real-world day
5. Switch to Gregorian in Settings
6. Pull down notification shade
7. Verify primary date is now Gregorian, secondaries are Jalali + Hijri
8. Switch to Hijri
9. Repeat verification

**Expected**: Primary date matches active calendar. All three systems
show the same real-world day. No off-by-one errors.

### V3: Mini Calendar Tile Integration (User Story 2)

1. With notification visible, look at the right-side mini tile
2. Verify it shows month name and day number
3. Verify the day number matches the device date
4. Verify the tile styling is subtle and integrated (not a heavy card)
5. Switch calendar types and verify tile updates accordingly

**Expected**: Tile is visually part of the main surface, shows correct
month/day for active calendar.

### V4: Action Buttons (SC-005, SC-006, User Story 3)

1. Expand the notification
2. Verify three actions: Today, New Event, Remind Later
3. Tap Today → verify app opens to today's actual date (not browsed date)
4. Tap New Event → verify Add Event screen opens
5. Tap Remind Later → verify Toast shows with correct snooze duration
6. Wait for snooze period → verify notification 1001 reappears
7. Verify no duplicate notifications created

**Expected**: All three actions work correctly. Snooze restores same
notification ID. No duplicates.

### V5: Snooze Duration Setting (SC-006, FR-011)

1. Open Settings screen
2. Find "Snooze Duration" / "مدت یادآوری" row
3. Verify default is 60 minutes
4. Change to 30 minutes
5. Go back, pull down notification
6. Tap Remind Later
7. Verify Toast says "Snoozed for 30 minutes" (or Persian equivalent)
8. Verify notification reappears after ~30 minutes (not 60)

**Expected**: Setting persists and affects snooze behavior immediately.

### V6: RTL Layout (SC-009, User Story 5)

1. With Jalali active, verify notification layout is RTL
2. Verify text flows correctly (no reversed strings)
3. Verify date and tile are properly mirrored
4. Switch to Gregorian
5. Verify LTR layout

**Expected**: RTL for Jalali/Hijri, LTR for Gregorian. No garbled text.

### V7: Expanded & Collapsed States (SC-008, User Story 4)

1. With notification collapsed, verify: app icon, primary date, mini tile
2. Expand notification, verify: primary date, secondary dates, message, actions
3. Verify both states use the same visual language
4. Verify no content overflow or visual artifacts

**Expected**: Both states are intentionally designed and visually consistent.

### V8: Build Verification (SC-010)

```bash
./gradlew clean assembleDebug
./gradlew test
```

**Expected**: Build succeeds, all tests pass, no regressions.

### V9: Lifecycle Stability

1. Enable notification, verify it appears
2. Force-stop app, relaunch → verify notification 1001 persists (no duplicate)
3. Change device timezone → verify notification updates
4. Change calendar type in Settings → verify notification updates
5. Disable notification → verify notification 1001 is cancelled
6. Re-enable → verify exactly one notification appears

**Expected**: Single notification instance across all lifecycle events.
