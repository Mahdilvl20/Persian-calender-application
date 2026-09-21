# Feature Specification: Luma Notification Small Icon — Dynamic Persian Day Number

**Feature Branch**: `008-notification-day-icon`

**Created**: 2026-09-17

**Status**: Draft

**Input**: User description: "Update the Daily/Persistent Notification small icon. Replace the current generic bell small icon with a compact Luma Calendar notification icon that displays the current Jalali (Persian) day number, generated dynamically from the real device-local date."

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.

  Assign priorities (P1, P2, P3, etc.) to the user stories.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Recognizable Luma Identity in Notification Icon (Priority: P1)

A user with the Daily Notification enabled sees the persistent notification in the status bar and shade. Instead of a generic bell that could belong to any app, the small icon is a compact Luma Calendar mark: a simplified rounded calendar silhouette that matches the Luma app icon identity. The icon is legible at Android notification small-icon size and works on both light and dark system backgrounds.

**Why this priority**: The notification icon is the most visible recurring brand surface for a calendar app — it is on screen all day. Brand recognition and system compliance are the foundation for the dynamic day-number behavior.

**Independent Test**: Can be fully tested by viewing the Daily Notification's small icon in the status bar and shade and confirming the bell is gone and the icon reads as a calendar in Luma's identity; delivers instant brand recognition.

**Acceptance Scenarios**:

1. **Given** the Daily Notification is active, **When** the user opens the notification shade, **Then** the small icon is a compact Luma calendar icon and the bell icon no longer appears anywhere in the notification.
2. **Given** the notification is shown in the status bar, **When** the system renders it on both light and dark backgrounds, **Then** the icon remains contrast-sufficient and recognizable.
3. **Given** the notification is rendered at standard Android small-icon size, **When** the user views it, **Then** the icon is legible without pixel-clutter or clipping.

---

### User Story 2 - Current Jalali Day Number on the Notification Icon (Priority: P1)

A Persian calendar user glances at the notification icon and can read today's Jalali day number directly on the small icon (e.g., ۲۶ on ۲۶ شهریور). The number reflects the REAL current device-local Jalali day, not the selected/viewed calendar date, an event date, or any other date source.

**Why this priority**: The day number is the core behavioral requirement that makes the icon a live calendar rather than a static logo — it answers "what day is it?" at a glance.

**Independent Test**: Can be fully tested by comparing the number on the notification icon against the device's real Jalali day (e.g., via the app's own primary date display) and confirming they match; delivers a glanceable live day indicator.

**Acceptance Scenarios**:

1. **Given** today's real Jalali date is ۲۶ شهریور ۱۴۰۵, **When** the Daily Notification is displayed, **Then** the small icon shows ۲۶.
2. **Given** the user has navigated the calendar to another date and selected events on other days, **When** the notification icon is rendered, **Then** it still shows the real current Jalali day, unaffected by selection or viewed date.
3. **Given** today's Jalali day is ۹ (single digit), **When** the icon is rendered, **Then** ۹ is displayed fully readable, not clipped or overflowing.

---

### User Story 3 - Automatic Day-Change Refresh Without Relaunch (Priority: P2)

A user leaves the notification active overnight. When the device date changes (at midnight or via a date/timezone change, reboot, or package update), the notification icon shows the new Jalali day number without the user opening the app or restarting anything. The existing Daily Notification is refreshed in place — no second notification is created and the notification ID/channel stay stable.

**Why this priority**: This makes the icon trustworthy as a live date indicator; without it, the icon would lie about today's date after midnight.

**Independent Test**: Can be fully tested by changing the device date across midnight and confirming the notification icon updates to the new Jalali day with no duplicate notification appearing; delivers a self-maintaining day indicator.

**Acceptance Scenarios**:

1. **Given** the Daily Notification is active, **When** the device-local date changes at midnight, **Then** the icon shows the new Jalali day number and the same single notification is updated in place.
2. **Given** the device reboots or the package updates, **When** the Daily Notification is restored, **Then** it uses the compact Luma icon with the current day number.
3. **Given** the device timezone changes, **When** the real device-local date changes as a result, **Then** the icon reflects the new date.

