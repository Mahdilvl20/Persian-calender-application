# Research: Remove Redundant Notification Header

**Date**: 2026-09-17

## Finding

The notification XML layouts contain a header LinearLayout with:
- ImageView (app icon, ic_notification_luma)
- TextView ("Luma Calendar")

This is visible inside the RemoteViews content area, duplicating the
Android system header which already shows "Luma Calendar · now".

**Decision**: Remove the header LinearLayout entirely from both
notification_luma_calendar.xml and notification_luma_calendar_expanded.xml.

**Rationale**: The system header provides app identity. The internal
header is redundant and wastes vertical space.

**Alternative**: Keep internal header but hide it — rejected because
dead code is worse than removed code.
