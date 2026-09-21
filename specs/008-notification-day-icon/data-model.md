# Data Model: Notification Small Icon — Dynamic Persian Day Number

**Date**: 2026-09-21
**Spec**: [spec.md](spec.md)

## Entities

### NotificationDayIcon (Virtual — runtime bitmap)

A monochrome alpha-mask bitmap rendered per refresh for the daily
notification small icon.

| Attribute | Value | Constraint | Rule |
|-----------|-------|------------|------|
| dayValue | Jalali day 1–31 | From CalendarConverter.gregorianToJalali(device date) | FR-002, FR-003 |
| dayText | Localized digit string | Persian digits (JALALI) or Latin (GREGORIAN/English) | FR-007 |
| glyph | Calendar silhouette + centered day number | White (opaque alpha) on transparent | FR-001, FR-006 |
| color model | Alpha-only (monochrome) | No gradient/color; system tints it | FR-006, FR-013 |
| size | Small-icon scale (e.g., 48–96px source) | No oversized bitmaps | FR-013 |
| excluded content | No month, Gregorian/Hijri date, extra text; no bell/alarm/clock | — | FR-004, FR-005 |

**Rendering**: `LumaNotificationIconGenerator.generateSmallIcon(context, dayText, sizePx)`
→ Bitmap → `IconCompat.createWithBitmap(bitmap)` →
`NotificationCompat.Builder.setSmallIcon(iconCompat)`.

**Digit-size adaptation**: single-digit days use larger text; double-digit
days use smaller text (reuse existing `if (dayText.length > 2)` logic
pattern) so 1–31 never clip (FR-008, SC-004).

### DailyNotification (existing, unchanged identity)

| Attribute | Value | Rule |
|-----------|-------|------|
| Notification ID | 1001 (constant) | FR-010 |
| Channel | luma_calendar_daily | FR-010 |
| Ongoing | true (unchanged) | — |
| Body layout | Header → Primary date → Secondary → Message → Actions (unchanged) | FR-012 |
| Small icon | NEW: dynamic day-number bitmap (was static bell) | FR-001 |

### EventReminder (existing, OUT OF SCOPE — must not change)

| Attribute | Value | Rule |
|-----------|-------|------|
| Small icon | `R.drawable.ic_notification_luma` (bell, UNCHANGED) | FR-015 |

## State Transitions (Day Icon)

```
[Daily notification refresh triggered]
  → read DateUtils.getRealDeviceLocalDate()
  → CalendarConverter.gregorianToJalali() → jalaliDay
  → localize digits (toPersianDigits if JALALI)
  → generateSmallIcon(dayText) → bitmap
  → setSmallIcon(IconCompat.createWithBitmap(bitmap))
  → notify(1001, ...)   [same ID, in-place update]
```

Triggers (existing lifecycle, FR-009): app start, foreground, midnight
alarm, date/time/timezone change, reboot, package update.

## Validation Rules

| Rule | Validation |
|------|------------|
| VR-001 | dayValue derived only from device-local date (never UTC/selected/event) |
| VR-002 | dayText uses localized digits per active calendar type |
| VR-003 | Small-icon bitmap is alpha-mask monochrome (no color dependence) |
| VR-004 | Days 1–31 render without clipping at small-icon size |
| VR-005 | Notification ID remains 1001; no duplicate posted |
| VR-006 | `ic_notification_luma.xml` unchanged; event reminders unaffected |