---

### User Story 4 - Localized Digit Rendering on the Daily Notification Icon (Priority: P2)

A user whose active localization is Persian/Jalali sees the day number in Persian digits (۲۶). The number is produced by the app's existing localization/number formatting system rather than hardcoded per-digit strings. The layout adapts between single-digit (۱, ۹) and double-digit (۱۰, ۲۵, ۳۰, ۳۱) days without overflow or clipping.

**Why this priority**: Persian digit rendering is a core expectation for Jalali users and validates the localization pipeline, but it rides on top of the icon mechanics established in P1 stories.

**Independent Test**: Can be fully tested by switching localization/calendar type and observing the digits rendered on the notification icon across single- and double-digit days; delivers native-feeling localization.

**Acceptance Scenarios**:

1. **Given** the active localization is Persian/Jalali, **When** the day number 26 is rendered on the icon, **Then** it appears as ۲۶ using Persian digits.
2. **Given** the active localization is Gregorian/English, **When** the day number 26 is rendered, **Then** it appears as 26 using Latin digits (localized formatting, not Persian digits).
3. **Given** any localization, **When** the Jalali day is ۱۰, ۲۵, ۳۰, or ۳۱ (two digits), **Then** the number stays fully within the icon bounds and remains readable.

---

## Edge Cases

- What happens when the Jalali day is single-digit (e.g., ۱, ۹)? The icon layout adapts so the digit stays centered/legible with no overflow or clipping.
- What happens when the Jalali day is double-digit (e.g., ۱۰, ۳۰, ۳۱)? The icon layout adapts so both digits remain inside the icon bounds and readable at notification size.
- What happens when the device timezone changes while the notification is active? If the change alters the real device-local date, the icon updates to the new Jalali day; if the date is unchanged, the notification need not redraw with a different day.
- What happens when the device reboots or the package updates? The Daily Notification (and its icon) is rebuilt from the current real device-local date using the existing reschedule/refresh lifecycle.
- What happens when the app has no events today or no network? The icon behavior is unchanged — it depends only on the device-local date, not events or connectivity.
- What happens if the notification channel is re-created or system UI caches the icon? The icon source remains stable and deterministic for the same date, so re-rendering shows the same day number until the date changes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST replace the bell small icon on the Daily Notification with a compact Luma Calendar notification icon that visually matches the Luma app icon identity (calendar shape, rounded/simplified silhouette, Luma visual identity).
- **FR-002**: System MUST render the current Jalali day number on the notification small icon.
- **FR-003**: System MUST derive the displayed day number from the real device-local date (device-time authority), not from selected/viewed calendar date, events, or any other date source.
- **FR-004**: System MUST NOT display the month name, Gregorian or Hijri date, or any extra text within the small icon.
- **FR-005**: System MUST NOT include bell, alarm, or clock imagery or unnecessary decoration in the notification icon.
- **FR-006**: The notification icon MUST remain valid as an Android notification small icon (monochrome-safe silhouette compatible with system rendering), not a re-colored full-color launcher icon pasted into the small-icon slot.
- **FR-007**: System MUST format the day number using the existing localization/number formatting system: Persian digits under Persian/Jalali localization, Latin digits under Gregorian/English localization.
- **FR-008**: The icon layout MUST adapt automatically between single-digit and double-digit Jalali days (۱–۳۱) so the number never overflows or becomes clipped.
- **FR-009**: System MUST update the icon's day number automatically when the real device-local date changes, covering midnight/date change, timezone change, device reboot, package update, and the Daily Notification refresh cycle.
- **FR-010**: The Daily Notification MUST keep its existing identity (same notification ID, 1001, and same channel, luma_calendar_daily); the icon update MUST refresh the existing notification in place rather than creating a new notification ID.
- **FR-011**: System MUST NOT reuse the dynamic launcher alias icons (ActivityAliasDayXX), MUST NOT create additional launcher Activities, and MUST NOT relaunch the main activity to update the notification icon.
- **FR-012**: System MUST NOT add another large icon into the Daily Notification body; the notification body layout (System Header → Primary Date → Secondary Dates → Message → Actions) remains unchanged, with the day icon living in the status/header area only.
- **FR-013**: The notification icon MUST keep working across modern Android versions, light/dark system UI, and small render sizes with sufficient contrast and without oversized bitmaps.
- **FR-014**: The bell icon resource usage MUST be fully removed from the Daily Notification; no duplicate icon implementation may be introduced.
- **FR-015**: Event reminder notification architecture (separate per-event reminders) MUST remain unchanged by this feature, including their existing small icon — the new compact Luma calendar icon applies to the Daily Notification only, not to per-event reminders.
- **FR-016**: System MUST NOT introduce a +1/-1 day workaround, UTC-based "today" determination, or hardcoded day values anywhere in this feature.

