# Notification Contract

**Date**: 2026-09-16
**Spec**: [spec.md](../spec.md)

## Daily Notification Contract

### Constants

| Constant | Value | Invariant |
|----------|-------|-----------|
| NOTIFICATION_ID | 1001 | Never changes, never incremented |
| CHANNEL_ID | "luma_calendar_daily" | Never renamed to work around bugs |
| ONGOING | true | Always persistent |
| AUTO_CANCEL | false | Tap never dismisses |

### Operations

**create()**
- Pre: Channel exists and is enabled; notification not already active
- Post: Exactly one notification with ID 1001 in shade
- Idempotent: Calling create() when ID 1001 exists → update instead

**update()**
- Pre: ID 1001 exists
- Post: ID 1001 shows new date; no second notification created
- Called by: app startup, foreground resume, midnight, reboot,
  date/time/timezone change, package update, preference change

**cancel()**
- Pre: ID 1001 may or may not exist
- Post: ID 1001 removed from shade
- Called by: user disables daily notification

### Invariants

1. At most one notification with ID 1001 exists at any time
2. update() never creates a second notification
3. Channel is created once, idempotently
4. No foreground service required
5. Completion target: within 2 seconds of user enabling

## Event Reminder Contract

### Constants

| Constant | Value | Invariant |
|----------|-------|-----------|
| CHANNEL_ID | "event_reminders" | Separate from daily |
| ONGOING | false | Dismissible |
| NOTIFICATION_ID | event.id-based | Never equals 1001 |

### Operations

**schedule(event)**
- Pre: event exists in DB, event.reminderMinutes > 0
- Post: AlarmManager set for (event.date + event.startTime - reminderMinutes)
- Cancel-then-schedule: always cancel existing alarm for this event first

**cancel(eventId)**
- Pre: Alarm may or may not exist
- Post: No alarm fires for this eventId; notification removed if visible
- Called by: event edit, event delete

**fire(eventId)**
- Pre: Alarm triggered
- Step 1: Load event from Room DB by eventId
- Step 2: If event not found → abort, no notification (FR-011)
- Step 3: If event found → build and show notification on "event_reminders"
- Post: User sees event-centric notification with Open/Remind Later actions

### Invariants

1. Event reminder never replaces daily notification (different IDs, channels)
2. Daily notification never replaces event reminder
3. Both can coexist in shade simultaneously
4. Trigger time = event time - reminder offset, within 1s accuracy
5. No duplicate alarms for same event
