# Quickstart Validation Guide

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device or emulator running API 24+
- App installed
- Both Jalali and Gregorian calendar types available

## Validation Scenarios

### V1: Hour → Minute Ordering in Jalali (SC-001)

1. Set active calendar to Jalali
2. Open Add Event → tap time field → Time Picker opens
3. Verify: Hour is on the LEFT, Minute is on the RIGHT
4. Verify: Separator is between them
5. Tap hour plus — verify hour changes (not minute)
6. Tap minute plus — verify minute changes (not hour)

**Expected**: `[Hour] : [Minute]` from left to right in RTL mode.

### V2: Hour → Minute Ordering in Gregorian (SC-002)

1. Set active calendar to Gregorian
2. Open Time Picker
3. Verify: Hour on LEFT, Minute on RIGHT (same as Jalali)

**Expected**: Same ordering in LTR mode.

### V3: Surrounding RTL Preserved (SC-003)

1. Set active calendar to Jalali
2. Open Time Picker
3. Verify header text flows RTL (title on right)
4. Verify Cancel/Confirm buttons are in RTL positions
5. But time fields remain Hour → Minute from left to right

**Expected**: RTL everywhere except the time-control row.

### V4: Plus/Minus Association (SC-003, SC-004)

1. Open Time Picker in Jalali mode
2. Tap hour plus — verify ONLY hour changes
3. Tap hour minus — verify ONLY hour changes
4. Tap minute plus — verify ONLY minute changes
5. Tap minute minus — verify ONLY minute changes

**Expected**: Controls never swap associations.

### V5: Separator Position (SC-005)

1. Open Time Picker in Jalali mode
2. Verify colon is centered between Hour and Minute
3. Verify spacing is balanced on both sides
4. Verify colon renders consistently (not reversed or displaced)

**Expected**: Separator always between Hour and Minute.

### V6: Build Verification (SC-009)

```bash
./gradlew clean assembleDebug
./gradlew test
```

**Expected**: Build succeeds, all tests pass.

### V7: Non-Regression (SC-008)

1. Open Time Picker in Gregorian mode — verify same Hour → Minute
2. Create an event with a time — verify time saves correctly
3. Edit an event time — verify time prefills correctly
4. Verify Daily Notification (ID 1001) unaffected
5. Verify Event Reminders unaffected

**Expected**: No regressions in any mode.
