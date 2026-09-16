# Settings Contract: Snooze Duration

**Date**: 2026-09-16
**Spec**: [spec.md](../spec.md)

## New Setting: Daily Notification Snooze Duration

### Storage

| Property | Value |
|----------|-------|
| SharedPreferences file | "notification_preferences" |
| Key | "daily_notification_snooze_minutes" |
| Type | Int |
| Default | 60 |
| Range | 15–480 (clamped) |

### Settings UI

A new row in SettingsScreen below the existing notification toggle:

**Label**: "Snooze Duration" (English) / "مدت یادآوری" (Persian)
**Display**: Current value with unit (e.g., "60 minutes" / "۶۰ دقیقه")
**Input**: Dropdown/picker with preset values:
- 15 min, 30 min, 45 min, 60 min (default), 90 min, 2 hours, 3 hours, 4 hours, 8 hours

### Behavior

- Reading: `NotificationPreferences.getSnoozeMinutes(context)` returns
  the stored value or 60 if unset.
- Writing: `NotificationPreferences.setSnoozeMinutes(context, minutes)`
  stores the value. Out-of-range values are clamped.
- The snooze duration is read at Remind Later tap time, not at
  notification build time. This means changing the setting takes
  effect immediately for the next snooze.

### LumaNotificationActionReceiver Modification

When ACTION_REMIND_LATER fires:
1. Read snooze duration from NotificationPreferences
2. Compute alarm trigger: `SystemClock.elapsedRealtime() + (snoozeMinutes * 60 * 1000L)`
3. Show Toast: "Snoozed for {duration}" / "به مدت {duration} یادآوری شد"
4. Schedule alarm targeting MidnightUpdateReceiver with SNOOZE_WAKEUP

### Toast Localization

Duration text must be localized:
- English: "Snoozed for {X} minutes" / "Snoozed for {X} hours"
- Persian: "به مدت {X} دقیقه یادآوری شد" / "به مدت {X} ساعت یادآوری شد"

Use existing AppStrings/LocalizationManager for these strings.
