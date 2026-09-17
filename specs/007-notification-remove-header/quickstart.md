# Quickstart Validation Guide

**Date**: 2026-09-17

## Validation Scenarios

### V1: No Duplicate Header

1. Enable daily notification
2. Pull down shade
3. Verify "Luma Calendar" appears only in system header
4. Verify no "Luma Calendar" text inside notification body
5. Verify no app icon inside RemoteViews content

### V2: Date Dominance

1. With notification visible, verify primary date is first element
2. Verify date is readable and not truncated
3. Verify secondary dates present below

### V3: Build Verification

```bash
./gradlew clean assembleDebug
./gradlew test
```

### V4: Non-Regression

1. Verify notification ID 1001 persists
2. Verify ongoing/non-dismissible
3. Verify Event Reminders unaffected
