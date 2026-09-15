# Data Model: Luma Calendar Project Rules

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Entities

### CalendarEvent (Room Entity)

Table: `calendar_events`

| Field | Type | Default | Constraints | Rule Reference |
|-------|------|---------|-------------|----------------|
| id | Long | auto-generated | PK, auto-increment | FR-011 |
| title | String | — | NOT NULL | — |
| date | String | — | "YYYY-MM-DD" format | FR-001 |
| startTime | String | — | "HH:mm" format | — |
| endTime | String | "" | Legacy/optional | — |
| category | String | — | "Work"/"Personal"/"Health"/"Social"/"Finance"/"Special" | — |
| colorHex | String | — | "#RRGGBB" format | — |
| location | String | "" | — | — |
| notes | String | "" | — | — |
| reminderMinutes | Int | 15 | 0–1440 (0 = no reminder) | FR-004, Clarification Q1 |
| calendarType | String | "Personal" | — | — |

**Queries**:
- `getAllEvents()`: Flow, ordered by date ASC, startTime ASC
- `getEventById(id)`: suspend, single event lookup (used by FR-011)
- `getEventsForDate(date)`: Flow, filtered by date
- `searchEvents(query)`: Flow, LIKE on title/notes/location
- `getEventsWithReminders()`: suspend, WHERE reminderMinutes > 0
- `insertEvent()`: OnConflictStrategy.REPLACE
- `deleteById(id)`: suspend

### DailyNotification (Virtual Entity)

Not a database entity. Represents the single persistent notification.

| Property | Value | Rule Reference |
|----------|-------|----------------|
| Notification ID | 1001 (constant) | FR-003, FR-010 |
| Channel ID | "luma_calendar_daily" | FR-003, FR-016 |
| Ongoing | true | FR-003 |
| AutoCancel | false | FR-003 |
| Content | Today's date in 3 calendar systems | User Story 3 |

**State Transitions**:
```
[Disabled] → enable() → [Active: ID 1001]
[Active] → disable() → [Cancelled: ID 1001 removed]
[Active] → midnight/boot/timezone → [Updated: ID 1001 refreshed]
[Active] → app restart → [No change: ID 1001 persists]
```

### EventReminder (Virtual Entity)

Not a database entity. Derived from CalendarEvent.reminderMinutes.

| Property | Value | Rule Reference |
|----------|-------|----------------|
| Channel ID | "event_reminders" | FR-004 |
| Notification ID | event-specific (derived from event.id) | FR-004 |
| Ongoing | false | — |
| Trigger time | event.date + event.startTime - reminderMinutes | FR-004, SC-003 |

**State Transitions**:
```
[Event Created] → scheduleAlarm() → [Alarm Active]
[Event Edited] → cancelAlarm(old) + scheduleAlarm(new) → [Alarm Updated]
[Event Deleted] → cancelAlarm() → [Alarm Cancelled]
[Alarm Fires] → loadEventFromDB(id) → if exists: show notification
                                  → if not: do nothing (FR-011)
```

### Holiday

| Field | Type | Notes |
|-------|------|-------|
| id | String | Unique identifier |
| name | String | Localized display name |
| dateString | String | Canonical Gregorian "YYYY-MM-DD" |
| isOfficialHoliday | Boolean | True for official holidays |
| type | HolidayType | OFFICIAL_HOLIDAY or OBSERVANCE |
| calendarType | CalendarType | JALALI, GREGORIAN, or HIJRI |
| localizedDayDisplay | String | e.g., "۱ فروردین" |
| description | String | — |

**Providers**: IranHolidayProvider (Jalali), USFederalHolidayProvider (Gregorian)

### PersianCalendarDay

| Field | Type | Notes |
|-------|------|-------|
| date | String | Gregorian "YYYY-MM-DD" |
| shamsiDate | String | Shamsi "YYYY/MM/DD" |
| isHoliday | Boolean | From API |
| holidayDescription | String? | From API |

**Source**: Persian calendar API via Retrofit, cached to disk JSON and
in-memory ConcurrentHashMap

## Relationships

```
CalendarEvent ──(reminderMinutes>0)──→ EventReminder (derived, not stored)
CalendarEvent ──(date matching)──→ Holiday (lookup via HolidayService)
PersianCalendarDay ──(date matching)──→ Holiday (Jalali holidays merged)
DailyNotification ──(uses all 3 calendar systems)──→ CalendarConverter
```

## Validation Rules

| Rule | Entity | Field | Validation | Source |
|------|--------|-------|------------|--------|
| VR-001 | CalendarEvent | date | Must match "YYYY-MM-DD" | FR-001 |
| VR-002 | CalendarEvent | startTime | Must match "HH:mm" | — |
| VR-003 | CalendarEvent | reminderMinutes | 0–1440 integer | Clarification Q1 |
| VR-004 | CalendarEvent | title | Non-empty after trim | — |
| VR-005 | DailyNotification | id | Always 1001 | FR-003, FR-010 |
| VR-006 | EventReminder | id | Must differ from 1001 | FR-004 |
