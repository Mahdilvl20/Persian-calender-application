# Feature Specification: Daily Notification Unified Surface

**Feature Branch**: `006-notification-unified-surface`

**Created**: 2026-09-17

**Status**: Draft

**Input**: The previous notification refinement (002) did NOT fully solve
the "rectangle inside rectangle" problem. The Android system notification
container still adds visible padding and surface around the custom
RemoteViews. Investigate the Android notification template system to
determine what is controllable, then redesign the notification to
achieve the most unified appearance possible within Android's
constraints.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Unified Notification Surface (Priority: P1)

As a user, I want the daily calendar notification to look like ONE
cohesive Luma Calendar surface. The current notification still shows
a visible Android container around the Luma content, creating a
"card inside a card" appearance.

**Why this priority**: This is the core visual defect that
undermines the premium brand identity of the notification.

**Independent Test**: Pull down the notification shade and visually
inspect whether the Luma content fills the notification surface
naturally or appears as a distinct inner card.

**Acceptance Scenarios**:

1. **Given** the daily notification is active, **When** the user
   views it, **Then** the Luma Calendar content fills the
   notification surface as naturally as possible within Android's
   rendering constraints.
2. **Given** the notification is displayed, **When** the user
   inspects the visual hierarchy, **Then** there is no large
   opaque inner rounded rectangle that clearly separates from
   the outer notification surface.
3. **Given** the notification is shown, **When** the user compares
   it with other notifications, **Then** it looks like a native
   Luma Calendar notification, not a generic notification
   containing an embedded card.
4. **Given** the notification exists, **When** the user checks its
   identity, **Then** it remains Notification ID 1001 on channel
   luma_calendar_daily with ongoing/persistent behavior.

---

### User Story 2 — Date Accuracy & Readability (Priority: P1)

As a user, I want the notification to clearly display today's date
in the active calendar system with secondary dates, and the primary
date must not be truncated.

**Why this priority**: The primary date is the notification's core
purpose. Truncation makes it unreadable.

**Independent Test**: Verify the primary date is fully readable in
both Jalali and Gregorian modes, with secondary dates visible.

**Acceptance Scenarios**:

1. **Given** the active calendar is Jalali, **When** the user reads
   the notification, **Then** the primary Jalali date is fully
   readable without truncation (e.g., "چهارشنبه ۲۶ شهریور ۱۴۰۵").
