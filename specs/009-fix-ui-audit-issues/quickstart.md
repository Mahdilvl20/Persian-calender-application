# Quickstart Validation Guide

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

## Prerequisites

- Android device/emulator API 24+
- App installed; a few events created across Personal, Work, and Holidays

## Validation Scenarios

### V1: Category filtering — calendar views (SC-001, US1)

1. Create events tagged Personal, Work, Holidays
2. Settings → turn OFF Work → return to Calendar
3. Verify Work events are gone from Month, Week, and Day views; Personal +
   Holidays remain
4. Turn Work back ON → verify events reappear immediately (no restart)
5. Turn all three OFF → verify no user events shown; empty-day states render

### V2: Category filtering does not affect Search (FR-002a, US1)

1. Turn OFF Work
2. Open Search, search for a known Work event title
3. Verify the Work event STILL appears in Search results (search is
   full-catalog); Search's own category chips still work independently

### V3: Category visibility persists (SC-001, SP-002)

1. Turn OFF Holidays
2. Fully restart the app
3. Verify Holidays remains OFF and its events stay hidden in calendar views

### V4: Appearance settings persist (SC-002, US2)

1. Change accent color, set first-day = Monday, enable week numbers, pick a theme
2. Fully restart the app
3. Verify all four retain their chosen values
4. (First-run) Clear app data → launch → verify defaults applied, no crash

### V5: Destructive-action confirmation (SC-003, US3)

1. Settings → tap "Clear all data" → dialog appears → Cancel → verify no data lost
2. Tap "Clear all data" → Confirm → verify data cleared as before
3. Repeat for "Reset sample data" (Cancel = no change; Confirm = restores)
4. Open dialog → dismiss (back / tap outside) → verify no action taken

### V6: Blank-title validation (SC-004, US4)

1. Add Event → leave title empty → attempt Save → verify blocked + localized
   feedback; event NOT auto-named "New Event"
2. Enter only spaces → attempt Save → verify treated as blank, rejected
3. Enter a valid title → Save → verify saves normally (trimmed)
4. Edit an existing event → clear title → attempt Save → verify same validation

### V7: Snooze description matches options (SC-005, US6)

1. Settings → snooze section → verify the description names exactly the
   selectable chip values

### V8: Persian loading state (US5)

1. Observe calendar while Persian data loads → verify either a design-consistent
   loading indicator or no dead/unused state; data fills in normally

### V9: Build & tests (SC-007)

```bash
./gradlew test
./gradlew clean assembleDebug
```

Expected: build succeeds; new tests for category filtering, appearance
persistence, and title validation pass; existing tests still pass.

### V10: Non-regression (SC-006)

- Daily notification (ID 1001) and event reminders unaffected
- Calendar switching (Jalali/Gregorian/Hijri), RTL/LTR, Today vs selected all
  behave as before
