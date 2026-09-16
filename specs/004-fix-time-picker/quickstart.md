# Quickstart Validation Guide

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device or emulator running API 24+
- App installed
- Both Jalali and Gregorian calendar types available in Settings

## Validation Scenarios

### V1: RTL Layout (SC-001, User Story 1)

1. Set active calendar to Jalali in Settings
2. Open Add Event → tap time field → Time Picker opens
3. Verify layout is RTL: hour on the right, minute on the left
4. Verify colon separator is between them
5. Verify numbers are NOT reversed (Persian digits render correctly)
6. Verify the header (title, toggle) flows RTL

**Expected**: Correct RTL layout with hour right, minute left.

### V2: LTR Layout (SC-002, User Story 1)

1. Set active calendar to Gregorian in Settings
2. Open Add Event → tap time field → Time Picker opens
3. Verify layout is LTR: hour on the left, minute on the right
4. Verify colon separator is between them
5. Verify the header flows LTR

**Expected**: Correct LTR layout with hour left, minute right.

### V3: Dynamic Direction Update (SC-003, User Story 1)

1. Open Time Picker in Jalali mode (RTL)
2. Without closing the picker, switch calendar type to Gregorian
3. Verify the picker layout updates to LTR
4. Switch back to Jalali
5. Verify the picker returns to RTL

**Expected**: Layout direction updates dynamically without closing.

### V4: Touch Targets (SC-004, User Story 2)

1. Open Time Picker
2. Tap the increment/decrement buttons — verify they are easy to tap
3. Verify no accidental taps on adjacent buttons
4. Tap quick pick chips (00, 15, 30, 45) — verify easy to tap
5. Toggle 12H/24H — verify easy to tap

**Expected**: All interactive elements meet 48dp minimum touch target.

### V5: Time Field Interaction (User Story 2)

1. Open Time Picker
2. Tap increment on hour — verify value increases
3. Tap decrement on hour — verify value decreases
4. Verify hour wraps at 23→0 and 0→23
5. Repeat for minute — verify wraps at 59→0 and 0→59 (5-min increments)
6. Verify active field has visual highlight

**Expected**: Steppers work correctly, active field highlighted.

### V6: AM/PM (User Story 3)

1. Open Time Picker in 12H mode
2. Verify AM/PM pills are visible
3. Tap AM — verify hour adjusts (≥12 → subtract 12)
4. Tap PM — verify hour adjusts (<12 → add 12)
5. In Persian locale, verify labels show ق.ظ / ب.ظ

**Expected**: AM/PM works correctly, localized labels.

### V7: Visual Design (User Story 4)

1. Open Time Picker
2. Verify Liquid Glass design (dark glass surface, translucent backgrounds)
3. Verify Vazirmatn font used throughout
4. Verify color tokens from Color.kt (no arbitrary colors)
5. Compare with other Luma Calendar components — verify visual consistency

**Expected**: Premium, consistent visual design.

### V8: Build Verification (SC-008)

```bash
./gradlew clean assembleDebug
./gradlew test
```

**Expected**: Build succeeds, all tests pass.

### V9: Non-Regression

1. Create an event with a start time — verify Time Picker works
2. Create an event with an end time — verify Time Picker works
3. Edit an existing event's time — verify Time Picker prefills correctly
4. Verify Daily Notification (ID 1001) is unaffected
5. Verify Event Reminders are unaffected

**Expected**: No regressions in event creation or notifications.
