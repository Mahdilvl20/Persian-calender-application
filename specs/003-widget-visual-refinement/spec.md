# Feature Specification: Widget Visual Refinement

**Feature Branch**: `003-widget-visual-refinement`

**Created**: 2026-09-16

**Status**: Draft

**Input**: Improve the home screen widget's visual layout and date
presentation. The widget functionality is already working correctly.
Only spacing, hierarchy, and typography need refinement.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Spacious Widget Layout (Priority: P1)

As a user, I want the home screen widget to feel clean, calm, and
premium with clear visual hierarchy between the month name, day number,
and secondary calendar information. The current layout feels crowded
and compressed.

**Why this priority**: The widget is the app's most visible surface
on the home screen. A cramped layout undermines the premium brand
identity.

**Independent Test**: Add the widget to the home screen and visually
verify that the month, day number, and secondary dates are clearly
separated with intentional whitespace.

**Acceptance Scenarios**:

1. **Given** the widget is placed on the home screen, **When** the
   user views it, **Then** the Persian month name is displayed at
   the top, clearly separated from the large day number below it.
2. **Given** the widget is displayed, **When** the user observes the
   layout, **Then** the main day number is the dominant visual element
   — large, centered, and vertically separated from both the month
   above and secondary dates below.
3. **Given** the widget is displayed, **When** the user looks at the
   bottom area, **Then** the secondary calendar values are visually
   independent with balanced horizontal spacing, not cramped together.
4. **Given** the widget is displayed, **When** the user observes the
   overall composition, **Then** the design feels spacious with
   intentional whitespace between all visual levels.

---

### User Story 2 — Date Accuracy & Calendar Consistency (Priority: P1)

As a user, I want the widget to always display the correct date from
the real device-local time, with the Persian month name, Jalali day
number, and secondary calendar information all representing the same
real-world day.

**Why this priority**: Incorrect dates on the home screen widget would
be immediately visible and damaging to user trust.

**Independent Test**: Compare the widget's displayed date with the
device's actual date in all three calendar systems.

**Acceptance Scenarios**:

1. **Given** the device date is 2026-09-16, **When** the user views
   the widget, **Then** the Persian month shows "شهریور", the day
   shows "۲۵" (Jalali), and secondary values show the Gregorian and
   Hijri equivalents.
2. **Given** the widget is displayed, **When** midnight passes,
   **Then** the widget automatically updates to show the new date
   without manual refresh.
3. **Given** the widget is displayed, **When** the device timezone
   changes, **Then** the widget updates to reflect the correct local
   date in the new timezone.
4. **Given** the widget shows all three calendar values, **When** the
   user compares them, **Then** all three represent the exact same
   real-world day.

---

### User Story 3 — Typography & Visual Identity (Priority: P2)

As a user, I want the widget to use the Vazirmatn font with correct
weight hierarchy and preserve the existing Luma Calendar visual style.

**Why this priority**: Typography and visual consistency complete the
premium feel and reinforce brand identity.

**Independent Test**: Verify font weights follow the hierarchy (month
= Medium, day = SemiBold/Bold, secondary = Regular/Medium) and the
existing visual style is preserved.

**Acceptance Scenarios**:

1. **Given** the widget is displayed, **When** the user observes the
   month name, **Then** it uses Medium weight and is visually lighter
   than the main day number.
2. **Given** the widget is displayed, **When** the user observes the
   main day number, **Then** it uses SemiBold or Bold weight and is
   the most prominent element.
3. **Given** the widget is displayed, **When** the user observes the
   secondary calendar values, **Then** they use Regular or Medium
   weight and are visually subordinate to the main day.
4. **Given** the widget is displayed, **When** the user compares it
   with the app's visual language, **Then** the widget maintains the
   existing Luma Calendar design identity.

---

### Edge Cases

- What happens when the widget is placed on a light-colored wallpaper?
  The widget should maintain readability with its existing dark surface.
- What happens when the device is set to English/Gregorian as primary?
  The widget should still show the Persian month name as specified
  (the widget is always Jalali-focused per the design requirement).
- What happens on very small home screens? The widget should adapt its
  spacing without clipping or overlapping text.
- What happens when the Jalali month name is long (e.g.,
  "دیسابر")? The text should fit without truncation or overflow.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The widget MUST display the current Jalali/Persian month
  name at the top, sourced from the existing calendar infrastructure.
- **FR-002**: The widget MUST display the current Jalali day number
  as the primary large centered element.
- **FR-003**: The widget MUST display secondary calendar information
  (Gregorian and/or Hijri day numbers) at the bottom with improved
  spacing.
- **FR-004**: The widget MUST use the real device-local date via the
  established device-time API. Selected date MUST NOT be used.
- **FR-005**: The widget MUST automatically update when the device date
  changes (midnight, timezone change).
- **FR-006**: Vertical spacing MUST clearly separate three visual
  levels: month name, main day number, and secondary dates.
- **FR-007**: The main day number MUST be the dominant visual element
  — larger and more prominent than both the month and secondary values.
- **FR-008**: Secondary calendar values MUST have balanced horizontal
  spacing so they appear as independent values, not a combined string.
- **FR-009**: Typography MUST follow Vazirmatn weight hierarchy:
  Medium (month), SemiBold/Bold (main day), Regular/Medium (secondary).
- **FR-010**: All elements MUST be horizontally centered within the
  widget except secondary values which are balanced around center.
- **FR-011**: The widget MUST preserve the existing visual identity
  (dark surface, color scheme, overall style).
- **FR-012**: RTL layout MUST be correctly applied for the Persian
  month name and any RTL text content.
- **FR-013**: No hardcoded dates, month names, or calendar values
  permitted. All values must come from the calendar conversion system.
- **FR-014**: Existing widget update mechanism (WidgetProvider,
  DynamicIconManager) MUST NOT be broken.
- **FR-015**: The Daily Notification and Event Reminder systems MUST
  NOT be affected by widget changes.
- **FR-016**: All calendar conversions MUST use the existing JDN-based
  CalendarConverter pathway. No duplicate conversion logic.

### Key Entities

- **WidgetLayout**: The RemoteViews composition defining the widget's
  visual structure. Contains three visual levels: month name (top),
  main day number (center), secondary calendar values (bottom). Must
  use intentional whitespace and clear hierarchy.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The widget displays the correct Persian month name at
  the top in 100% of cases matching the device date.
- **SC-002**: The main day number is visually dominant — at least
  1.5x larger than the month name text.
- **SC-003**: Vertical spacing between the three visual levels
  (month, day, secondary) is clearly improved compared to the current
  layout.
- **SC-004**: Secondary calendar values are horizontally balanced and
  visually independent (not appearing as a combined string).
- **SC-005**: The widget automatically updates within 1 second of a
  date change (midnight, timezone).
- **SC-006**: The widget preserves the existing Luma Calendar visual
  identity with no jarring style changes.
- **SC-007**: RTL layout renders correctly for the Persian month name
  with no reversed or garbled text.
- **SC-008**: Build succeeds and existing tests pass with no regressions.
- **SC-009**: Daily Notification and Event Reminder functionality
  remain unaffected.

## Assumptions

- The existing widget architecture (WidgetProvider, widget XML layout,
  DynamicIconManager) is reused.
- The widget is always Jalali-focused per the user's design
  requirement (Persian month name at top).
- The existing Vazirmatn font files are bundled and available for
  widget text rendering.
- RemoteViews is the rendering mechanism for the widget layout
  (XML-based, not Compose).
- The existing color tokens and dark surface style are preserved.
- Widget resize behavior is handled by the existing AppWidgetProvider
  configuration.
