# Feature Specification: Daily Notification Redesign

**Feature Branch**: `002-daily-notification-redesign`

**Created**: 2026-09-16

**Status**: Draft

**Input**: Redesign the daily persistent notification to eliminate the
"rectangle inside rectangle" visual problem and create one unified,
premium Luma Calendar notification surface.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Unified Notification Surface (Priority: P1)

As a user, I want the daily calendar notification to look like ONE
cohesive Luma Calendar surface, not a generic Android notification
containing an embedded card. The notification should feel like a native
extension of the app.

**Why this priority**: The current nested-card appearance is the primary
visual defect. Fixing it is the core goal of this feature.

**Independent Test**: Pull down the notification shade and visually
verify that the notification reads as a single unified surface with no
visible "card inside card" nesting.

**Acceptance Scenarios**:

1. **Given** the daily notification is active, **When** the user views
   the notification shade, **Then** the notification appears as one
   cohesive dark glass surface with no inner rounded-rectangle card.
2. **Given** the notification is displayed, **When** the user inspects
   the layout hierarchy, **Then** there is no nested glass card or
   visually dominant inner container.
3. **Given** the notification is shown, **When** the user observes the
   visual design, **Then** it uses the Luma Calendar design language
   (dark glass, translucent surfaces, subtle borders, ambient glow).
4. **Given** the notification exists, **When** the user checks the
   notification details, **Then** it remains Notification ID 1001 on
   channel luma_calendar_daily with ongoing/persistent behavior.

---

### User Story 2 — Date Display & Calendar Tile (Priority: P1)

As a user, I want the notification to prominently show today's date in
the active calendar system with secondary dates for the other two
calendars, plus a visually integrated mini calendar tile showing the
month and day number.

**Why this priority**: Date information is the notification's primary
purpose. It must be clear, accurate, and visually balanced with the tile.

**Independent Test**: Verify the primary date matches the device date
in the active calendar, secondary dates show the other two calendars,
and the mini tile shows the correct month and day.

**Acceptance Scenarios**:

1. **Given** the active calendar is Jalali, **When** the user reads
   the notification, **Then** the primary date is the Jalali date
   (e.g., "چهارشنبه ۲۵ شهریور ۱۴۰۵") and secondary dates show
   Gregorian and Hijri.
2. **Given** the active calendar is Gregorian, **When** the user reads
   the notification, **Then** the primary date is Gregorian and
   secondary dates show Jalali and Hijri.
3. **Given** the active calendar is Hijri, **When** the user reads
   the notification, **Then** the primary date is Hijri and secondary
   dates show Jalali and Gregorian.
4. **Given** all three calendars are displayed, **When** the user
   compares them, **Then** all three represent the exact same
   real-world day based on device local time.
5. **Given** the notification is shown, **When** the user looks at the
   mini calendar tile, **Then** it shows the month name and day number
   from the active calendar, visually integrated (not a separate card).
6. **Given** the mini tile is displayed, **When** the user observes
   its styling, **Then** it uses the same glass surface language,
   border treatment, and corner radius as the main notification.

---

### User Story 3 — Actions & Behavior (Priority: P1)

As a user, I want three compact integrated action buttons (Today,
New Event, Remind Later) that look like controls belonging to the
notification, not independent floating cards.

**Why this priority**: Actions are the primary interaction mechanism.
They must be usable, visually cohesive, and functionally correct.

**Independent Test**: Tap each action and verify correct behavior:
Today opens today's date, New Event opens add event, Remind Later
snoozes using the same notification ID.

**Acceptance Scenarios**:

1. **Given** the notification is shown, **When** the user sees the
   action buttons, **Then** three actions are visible: Today, New
   Event, Remind Later, styled as compact glass controls.
2. **Given** the user taps Today, **When** the app opens, **Then**
   it navigates to the actual device-local today's date (not the
   currently browsed date).
3. **Given** the user taps New Event, **When** the app opens,
   **Then** the Add Event screen is displayed.
4. **Given** the user taps Remind Later, **When** the snooze period
   elapses, **Then** the same notification ID 1001 is restored
   (no new notification created).
5. **Given** the active locale is Persian, **When** the user sees the
   actions, **Then** the labels are in Persian (امروز، رویداد جدید،
   یادآوری بعداً).
