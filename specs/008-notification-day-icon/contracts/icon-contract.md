# Small Icon Contract

**Date**: 2026-09-21
**Spec**: [spec.md](../spec.md)

## Renderer Contract

### `LumaNotificationIconGenerator.generateSmallIcon(context, dayText, sizePx): Bitmap`

**Input**:
- `context`: for Vazirmatn typeface loading
- `dayText`: localized day-number string (e.g., "۲۶" or "9")
- `sizePx`: source bitmap size (small-icon scale)

**Output**: An ARGB_8888 Bitmap that is an **alpha mask**:
- Opaque white (`#FFFFFFFF`) where the calendar silhouette and day
  number are drawn
- Fully transparent elsewhere
- NO gradient, NO color fills (system will tint it)

**Invariants**:
1. Same `dayText` → deterministically identical bitmap
2. Day number is centered and fully within bounds for 1–31 (single and
   double digit)
3. No bell, alarm, clock, month text, or secondary date
4. Vazirmatn font used when available; graceful fallback otherwise

## Builder Contract

### Daily notification (`LumaNotificationManager.kt`)

**Before**:
```
.setSmallIcon(R.drawable.ic_notification_luma)
```

**After**:
```
.setSmallIcon(IconCompat.createWithBitmap(smallIconBitmap))
```
where `smallIconBitmap = LumaNotificationIconGenerator.generateSmallIcon(context, dayText, sizePx)`
and `dayText` is the localized Jalali day of the device-local date.

**Invariants**:
1. Notification ID stays 1001, channel stays luma_calendar_daily
2. In-place update — no new notification posted
3. Body RemoteViews unchanged (FR-012)

### Event reminders (`EventNotifications.kt`) — MUST NOT CHANGE

```
.setSmallIcon(R.drawable.ic_notification_luma)   // stays exactly as-is (FR-015)
```

## Prohibited

- Deleting/replacing `ic_notification_luma.xml` (still used by event reminders)
- Full-color bitmap passed as small icon (renders as white blob)
- UTC / selected-date / event-date / ±1-day day derivation
- Hardcoded per-digit strings
- New Activity, ActivityAlias, or MainActivity relaunch
