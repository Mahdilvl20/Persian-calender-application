# Feature Specification: Remove Redundant Notification Header

**Feature Branch**: `007-notification-remove-header`

**Created**: 2026-09-17

**Status**: Draft

**Input**: The Android system notification header already shows
"Luma Calendar · now". The custom RemoteViews content also has an
internal "Luma Calendar" header with icon, creating a redundant
double header. Remove the internal header so the notification
content starts directly with calendar information.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Single App Identity (Priority: P1)

As a user, I want the notification to show "Luma Calendar" only
once (in the system header), not duplicated inside the notification
content. The content should start with the calendar date.

**Why this priority**: Duplicate headers look unprofessional and
waste vertical space that should display the date.

**Independent Test**: Pull down notification shade, verify
"Luma Calendar" appears only once in the system header, and the
notification body starts with the primary date.

**Acceptance Scenarios**:

1. **Given** the daily notification is active, **When** the user
   views the notification, **Then** "Luma Calendar" appears only
   in the Android system header (not inside the RemoteViews content).
2. **Given** the notification is displayed, **When** the user reads
   the body, **Then** the first element is the primary date, not
   an app header.
3. **Given** the notification is shown, **When** the user inspects
   the content, **Then** there is no second app icon or logo inside
   the RemoteViews.

---

### User Story 2 — Date Dominance (Priority: P1)

As a user, I want the primary date to be visually dominant in the
notification. After removing the redundant header, the date should
have more vertical space and be clearly readable.

**Why this priority**: The date is the notification's core purpose.
It must be the most prominent element.

**Independent Test**: Verify the primary date is larger and more
prominent after the header removal.

**Acceptance Scenarios**:

1. **Given** the redundant header is removed, **When** the user
   views the notification, **Then** the primary date is visually
   dominant and clearly readable.
2. **Given** the notification is displayed, **When** the user reads
   the primary date, **Then** it is not truncated.
3. **Given** the notification is displayed, **When** the user
   observes the layout, **Then** spacing is balanced and the
   content breathes.

---

### Edge Cases

- What happens when the notification is collapsed? The primary
  date should still be visible and dominant.
- What happens on very narrow notification widths? The date
  should not overflow or clip.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The internal "Luma Calendar" header (app icon +
  app name text) MUST be removed from both notification XML layouts.
- **FR-002**: The Android system notification header (showing
  "Luma Calendar · now") MUST remain intact and unchanged.
- **FR-003**: The notification content MUST start directly with
  the primary date after the system header.
- **FR-004**: No second app icon or logo MUST appear inside the
  RemoteViews content.
- **FR-005**: The primary date MUST be visually dominant with
  appropriate font size and spacing.
- **FR-006**: Secondary dates, daily message, mini calendar tile,
  and actions MUST remain present and correctly positioned.
- **FR-007**: Notification ID MUST remain 1001, channel
  luma_calendar_daily, ongoing, persistent.
- **FR-008**: Event Reminder system MUST NOT be modified.
- **FR-009**: Calendar/date logic MUST NOT be modified.
- **FR-010**: No unrelated code MUST be changed.
- **FR-011**: Liquid Glass design language MUST be preserved.
- **FR-012**: RTL layout MUST work correctly for Jalali/Hijri.

### Key Entities

- **NotificationContent**: The RemoteViews body after removing
  the internal header. First element is the primary date.
  Hierarchy: Primary date → Secondary dates → Daily message →
  Mini tile → Actions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: "Luma Calendar" text appears exactly once in the
  notification (in the system header only).
- **SC-002**: No app icon or logo exists inside the RemoteViews
  content area.
- **SC-003**: The primary date is the first and most prominent
  element in the notification body.
- **SC-004**: The primary date is fully readable without truncation.
- **SC-005**: All three calendar dates (Jalali, Gregorian, Hijri)
  remain present and synchronized.
- **SC-006**: Notification ID 1001 and channel luma_calendar_daily
  are unchanged.
- **SC-007**: No regression in notification functionality.
- **SC-008**: Build succeeds and existing tests pass.

## Assumptions

- The Android system notification header ("Luma Calendar · now")
  is rendered by the system and is NOT part of our RemoteViews.
- The internal header in the RemoteViews is a separate LinearLayout
  with an ImageView (app icon) and TextView ("Luma Calendar").
- Removing the internal header does not affect notification
  identity, channel, or ongoing behavior.
- The `.setSmallIcon()` on the builder is separate from the
  RemoteViews and MUST NOT be removed.
