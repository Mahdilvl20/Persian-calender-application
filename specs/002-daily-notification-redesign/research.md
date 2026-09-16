# Research: Daily Notification Redesign

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Root Cause Analysis

### Why "Rectangle Inside Rectangle" Happens

The notification visual nesting is caused by 4 layers of backgrounds:

```
Layer 0: System notification card (Android/OEM surface)
  └── Layer 1: DecoratedCustomViewStyle wrapper
        └── Layer 2: notification_glass_bg on root LinearLayout
              └── Layer 3: notification_calendar_tile_bg on tile
              └── Layer 4: notification_action_btn_*_bg on buttons
```

**Layer 0** is Android's system notification card — a rounded rectangle
provided by the OS. We cannot remove it.

**Layer 1** is `DecoratedCustomViewStyle()` which wraps custom RemoteViews
inside the system card. This adds the standard notification header (icon,
app name, timestamp) above our custom content.

**Layer 2** is `@drawable/notification_glass_bg` applied to the root
LinearLayout of both XML layouts. This drawable is a dark (#EB0B101D)
rounded rectangle with 24dp corners and a 1dp indigo stroke (#33818CF8).
This creates a visible second card inside the system notification.

**Layer 3** is `@drawable/notification_calendar_tile_bg` on the mini
calendar tile — a third rounded rectangle with its own gradient and stroke.

**Layer 4** is the action button backgrounds in the expanded view.

### Decision: Fix Strategy

**Decision**: Remove Layer 2 (notification_glass_bg) from both XML root
layouts and simplify Layer 3 (tile background). Keep Layer 0 (system)
and Layer 1 (DecoratedCustomViewStyle) as-is.

**Rationale**: The system notification card already provides a dark
rounded surface. Adding notification_glass_bg on top creates the visible
double-card effect. By removing the root background and reducing the
tile to a minimal integrated element, the content sits directly on the
system surface — achieving the "one unified surface" goal.

**Alternatives considered**:
- Switch away from DecoratedCustomViewStyle: Rejected because we'd
  lose the system header (app icon, app name, timestamp) for free.
  Re-implementing this inside RemoteViews adds complexity.
- Keep glass_bg but make it transparent: Rejected because a fully
  transparent background defeats the purpose of having it; better
  to remove it entirely.
- Use a single full-bleed background with no tile separation: Partially
  adopted — the tile gets a subtle integrated treatment instead of
  its own distinct card.

## Notification Header (App Name Duplication)

**Finding**: The codebase does NOT use `setSubText()`. The header
row in the XML layouts is set to `visibility="gone"`. Android's
`DecoratedCustomViewStyle` automatically provides the app name and
timestamp in the system header area.

**Decision**: No change needed for app name duplication. The system
header already shows "Luma Calendar · now" correctly.

**Rationale**: No setSubText() call exists. The manual header row
in XML is hidden. The system handles app identity.

## Snooze Duration

**Finding**: Currently hardcoded to 60 minutes in
LumaNotificationActionReceiver.kt (line 64: `snoozeDuration = 60 * 60 * 1000L`).
Toast message is also hardcoded ("Snoozed for 1 hour" / Persian equivalent).

**Decision**: Make snooze duration user-configurable via
NotificationPreferences. Default remains 60 minutes. Settings screen
gets a new row. Toast message dynamically reflects the chosen duration.

**Rationale**: FR-011 from the spec requires user-configurable snooze.
The existing NotificationPreferences infrastructure (SharedPreferences)
is the natural place to store this.

**Alternatives considered**:
- Preset options (30min/1hr/2hr): Rejected per user's choice of
  fully configurable.
- Slider in settings: Rejected as over-engineered; a simple numeric
  input or dropdown is sufficient.

## Color Tokens for Notification

**Finding**: The notification currently uses:
- Root background: notification_glass_bg (#EB0B10D + stroke #33818CF8)
- Tile: notification_calendar_tile_bg (gradient #336366F1 → #181E1B4B + stroke #4D818CF8)
- Action primary: notification_action_btn_primary_bg (gradient #336366F1 → #1F4F46E5 + stroke #59818CF8)
- Action secondary: notification_action_btn_bg (solid #14FFFFFF + stroke #24FFFFFF)
- Accent color: notification_accent (#FF6366F1)
- Text colors: white (#FFFFFF), secondary (#94A3B8), tile text (#C7D2FE)

**Decision**: After removing notification_glass_bg, the tile and action
button backgrounds remain but are simplified. The tile loses its heavy
gradient/stroke and becomes a subtle integrated element. Action buttons
keep their existing treatment (it's already subtle and well-designed).

**Rationale**: The action buttons and tile are secondary nesting
problems. The primary fix (removing root glass_bg) eliminates the
main "rectangle inside rectangle." Simplifying the tile completes
the unified surface goal.
