# Quickstart Validation Guide

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device or emulator running API 24+
- App installed
- Widget placed on home screen (2x2 grid minimum)
- Device timezone set to Asia/Tehran for Jalali testing

## Validation Scenarios

### V1: Persian Month at Top (SC-001, User Story 1)

1. Place widget on home screen
2. Verify the top section shows the current Jalali month name
   (e.g., "شهریور" for September 2026)
3. Verify it is NOT a Gregorian month name (not "September")
4. Verify the month text is Medium weight (not Bold)
5. Verify it is visually lighter than the main day number

**Expected**: Persian month name displayed, correctly sized, centered.

### V2: Main Day Number (SC-002, User Story 1)

1. Look at the widget's center
2. Verify the large day number is the Jalali day (e.g., "۲۵")
3. Verify it is the most prominent element (40sp, Bold)
4. Verify it uses Persian digits (not "25" but "۲۵")
5. Verify it is vertically separated from the month above

**Expected**: Jalali day number is dominant, centered, correctly formatted.

### V3: Secondary Calendar Values (SC-003, SC-004, User Story 1)

1. Look at the widget's bottom area
2. Verify two secondary values are displayed (Gregorian and Hijri day)
3. Verify they are visually independent with balanced spacing
4. Verify they do NOT appear as a combined string
5. Verify they use smaller text and Regular weight

**Expected**: Two secondary values, well-spaced, visually subordinate.

### V4: Date Accuracy (SC-001, User Story 2)

1. Compare widget's Jalali date with device date converted manually
2. Verify Jalali month + day = correct conversion of today
3. Verify secondary values = Gregorian and Hijri of the same day
4. Wait for midnight → verify widget updates automatically

**Expected**: All values accurate and auto-updating.

### V5: Spacing & Hierarchy (SC-003, SC-004, User Story 1)

1. Visually inspect the widget
2. Verify three clear visual levels: month, day, secondary
3. Verify intentional whitespace between each level
4. Verify the widget does NOT feel crowded or compressed
5. Compare with the old layout — spacing should be noticeably improved

**Expected**: Spacious, clean, premium feel with clear hierarchy.

### V6: Typography (User Story 3)

1. Verify month text uses Medium weight
2. Verify day number uses Bold weight
3. Verify secondary values use Regular weight
4. Verify Vazirmatn font is applied throughout

**Expected**: Correct weight hierarchy, consistent font.

### V7: RTL & Localization

1. With device in Persian locale, verify month name renders in Persian
2. Verify the widget layout is structurally correct (centered elements)
3. Switch to English locale, verify widget still functions

**Expected**: RTL-compatible, locale-aware rendering.

### V8: Build Verification (SC-008)

```bash
./gradlew clean assembleDebug
./gradlew test
```

**Expected**: Build succeeds, all tests pass.

### V9: Non-Regression

1. Verify Daily Notification (ID 1001) is unaffected
2. Verify Event Reminders are unaffected
3. Verify DynamicIconManager shortcut icon still updates
4. Verify widget click still opens MainActivity

**Expected**: No regressions in notification or icon systems.
