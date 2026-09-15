# Feature Specification: Luma Calendar Project Rules

**Feature Branch**: `001-project-rules`

**Created**: 2026-09-16

**Status**: Draft

**Input**: Comprehensive project rules covering architecture, UI design, notifications, error handling, testing, and development governance for the Luma Calendar Android application.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calendar Core & Date Accuracy (Priority: P1)

As a user, I expect the calendar to always show the correct date based on my
device's real local time, supporting Jalali, Gregorian, and Hijri calendars
with accurate conversions between them.

**Why this priority**: Date accuracy is the fundamental purpose of a calendar
app. Without correct dates, all other features are meaningless.

**Independent Test**: Can be fully tested by launching the app, verifying
today's date matches the device date in all three calendar systems, and
switching between calendar types while confirming date consistency.

**Acceptance Scenarios**:

1. **Given** the device date is 2026-09-16, **When** the app launches,
   **Then** today is displayed as 16 September 2026 (Gregorian),
   25 Shahrivar 1405 (Jalali), and 24 Rabi al-Awwal 1448 (Hijri).
2. **Given** the user selects a different date, **When** they look for
   "today", **Then** the real device date is highlighted, not the selected date.
3. **Given** the user switches from Jalali to Gregorian calendar, **When**
   the same real-world date is displayed, **Then** all three systems show
   the equivalent date with no off-by-one errors.
4. **Given** the device timezone changes, **When** the app resumes, **Then**
   today's date updates to reflect the new timezone.

---

### User Story 2 - Premium Liquid Glass UI (Priority: P1)

As a user, I expect a consistent, premium visual experience using Liquid
Glass / Glassmorphism design with dark backgrounds, translucent surfaces,
ambient glow, and Vazirmatn typography throughout the app.

**Why this priority**: Visual identity and consistency define the brand.
Inconsistent UI breaks user trust.

**Independent Test**: Can be tested by navigating through all screens
(Calendar, Search, Settings) and verifying consistent glass surfaces,
typography weights, color tokens, and ambient glow effects.

**Acceptance Scenarios**:

1. **Given** the user opens any screen, **When** they observe the UI,
   **Then** glass surfaces use the defined `GlassSurface*` color tokens
   with proper transparency levels.
2. **Given** the user views any text, **When** they check the typography,
   **Then** Vazirmatn font is applied with the correct weight (Regular
   for body, Medium for buttons/navigation, SemiBold for titles).
3. **Given** the user navigates between screens, **When** they observe
   transitions, **Then** the dark background with blue/violet ambient
   glow is consistently visible.
4. **Given** a new feature is added, **When** it follows the design
   system, **Then** it uses existing glass components and color tokens
   without inventing new visual primitives.

---

### User Story 3 - Daily Persistent Notification (Priority: P1)

As a user, I expect a single persistent daily notification showing today's
calendar information that remains in the notification shade across reboots,
midnight transitions, and app restarts without duplication.

**Why this priority**: The daily notification is the app's most visible
persistent feature and must be rock-solid with zero duplication.

**Independent Test**: Can be tested by enabling notifications, verifying
exactly one notification with ID 1001 exists, restarting the app,
rebooting the device, and confirming no duplicates appear.

**Acceptance Scenarios**:

1. **Given** daily notification is enabled, **When** the user checks the
   notification shade, **Then** exactly one notification with ID 1001 on
   channel `luma_calendar_daily` exists with `setOngoing(true)`.
2. **Given** a notification is active, **When** the user taps it, **Then**
   it is NOT dismissed (persistent/ongoing).
3. **Given** midnight passes, **When** the new day starts, **Then**
   notification 1001 is updated (not recreated) with the new date.
4. **Given** the device reboots, **When** the system starts, **Then**
   notification 1001 is restored with correct today's date.
5. **Given** the user disables daily notification, **When** they check,
   **Then** notification 1001 is cancelled; re-enabling creates exactly
   one new notification with ID 1001.

---

### User Story 4 - Event Reminder Notifications (Priority: P2)

As a user, I expect event reminders to fire at the exact scheduled time,
independently from the daily notification, with correct event details loaded
from the database.

**Why this priority**: Reminders are essential for a calendar's utility
but are secondary to the daily calendar display.

**Independent Test**: Can be tested by creating an event with a 1-minute
reminder, waiting for the alarm, and verifying the notification appears
at the correct time with event details.

**Acceptance Scenarios**:

1. **Given** an event at 09:50 with 1-minute-before reminder, **When**
   the time reaches 09:49, **Then** a notification fires on channel
   `event_reminders` with event-specific ID (not 1001).
2. **Given** an event reminder is scheduled, **When** the user edits the
   event, **Then** the old alarm is cancelled and a new one is scheduled.
3. **Given** an event is deleted, **When** the scheduled reminder time
   arrives, **Then** no notification fires (alarm was cancelled).
4. **Given** both daily and event notifications exist, **When** the user
   checks the shade, **Then** both are visible simultaneously without
   replacing each other.

---

### User Story 5 - RTL/LTR Localization (Priority: P2)

As a Persian user, I expect the entire UI to flow right-to-left with
proper system LayoutDirection. As an English user, I expect left-to-right
layout. Text must never be manually reversed.

**Why this priority**: Incorrect RTL breaks readability for the primary
Persian user base.

**Independent Test**: Can be tested by switching calendar type to Jalali
and verifying the entire layout mirrors to RTL, then switching to
Gregorian and verifying LTR layout.

**Acceptance Scenarios**:

1. **Given** calendar type is Jalali, **When** the user views any
   screen, **Then** LayoutDirection is RTL and text flows naturally.
2. **Given** calendar type is Gregorian, **When** the user views any
   screen, **Then** LayoutDirection is LTR.
3. **Given** a Persian string is displayed, **When** the user reads it,
   **Then** the text is NOT reversed or manipulated for RTL simulation.

---

### User Story 6 - Error Resilience (Priority: P2)

As a user, I expect the app to never crash regardless of invalid input,
network failures, missing permissions, or calendar conversion edge cases.
Errors should be logged and handled gracefully.

**Why this priority**: Crashes destroy user trust. A calendar must be
reliable under all conditions.

**Independent Test**: Can be tested by entering invalid dates/times,
disconnecting network, revoking notification permission, and switching
calendars rapidly, confirming no crashes occur.

**Acceptance Scenarios**:

1. **Given** invalid date input (e.g., "99-99-9999"), **When** the
   system processes it, **Then** no crash occurs; a safe default or
   error message is shown.
2. **Given** network is unavailable, **When** the app launches,
   **Then** the calendar grid displays correctly from local cache.
3. **Given** notification permission is denied, **When** the system
   attempts to show a notification, **Then** a SecurityException is
   caught, logged, and no crash occurs.
4. **Given** a calendar conversion edge case (e.g., 29 Esfand in
   non-leap year), **When** conversion runs, **Then** a safe fallback
   is used without crash.

---

### Edge Cases

- What happens when the device clock is set backwards past midnight?
  The notification should update to the previous date.
- What happens when two events have reminders at the exact same time?
  Both notifications should appear with their respective event IDs.
- What happens when a notification channel is disabled by the user in
  system settings? The app should detect this and log it, not crash.
- What happens during DST transitions? Date calculations must use the
  device timezone to avoid duplicate or skipped hours.
- What happens when Room database is corrupted? The app should launch
  with an empty state and log the error, not crash.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST use device local timezone for all "today"
  calculations via the established DateUtils device-time API.
- **FR-002**: System MUST convert dates between Jalali, Gregorian, and
  Hijri using Julian Day Number as the canonical intermediate.
- **FR-003**: System MUST maintain a single persistent daily notification
  with ID 1001 on channel `luma_calendar_daily`, set as ongoing.
- **FR-004**: System MUST cancel and recreate event reminders on event
  edit/delete, using event-specific notification IDs on channel
  `event_reminders`.
- **FR-005**: System MUST apply RTL layout direction when calendar type
  is Jalali or Hijri, and LTR for Gregorian.
- **FR-006**: System MUST display Vazirmatn font with weight rules:
  Regular (body), Medium (buttons/navigation), SemiBold (titles),
  Bold (major headings only).
- **FR-007**: System MUST use the Liquid Glass design system tokens
  (GlassSurface*, AmbientGlow*, category colors) for all UI components.
- **FR-008**: System MUST schedule midnight update to refresh daily
  notification at date change without creating notification loops.
- **FR-009**: System MUST update daily notification on date/time/timezone
  changes, device reboot, and package update using ID 1001.
- **FR-010**: System MUST NOT create duplicate daily notifications under
  any circumstance (restart, reboot, midnight, settings change).