### Key Entities *(include if feature involves data)*

- **Notification Day Icon**: The compact Luma-branded notification icon artifact carrying the current Jalali day number. Key attributes: brand silhouette, day number (1–31), localized digit form (Persian or Latin), and Android small-icon compatibility.
- **Daily Notification**: The existing persistent daily calendar notification (ID 1001, channel `luma_calendar_daily`). Key attributes: identity stability across refreshes, icon state reflecting the current Jalali day, and unchanged body structure.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The bell icon no longer appears in the Daily Notification; 100% of visible notification renderings show the compact Luma calendar icon.
- **SC-002**: The day number shown on the notification icon matches the real device-local Jalali day in 100% of checks performed on a given day.
- **SC-003**: After a device date change (midnight or timezone change), the icon shows the new Jalali day without user action, verified on every day-change event tested.
- **SC-004**: All Jalali days ۱ through ۳۱ (single- and double-digit) render fully inside the icon bounds with no clipping or overflow.
- **SC-005**: Persian digit rendering matches the active localization (Persian digits for Persian/Jalali, Latin digits for Gregorian/English) in 100% of checks.
- **SC-006**: Only one Daily Notification exists at any time (notification ID 1001 unchanged); zero duplicate notifications are created during icon updates.
- **SC-007**: The notification icon remains legible at Android notification small-icon size on light and dark system UI in user spot-checks (target: recognizable without zooming).

## Clarifications

### Session 2026-09-18

- Q: Should the new compact Luma calendar icon also replace the bell on per-event reminder notifications, or apply only to the persistent Daily Notification? → A: Daily notification only — event reminders keep their current small icon untouched; scope is the Daily Notification (ID 1001) only.

## Assumptions

- The Daily Notification is the persistent daily calendar notification with ID 1001 on channel `luma_calendar_daily`; event reminders are a separate system and are out of scope.
- The day number shown is the Jalali day of the real device-local date regardless of the calendar type the user is currently viewing; per the feature brief, Jalali is the authoritative day number source for this icon.
- Android requires a dedicated notification drawable optimized for small-icon rendering; the full-color launcher icon will not simply be placed into setSmallIcon(). The exact re-use of existing icon assets/generation logic is a design decision for the planning phase.
- The existing DynamicIconManager / icon-generation logic and date authority (DateUtils device-time helpers + CalendarConverter) will be reused where technically appropriate rather than duplicated.
- The existing midnight/reboot/package-update lifecycle receivers already exist for the Daily Notification; this feature rides on that lifecycle rather than inventing a new one.
- No new third-party dependencies are needed for this feature.

## Notes

- Per the project constitution: device-time authority (Principle I) governs the date source; calendar conversion (Principle II) must go through CalendarConverter; localization (Principle IV) must use the existing localization/number formatting system; testable date logic (Principle VII) applies to the day-number derivation.
- The required process steps in the feature brief (inspect launcher icon, inspect current small icon, inspect DynamicIconManager, determine Android-compatible approach, decide reuse vs. dedicated drawable, create plan, implement) are planning/implementation-phase activities; this spec captures the WHAT/WHY.
