# Research: Notification Small Icon — Dynamic Persian Day Number

**Date**: 2026-09-21
**Spec**: [spec.md](spec.md)

## Current State

- **Daily notification small icon**: `ic_notification_luma.xml` — a static
  white bell vector (24dp, `fillColor="#FFFFFFFF"`). Set via
  `.setSmallIcon(R.drawable.ic_notification_luma)` in
  `LumaNotificationManager.kt`.
- **Existing icon generator**: `LumaNotificationIconGenerator.generateIcon()`
  renders a full-color glassmorphism bitmap (blue→violet gradient, white
  calendar outline, day number) at 120px. This was used for the in-body
  RemoteViews app icon (removed in feature 007). It is NOT suitable as-is
  for a status-bar small icon (see Decision 1).

## Decision 1: Small Icon Must Be Monochrome Alpha-Based

**Decision**: Generate a dedicated small-icon bitmap that is white glyph
on transparent background (alpha mask only). Drop the gradient/glass
background used by the in-body icon. Pass via
`NotificationCompat.Builder.setSmallIcon(IconCompat.createWithBitmap(bitmap))`.

**Rationale**: Android renders notification small icons as **alpha masks** —
the system discards RGB color and tints the icon (white on most system UIs,
theme-colored on others). A full-color gradient bitmap would appear as a
solid white blob. The day number and calendar silhouette must be expressed
purely through the alpha channel: opaque white where the glyph/number is,
transparent everywhere else.

**Alternatives considered**:
- Reuse `generateIcon()` full-color bitmap directly → REJECTED: system
  tinting flattens it to a white blob; day number becomes invisible.
- Static per-day vector drawables (31 XML files) → REJECTED: cannot embed
  Persian digits reliably, bloats resources, duplicates the existing
  dynamic-render approach, and violates "no duplicate icon implementation"
  (FR-014).
- Adaptive icon / launcher alias reuse → REJECTED by FR-011 (no
  ActivityAlias, no launcher icon reuse).

## Decision 2: Add a Small-Icon Renderer to the Existing Generator

**Decision**: Add a new function to `LumaNotificationIconGenerator`
(e.g., `generateSmallIcon(context, dayText, sizePx)`) that renders a
monochrome calendar silhouette + centered day number in opaque white on
transparent. Reuse the existing Vazirmatn typeface loading and
single/double-digit text-size logic. Keep `generateIcon()` if still
referenced elsewhere; otherwise it becomes dead code to remove.

**Rationale**: Reuses the established rendering pipeline (typeface,
digit-size adaptation) rather than duplicating it (FR-014, constitution
Principle VI simplicity). The only new concern is the monochrome/alpha
constraint from Decision 1.

**Alternatives considered**:
- New standalone class → REJECTED: unnecessary parallel implementation.

## Decision 3: Day Number Source & Localization

**Decision**: Day number = Jalali day of `DateUtils.getRealDeviceLocalDate()`
converted via `CalendarConverter.gregorianToJalali()`. Format with
`CalendarConverter.toPersianDigits()` when active calendar type is JALALI;
Latin digits otherwise.

**Rationale**: Device-time authority (Principle I), JDN conversion
(Principle II), existing localization pipeline (Principle IV, FR-007).
Matches how the in-body tile already derives its day number in
`LumaNotificationManager.updateNotification()`.

**Note**: Per spec assumption, the small-icon day number is always the
**Jalali** day regardless of viewed calendar type — but the digit *form*
(Persian vs Latin) follows the active localization per FR-007. Confirm
this pairing during implementation: Jalali day value, digits localized.

## Decision 4: Refresh Lifecycle

**Decision**: No new lifecycle. The small icon is regenerated every time
`LumaNotificationManager.updateNotification()` runs and re-posts
notification 1001. Existing triggers (midnight alarm,
`ReminderRescheduleReceiver` for boot/time/timezone/package-update, app
foreground) already call this path.

**Rationale**: FR-009, FR-010 — refresh in place, no duplicate
notification, ride the existing lifecycle (spec assumption confirmed).

## Open Verification Items (for implementation)

1. Confirm `IconCompat.createWithBitmap()` small-icon path renders the
   day number legibly at status-bar size on API 24 and API 35 (light +
   dark system UI).
2. Confirm single-digit (۹) and double-digit (۳۱) both fit within the
   silhouette without clipping at small-icon scale.
3. Reference check complete (grep for `ic_notification_luma`): the bell
   drawable is used in TWO places —
   `LumaNotificationManager.kt:299` (daily notification) AND
   `EventNotifications.kt:300` (event reminders).

## Decision 5: Do NOT Delete or Replace `ic_notification_luma.xml`

**Decision**: Keep `ic_notification_luma.xml` unchanged. The daily
notification switches to a dynamically-generated small icon via
`setSmallIcon(IconCompat.createWithBitmap(...))`. Event reminders keep
`.setSmallIcon(R.drawable.ic_notification_luma)` exactly as-is.

**Rationale**: FR-015 requires event reminders to keep their current
small icon untouched, and the grep confirms `EventNotifications.kt:300`
still references the bell. Deleting or replacing the drawable (as the
plan's Project Structure section originally suggested) would break the
event-reminder icon or silently change it. Only the daily notification's
`setSmallIcon(...)` call in `LumaNotificationManager.kt` changes.

**Correction to plan.md**: The plan's "REPLACED: bell → Luma calendar
silhouette" for `ic_notification_luma.xml` is superseded — the bell
resource stays for event reminders; the daily notification uses a runtime
bitmap icon instead. No drawable file is replaced or deleted.