6. **Given** the active locale is English, **When** the user sees the
   actions, **Then** the labels are in English (Today, New Event,
   Remind Later).

---

### User Story 4 — Expanded & Collapsed States (Priority: P2)

As a user, I want both the expanded and collapsed notification states
to be intentionally designed with the same unified visual language.
The collapsed state should show essential information; the expanded
state should show additional detail.

**Why this priority**: Android automatically collapses notifications.
Both states must look polished and intentional.

**Independent Test**: Verify collapsed shows identity + date + minimal
context; expanded shows full date info + secondary dates + message +
actions. Both use the same unified surface.

**Acceptance Scenarios**:

1. **Given** the notification is collapsed, **When** the user views
   it, **Then** it shows app identity, primary date, and mini tile
   without excessive content.
2. **Given** the notification is expanded, **When** the user views
   it, **Then** it shows primary date, secondary dates, daily message,
   and action buttons on one coherent surface.
3. **Given** both states are displayed, **When** the user compares
   them, **Then** both use the same design language with no visual
   disconnect between states.

---

### User Story 5 — Content & Typography (Priority: P2)

As a user, I want the notification to display a daily motivational
message, use correct Vazirmatn typography weights, and respect RTL/LTR
based on the active calendar.

**Why this priority**: Typography and localization complete the premium
feel. Incorrect weights or broken RTL degrade the experience.

**Independent Test**: Verify message is present (not hardcoded),
typography follows weight hierarchy, and layout direction matches
the active calendar type.

**Acceptance Scenarios**:

1. **Given** the notification is shown, **When** the user reads the
   daily message, **Then** it is displayed from the project's string
   resources (not hardcoded).
2. **Given** the active calendar is Jalali, **When** the user views
   the notification, **Then** the layout direction is RTL.
3. **Given** the active calendar is Gregorian, **When** the user views
   the notification, **Then** the layout direction is LTR.
4. **Given** any text is displayed, **When** the user observes
   typography, **Then** SemiBold is used for primary date/month,
   Medium for buttons and metadata, Regular for secondary text.

---

### Edge Cases

- What happens when the device timezone changes while the notification
  is visible? The notification should update to reflect the new timezone.
- What happens when two calendar types produce the same date string?
  Both should still be displayed as separate secondary lines.
- What happens when the daily message string is missing from resources?
  The notification should render without the message area, not crash.
- What happens during a DST transition? The notification should display
  the correct local date without ambiguity.
- What happens when RemoteViews hits its maximum layout complexity?
  The notification should gracefully degrade rather than fail silently.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Notification MUST use ID 1001 and channel
  luma_calendar_daily. This MUST NOT change.
- **FR-002**: Notification MUST be ongoing with auto-cancel disabled.
  Tapping MUST NOT dismiss it.
- **FR-003**: Exactly one daily notification MUST exist at any time.
  All updates MUST replace notification 1001 in place.
- **FR-004**: The notification visual structure MUST be a single
  unified surface. No nested glass cards or visually dominant inner
  containers are permitted.
- **FR-005**: The primary date MUST be based on the device's real
  local timezone via the established device-time API. Selected date
  MUST NOT be used.
- **FR-006**: All three calendar systems (Jalali, Gregorian, Hijri)
  MUST display the same real-world day. The active CalendarType
  determines the primary displayed calendar.
- **FR-007**: The mini calendar tile MUST show the month name and day
  number from the active calendar, visually integrated into the main
  notification composition (not as a separate floating element).
- **FR-008**: Secondary calendar dates MUST show the other two calendar
  systems below the primary date.
- **FR-009**: Three action buttons MUST be present: Today, New Event,
  Remind Later. They MUST be styled as compact glass controls within
  the same notification surface.
- **FR-010**: Today action MUST open the actual device-local current
  date. It MUST NOT open the currently browsed date if different.
- **FR-011**: Remind Later MUST snooze using the same notification
  ID 1001. It MUST NOT create a new notification. The snooze duration
  MUST be user-configurable in the Settings screen (default: 1 hour).
  A new setting row for daily notification snooze duration MUST be
  added to SettingsScreen.
