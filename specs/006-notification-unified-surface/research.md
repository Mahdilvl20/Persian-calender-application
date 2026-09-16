# Research: Daily Notification Unified Surface

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Root Cause Analysis

### Current State (After 002 Fix)

The 002 fix successfully:
- Removed `notification_glass_bg` from both XML root layouts (now `@null`)
- Hidden the custom header row to prevent duplicate app names
- Simplified the tile background to subtle `#0DFFFFFF`

**What STILL causes the nested appearance:**

### Primary Cause: DecoratedCustomViewStyle System Container

`NotificationCompat.DecoratedCustomViewStyle()` tells Android to
render custom RemoteViews inside the system's standard notification
template. This template provides:

1. **System container**: A rounded-corner card with its own
   background (light card on light mode, dark card on dark mode).
   This is the outer "rectangle" — it's non-removable when using
   DecoratedCustomViewStyle.

2. **System padding**: The system applies its own padding around
   the custom view inside this container.

3. **System header**: The system generates a header row showing
   app name, icon, and timestamp — in addition to any custom
   header in RemoteViews.

### Secondary Cause: Remaining Child Backgrounds

Inside the system card, child elements still have visible
backgrounds:
- `notification_calendar_tile_bg` (#0DFFFFFF + 8dp corners)
- `notification_action_btn_primary_bg` (indigo gradient + stroke)
- `notification_action_btn_bg` (#14FFFFFF + stroke)

These create additional visual rectangles inside the system container.

## Fix Strategy Options

### Option A: Remove DecoratedCustomViewStyle (Recommended)

**What changes**: Remove `.setStyle(DecoratedCustomViewStyle())`
from the builder. Without it, `setCustomContentView` /
`setCustomBigContentView` render RemoteViews directly in the
notification without the system container.

**Trade-off**: Loses the system-generated header (app name, icon,
timestamp). The custom header row (currently hidden with
`visibility="gone"`) must be made visible again and styled to
replace the system header.

**Result**: No system container, no system padding, full control
over the notification surface. The Luma content IS the notification.

### Option B: Keep DecoratedCustomViewStyle, Minimize Inner

**What changes**: Keep the style but remove all remaining inner
backgrounds (tile, action buttons). Use only spacing and
typography for visual hierarchy.

**Trade-off**: The system container remains visible. The outer
"rectangle" persists. Inner rectangles are reduced but the overall
nested appearance remains.

**Result**: Better than current but not fully unified.

### Option C: Switch to BigTextStyle

**What changes**: Use `NotificationCompat.BigTextStyle()` instead.
This gives a text-focused notification with expand/collapse.

**Trade-off**: Loses the custom visual layout entirely. Not suitable
for a visually rich calendar notification.

**Result**: Not applicable — wrong design direction.

## Decision

**Option A (Remove DecoratedCustomViewStyle)** is the only approach
that eliminates the system container and achieves a truly unified
surface. The trade-off (implementing our own header) is acceptable
because:
- The header is simple (app name + timestamp)
- We already have the icon and localization infrastructure
- Full visual control is worth the small implementation effort

## Verification of Findings

| Finding | Source | Evidence |
|---------|--------|----------|
| Root backgrounds removed | 002 fix | Both layouts have `android:background="@null"` |
| DecoratedCustomViewStyle adds system container | Android docs + audit | Builder uses `.setStyle(DecoratedCustomViewStyle())` at line 316 |
| Custom header hidden, not removed | XML audit | `android:visibility="gone"` on header rows |
| notification_glass_bg.xml is dead code | Audit | No layout references it anymore |
| Child backgrounds remain | XML audit | Tile and action button drawables still applied |
