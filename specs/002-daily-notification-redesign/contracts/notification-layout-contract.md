# Notification Layout Contract

**Date**: 2026-09-16
**Spec**: [spec.md](../spec.md)

## Root Layout Rule

Both collapsed and expanded XML layouts MUST NOT apply any background
drawable to the root LinearLayout. The system notification card provides
the surface. Our content sits directly on it.

**Before** (prohibited):
```xml
<LinearLayout
    android:background="@drawable/notification_glass_bg"
    ...>
```

**After** (required):
```xml
<LinearLayout
    android:background="@null"
    ...>
```

## Mini Calendar Tile Rule

The tile MUST use a subtle integrated background, NOT a distinct card.
No heavy gradient. No prominent stroke. The tile should feel like a
subtle section of the main surface, not a separate element.

**Acceptable**: Subtle translucent background (#0DFFFFFF or similar),
no stroke, 8dp corners.

**Prohibited**: Gradient fills, visible border/stroke, corner radius
matching or exceeding the root container.

## Action Button Rule

Action buttons MAY retain their existing subtle background treatment.
They are compact enough that they don't create a "card inside card"
appearance. The key requirement is equal visual weight and consistent
height (34dp).

## RemoteViews Compatibility

All notification views MUST be RemoteViews-compatible:
- LinearLayout, RelativeLayout, FrameLayout
- TextView, ImageView
- ProgressBar (indeterminate)
- No Compose, no custom View subclasses, no RecyclerView

## View ID Stability

Existing view IDs MUST be preserved to avoid breaking
LumaNotificationManager's RemoteViews population:
- notification_main_date
- notification_secondary_date
- notification_daily_message
- notification_tile_month
- notification_tile_day
- notification_action_today
- notification_action_new_event
- notification_action_remind_later

New view IDs may be added but existing ones MUST NOT be renamed.

## Typography in RemoteViews

RemoteViews does not support Compose typography. Font weights are
set via XML attributes:
- `android:textStyle="bold"` for SemiBold equivalent (primary date, day)
- `android:textStyle="normal"` for Regular (secondary text, message)
- Font family set via `android:fontFamily` if Vazirmatn is bundled

Text sizes (SP):
- Primary date: 14sp (collapsed), 16sp (expanded)
- Secondary date: 11sp (collapsed), 12sp (expanded)
- Daily message: 12sp (expanded only)
- Tile month: 9sp (collapsed), 10sp (expanded)
- Tile day: 18sp (collapsed), 22sp (expanded)
- Action labels: 10sp