- **FR-012**: The daily message MUST be sourced from the project's
  string resources via the existing localization architecture. It
  MUST NOT be hardcoded in the layout.
- **FR-013**: Layout direction MUST be RTL for Jalali/Hijri and LTR
  for Gregorian, driven by the existing LocalizationManager.
- **FR-014**: Typography MUST follow the Vazirmatn weight hierarchy:
  SemiBold (primary date, month), Medium (buttons, metadata),
  Regular (secondary text).
- **FR-015**: Colors MUST use existing design tokens (GlassSurface*,
  GlassBorder*, AccentPresets). No arbitrary color values permitted.
- **FR-016**: The collapsed state MUST be intentionally designed to
  show identity, primary date, and mini tile without excessive content.
- **FR-017**: The expanded state MUST show primary date, secondary
  dates, daily message, and actions on one coherent surface.
- **FR-018**: All notification updates MUST remain efficient and
  lifecycle-safe. No main thread blocking, no unnecessary services,
  no Activity launching.
- **FR-019**: Event Reminder architecture MUST NOT be modified.
  Event Reminders MUST coexist with the daily notification.
- **FR-020**: The notification MUST update on: app start, foreground
  resume, midnight, date/time/timezone change, reboot, package
  update, calendar type change, and settings change.
- **FR-021**: The notification MUST work responsively across narrow,
  standard, and larger phone widths without text overlap.
- **FR-022**: Accessibility MUST be maintained: readable text sizes,
  sufficient contrast, usable touch targets.
- **FR-023**: No hardcoded dates, no manual RTL string reversal, no
  arbitrary colors, no hardcoded user-facing strings.
- **FR-024**: Existing architecture (LumaNotificationManager,
  DateUtils, CalendarConverter, LocalizationManager, AppStrings)
  MUST be reused. No duplicate systems.

### Key Entities

- **DailyNotification**: The single persistent notification (ID 1001)
  on channel luma_calendar_daily. Shows today's date in all three
  calendar systems with integrated mini tile, daily message, and
  action buttons. Must be ongoing, non-dismissible, and updated
  in place.
- **NotificationLayout**: The RemoteViews composition defining the
  notification's visual structure. Consists of a collapsed layout
  (identity + primary date + tile) and expanded layout (full date
  info + secondary dates + message + actions). Must use a single
  unified surface with no nested cards.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The notification visually reads as ONE unified surface
  with no visible "card inside card" nesting when inspected by a user.
- **SC-002**: The primary date displayed matches the device's actual
  date in the active calendar system in 100% of cases.
- **SC-003**: All three calendar systems show the same real-world day
  with zero off-by-one errors.
- **SC-004**: The notification appears within 2 seconds of the user
  enabling it in settings.
- **SC-005**: Tapping Today opens the actual device-local today's
  date, not the browsed date.
- **SC-006**: Remind Later restores the same notification ID 1001
  after the snooze period with no duplicate notifications.
- **SC-007**: The notification remains visually consistent with the
  Luma Calendar app's Liquid Glass design language.
- **SC-008**: Both collapsed and expanded states render correctly
  without visual artifacts or content overflow.
- **SC-009**: RTL layout renders correctly for Jalali/Hijri with no
  reversed or garbled text.
- **SC-010**: No crashes, no regressions in existing calendar or
  notification functionality.

## Clarifications

### Session 2026-09-16

- Q: How long should the "Remind Later" snooze period last? → A:
  User-configurable in Settings (default: 1 hour). New settings row
  added for snooze duration.

## Assumptions

- The existing LumaNotificationManager, DateUtils, CalendarConverter,
  LocalizationManager, and AppStrings architecture is reused.
- Android RemoteViews is the rendering mechanism for notification
  layouts (XML-based, not Compose).
- The existing notification channel luma_calendar_daily and ID 1001
  are preserved without change.
- Event Reminder notifications continue to operate independently
  on the event_reminders channel.
- The current notification XML layouts (notification_luma_calendar.xml,
  notification_luma_calendar_expanded.xml) will be redesigned, not
  incrementally patched.
- The Vazirmatn font files are bundled and available for notification
  text rendering.
- Dynamic day icon behavior (based on device date, not selected date)
  is preserved.
- The notification must be lightweight and must not start foreground
  services or launch activities for rendering.
