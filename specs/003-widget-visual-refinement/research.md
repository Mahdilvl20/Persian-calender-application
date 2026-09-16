# Research: Widget Visual Refinement

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Current Widget Structure

The widget has two sections:
- **Header strip** (`widget_header_bg`): gradient indigo→blue, contains
  month name (12sp, bold, white)
- **Main content** (`layout_weight=1`, centered): day number (40sp, bold),
  weekday (12sp, normal), event text (10sp, normal)

### Problem: Month Shows Gregorian, Not Persian

The current WidgetProvider extracts the month from Java's
`LocalDate.month.getDisplayName()` which returns the device locale's
month name (e.g., "JANUARY" or "September" in English, "ژانویه" in
Persian locale). This is NOT the Jalali month name.

The user wants the Jalali/Persian month name (e.g., "شهریور") at the top.

### Problem: No Secondary Calendar Numbers

The current widget shows weekday name and event text below the day
number, NOT secondary calendar day numbers (Gregorian/Hijri). The
user's design shows "۳" and "۱۶" as secondary values at the bottom.

### Problem: Insufficient Spacing

Current spacing between elements:
- Day number to weekday: 2dp
- Weekday to event text: 4dp

This is too compressed for the three-level hierarchy the user wants.

## Decisions

### Decision 1: Replace Month Source with Jalali Month

**Decision**: Change the widget month text from device-locale Gregorian
month to Jalali month name via CalendarConverter.

**Rationale**: The widget is Jalali-focused per the user's design
requirement. The month name at the top MUST be the Persian month.

**Implementation**: Use CalendarConverter to convert the device date
to Jalali, then extract the month name. The existing
`CalendarConverter.getMonthName()` function can provide this.

### Decision 2: Replace Weekday/Event with Secondary Calendar Days

**Decision**: Replace the current weekday text and event text with
two secondary calendar day numbers (Gregorian and Hijri).

**Rationale**: The user's design explicitly shows secondary calendar
values at the bottom, not weekday/event information.

**Implementation**: Convert the device date to both Gregorian and
Hijri, extract day numbers, format with Persian digits via
`CalendarConverter.toPersianDigits()`.

### Decision 3: Increase Vertical Spacing

**Decision**: Increase spacing between the three visual levels:
- Month to day number: ~8-12dp
- Day number to secondary dates: ~8-12dp
- Widget edges: maintain current padding or slightly increase

**Rationale**: The current 2dp/4dp spacing creates the crowded
appearance the user wants to fix.

### Decision 4: Typography Weight Adjustment

**Decision**: Change month text from Bold to Medium weight per the
spec's typography hierarchy. Keep day number as Bold. Secondary
values use Regular.

**Rationale**: The spec requires Medium for month, SemiBold/Bold for
day, Regular for secondary. Currently month is Bold which competes
with the day number.

### Decision 5: RTL for Persian Month Name

**Decision**: Add `android:layoutDirection="locale"` to the widget
root so the Persian month name renders correctly in RTL context.

**Rationale**: The widget currently has no explicit RTL handling.
Since the widget is Jalali-focused, RTL should be applied.

## Alternatives Considered

- Keep Gregorian month + add Jalali below: Rejected because the user
  explicitly wants Persian month at top.
- Keep weekday text: Rejected because the user's design replaces it
  with secondary calendar numbers.
- Add more text elements: Rejected because the user wants cleaner,
  less crowded design with fewer competing elements.