2. **Given** the active calendar is Gregorian, **When** the user
   reads the notification, **Then** the primary Gregorian date is
   fully readable without truncation (e.g., "Thursday, September 17,
   2026").
3. **Given** all three calendars are displayed, **When** the user
   compares them, **Then** all three represent the same real-world
   day.
4. **Given** the notification is shown, **When** the user looks at
   the mini calendar tile, **Then** it is visually integrated (not
   a separate floating card).

---

### User Story 3 — Actions, Message & Layout (Priority: P2)

As a user, I want compact action buttons, a daily message, and
both collapsed and expanded states that are intentionally designed
and feel like part of the same notification.

**Why this priority**: Actions and message complete the notification
utility. Both states must look polished.

**Independent Test**: Verify actions are compact and integrated,
message is visually secondary, collapsed and expanded states are
both intentionally designed.

**Acceptance Scenarios**:

1. **Given** the notification is expanded, **When** the user sees
   the actions, **Then** Today, New Event, and Remind Later are
   compact glass controls belonging to the notification surface.
2. **Given** the notification is expanded, **When** the user reads
   the daily message, **Then** it is visually secondary to the
   primary date, correctly localized, and properly spaced.
3. **Given** the notification is collapsed, **When** the user views
   it, **Then** it shows primary date and minimal context without
   excessive content or truncation.
4. **Given** both states are displayed, **When** the user compares
   them, **Then** both use the same unified visual language.

---

### Edge Cases

- What happens when the primary date is very long (e.g., Persian
  weekday + full month name + year)? The layout should handle
  wrapping or sizing to avoid truncation.
- What happens on very narrow notification widths (e.g., tablet
  landscape)? The notification should degrade gracefully.
- What happens when the device uses a light notification theme?
  The notification should maintain readability.
- What happens when Android changes notification rendering in a
  future OS version? The design should be resilient to minor
  system changes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The notification MUST investigate and document what
  Android's `DecoratedCustomViewStyle` adds around custom
  RemoteViews (padding, margins, background). The implementation
  MUST work within these constraints, not against them.
- **FR-002**: The RemoteViews root layout MUST NOT apply its own
  large opaque rounded rectangle background that creates a visible
  inner card. The content should blend with the system notification
  surface.
- **FR-003**: The notification visual structure MUST minimize the
  appearance of nested rectangles by reducing inner backgrounds,
  reducing excessive borders, and letting the system surface serve
  as the visual canvas.
- **FR-004**: The primary date MUST be fully readable without
  truncation. If the date is long, the layout MUST accommodate it
  through wrapping, sizing adjustments, or repositioning elements.
- **FR-005**: The mini calendar tile MUST be visually integrated
  into the notification, not appearing as a separate floating card.
- **FR-006**: Secondary calendar dates MUST show the other two
  calendar systems below or beside the primary date.
- **FR-007**: Three action buttons (Today, New Event, Remind Later)
  MUST be present as compact glass controls within the notification
  surface.
- **FR-008**: The daily message MUST be sourced from string resources
  and be visually secondary to the primary date.
- **FR-009**: The collapsed state MUST be intentionally designed
  with primary date and minimal context.
- **FR-010**: The expanded state MUST show full date info, secondary
  dates, message, and actions on one coherent surface.
- **FR-011**: Layout direction MUST be RTL for Jalali/Hijri and LTR
  for Gregorian, driven by the existing LocalizationManager.
- **FR-012**: The notification MUST use existing design tokens
  (GlassSurface*, GlassBorder*, AccentPresets). No arbitrary colors.
- **FR-013**: Typography MUST follow Vazirmatn weight hierarchy.
- **FR-014**: The notification MUST use device-local time via the
  established device-time API. No hardcoded dates.
- **FR-015**: Notification ID MUST remain 1001, channel
  luma_calendar_daily, ongoing, persistent, auto-cancel disabled.
- **FR-016**: Event Reminder architecture MUST NOT be modified.
- **FR-017**: No foreground service, no ActivityAlias, no duplicate
  notification IDs, no duplicate alarms.
- **FR-018**: All calendar conversions MUST use the existing JDN-based
  CalendarConverter pathway.
- **FR-019**: The implementation MUST respect Android notification
  rendering constraints and not use unsupported hacks to remove
  system UI.
- **FR-020**: Existing notification update triggers (app start,
  foreground, midnight, reboot, timezone change, calendar type
  change) MUST remain functional.

### Android Template Constraints

**Application-controlled:**
- RemoteViews content layout
- Typography and text sizes
- Colors and transparency
- Spacing and padding
- Icons and images
- Action buttons and PendingIntents
- Inner background drawables

**System-controlled:**
- Notification outer container shape and padding
- System header (app name, timestamp, expand/collapse)
- App identity display
- System notification margins
- Device-specific notification rendering variations
- Notification channel importance behavior

**Investigation required:** The implementation MUST first determine
the exact visual contribution of `DecoratedCustomViewStyle` to the
nested appearance, then design accordingly.

### Key Entities

- **UnifiedNotificationLayout**: The redesigned RemoteViews
  composition that minimizes nested rectangles. Uses the system
  notification surface as the visual canvas, with content that
  blends naturally into it.

- **NotificationTemplateConstraint**: The boundary between what
  Android controls (outer container, header, margins) and what
  the app controls (content, colors, spacing). The design MUST
  work within this boundary.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The notification no longer visually appears as a
  large card inside another large card when inspected by a user.
- **SC-002**: The Luma Calendar content fills the notification
  surface as naturally as Android's template allows.
- **SC-003**: The primary date is fully readable without truncation
  in both Jalali and Gregorian modes.
- **SC-004**: All three calendar systems show the same real-world
  day with zero off-by-one errors.
- **SC-005**: The mini calendar tile is visually integrated, not
  a separate floating element.
- **SC-006**: Actions are compact and visually belong to the
  notification surface.
- **SC-007**: Both collapsed and expanded states are intentionally
  designed and visually consistent.
- **SC-008**: System header shows "Luma Calendar" only once (no
  duplication via setSubText or custom text).
- **SC-009**: The notification preserves the Liquid Glass design
  language (dark feel, translucent elements, subtle borders).
- **SC-010**: No crashes, no regressions in notification or
  calendar functionality.

## Assumptions

- The existing LumaNotificationManager architecture is reused.
- Android's `DecoratedCustomViewStyle` adds system-controlled
  padding and container styling around custom RemoteViews. The
  implementation must work within this, not fight it.
- The root cause of the remaining nested appearance is the
  combination of: (1) DecoratedCustomViewStyle's system padding,
  and (2) any remaining background/stroke on the RemoteViews root.
- The investigation step is critical — the exact fix depends on
  what Android actually renders vs. what the app renders.
- The notification XML layouts will be redesigned, not
  incrementally patched.
- Event Reminder notifications continue independently.
