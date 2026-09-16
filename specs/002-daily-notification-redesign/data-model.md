# Data Model: Daily Notification Redesign

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Entities

### DailyNotification (Virtual)

Single persistent notification. Not a database entity.

| Property | Value | Source | Rule |
|----------|-------|--------|------|
| ID | 1001 (constant) | LumaNotificationManager.NOTIFICATION_ID_DAILY | FR-001 |
| Channel | "luma_calendar_daily" | LumaNotificationManager.CHANNEL_ID_DAILY | FR-001 |
| Ongoing | true | Builder | FR-002 |
| AutoCancel | false | Builder | FR-002 |
| Style | DecoratedCustomViewStyle | Builder | Research decision |
| Priority | PRIORITY_DEFAULT | Builder | — |
| Category | CATEGORY_EVENT | Builder | — |
| Visibility | VISIBILITY_PUBLIC | Builder | — |

### NotificationLayout (Virtual)

RemoteViews composition for the notification.

| Property | Layout File | Content |
|----------|-------------|---------|
| Collapsed | notification_luma_calendar.xml | App icon, primary date, secondary date, mini tile |
| Expanded | notification_luma_calendar_expanded.xml | Primary date, secondary dates, daily message, mini tile, 3 actions |

**Collapsed layout elements** (after redesign):
```
LinearLayout (root, NO background drawable)
  ├── ImageView (app icon, 38x38dp)
  ├── LinearLayout (content column, weight=1)
  │     ├── TextView (primary date, SemiBold)
  │     └── TextView (secondary date, Regular)
  └── LinearLayout (mini tile, subtle integrated bg)
        ├── TextView (month name, Regular)
        └── TextView (day number, SemiBold)
```

**Expanded layout elements** (after redesign):
```
LinearLayout (root, NO background drawable)
  ├── LinearLayout (date row, horizontal)
  │     ├── LinearLayout (date column, weight=1)
  │     │     ├── TextView (primary date, SemiBold)
  │     │     ├── TextView (secondary date, Regular)
  │     │     └── TextView (daily message, Regular)
  │     └── LinearLayout (mini tile, subtle integrated bg)
  └── LinearLayout (actions row, horizontal)
        ├── LinearLayout (Today, weight=1, action bg)
        ├── LinearLayout (New Event, weight=1, action bg)
        └── LinearLayout (Remind Later, weight=1, action bg)
```

### NotificationPreferences (SharedPreferences)

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| daily_calendar_notification_enabled | Boolean | true | Daily notification on/off |
| event_reminders_enabled | Boolean | true | Event reminders on/off |
| notification_permission_requested | Boolean | false | Permission prompt tracking |
| active_calendar_type | String | "JALALI" | Calendar type for notification |
| daily_notification_snooze_minutes | Int | 60 | NEW: Snooze duration in minutes |

**Validation**: snooze_minutes MUST be in range 15–480 (15 min to 8 hours).
Values outside this range are clamped to nearest bound.

### SnoozeDuration (New Settings Entity)

| Property | Value |
|----------|-------|
| Storage key | "daily_notification_snooze_minutes" |
| Type | Int (minutes) |
| Default | 60 |
| Min | 15 |
| Max | 480 |
| Display | Dropdown with presets: 15, 30, 45, 60, 90, 120, 180, 240, 480 minutes |

## State Transitions

### DailyNotification Lifecycle (Unchanged)

```
[Disabled] → enable() → [Active: ID 1001]
[Active] → disable() → [Cancelled: ID 1001 removed]
[Active] → midnight/reboot/tz → [Updated: ID 1001 refreshed]
[Active] → app restart → [No change: ID 1001 persists]
[Active] → Remind Later → [Snoozed: ID 1001 updated with ack, timer set]
[Snoozed] → timer fires → [Active: ID 1001 refreshed with date content]
```

### Snooze Flow (Modified)

```
User taps Remind Later
  → Read snooze_minutes from NotificationPreferences
  → Show Toast with duration ("Snoozed for X minutes")
  → Update notification 1001 with snooze acknowledgment
  → Schedule AlarmManager for snooze_minutes from now
  → Alarm fires MidnightUpdateReceiver(SNOOZE_WAKEUP)
  → updateNotification() restores normal date content
```