- **FR-011**: System MUST load event data from Room database for
  reminder notifications; if event does not exist, notification MUST
  NOT be created.
- **FR-012**: System MUST handle all parsing exceptions (date, time,
  calendar conversion) gracefully with logging and safe defaults.
- **FR-013**: System MUST NOT use foreground service for daily
  notification persistence.
- **FR-014**: System MUST NOT use Android Live Update for notification
  persistence.
- **FR-015**: System MUST cancel notification 1001 when daily
  notification is disabled by user, and recreate it when re-enabled.
- **FR-016**: System MUST use idempotent channel creation; channels
  MUST NOT be recreated on each notification update.
- **FR-017**: System MUST prevent AlarmManager duplicates: cancel
  existing alarm before scheduling new one for same event.
- **FR-018**: System MUST use device local date for dynamic day icon
  (not selected date).
- **FR-019**: System MUST NOT create ActivityAlias for icon switching;
  MainActivity MUST remain stable.
- **FR-020**: System MUST log notification operations with tag
  `LumaDailyNotification` for debugging.
- **FR-021**: System MUST prevent notification loops, receiver loops,
  alarm loops, duplicate coroutines, and main thread blocking.
- **FR-022**: System MUST NOT use hardcoded dates, events, or business
  data in production code.
- **FR-023**: System MUST maintain backward compatibility when modifying
  existing features; current consumers must be identified first.
- **FR-024**: System MUST NOT create parallel implementations for the
  same capability (one notification manager, one event scheduler).
- **FR-025**: System MUST find all call sites, dependencies, receivers,
  manifest entries, and alarms before refactoring.

### Key Entities

- **CalendarEvent**: A user-created event with title, date, time,
  category, color, location, notes, reminder offset (free-form integer
  in minutes, 0–1440, where 0 means no reminder), and calendar type.
- **DailyNotification**: The single persistent notification (ID 1001)
  showing today's date in all three calendar systems.
- **EventReminder**: A time-triggered notification tied to a specific
  CalendarEvent, with its own notification ID on the event_reminders
  channel.
- **Holiday**: Official holidays for Iran (Jalali) and US Federal
  (Gregorian), loaded from providers and API.
- **PersianCalendarDay**: Day metadata from the Persian calendar API
  including holiday descriptions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: App displays correct today's date in all three calendar
  systems matching the device date in 100% of cases.
- **SC-002**: Daily notification exists as exactly one instance (ID 1001)
  across app restarts, device reboots, and midnight transitions.
- **SC-011**: When the user enables daily notification in settings, a
  notification with ID 1001 appears in the shade within 2 seconds.
- **SC-003**: Event reminders fire at the mathematically correct trigger
  time (event time minus reminder offset) within 1-second accuracy.
- **SC-004**: Zero crashes from user input, network failure, permission
  denial, or calendar conversion edge cases.
- **SC-005**: RTL layout is correctly applied for Jalali/Hijri calendar
  types with no reversed text strings.
- **SC-006**: All screens maintain consistent Liquid Glass visual design
  with no generic Material defaults visible.
- **SC-007**: App remains fully functional offline for calendar display,
  date navigation, and event management.
- **SC-008**: No notification loops, alarm loops, or duplicate
  PendingIntents exist under any usage pattern.
- **SC-009**: Build succeeds and `./gradlew test` passes with all
  existing tests green after any change.
- **SC-010**: Typography follows Vazirmatn weight hierarchy across all
  screens with no unjustified Bold usage.

## Clarifications

### Session 2026-09-16

- Q: What reminder offset options should be available? → A: Free-form
  integer input in minutes (0–1440), not preset options.
- Q: How quickly should the daily notification appear after enable? →
  A: Within 2 seconds, async acceptable.

## Assumptions

- Target devices run Android 7.0 (API 24) or higher.
- Users expect both Persian (Jalali) and Gregorian calendar support.
- The existing MVVM architecture with single LumaViewModel is maintained.
- The existing Room database schema (CalendarEvent entity) is the
  authoritative data source for events.
- The existing notification infrastructure (LumaNotificationManager for
  daily, EventNotificationScheduler for events) is the single
  implementation for each capability.
- The existing Design System (GlassComponents, Color tokens, AccentPresets)
  is the canonical source for all UI styling.
- Firebase AI / Gemini integration is optional and degrades gracefully.
- ProGuard remains disabled for release builds.
- The Vazirmatn font files (regular, medium, semibold, bold) are
  bundled in the app resources.
