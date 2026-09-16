# Feature Specification: Time Picker RTL/LTR Fix & UI Refinement

**Feature Branch**: `004-fix-time-picker`

**Created**: 2026-09-17

**Status**: Draft

**Input**: Fix the RTL layout bug in the Time Picker when Persian/Jalali
is active, and refine the time picker UI for better spacing, alignment,
and visual hierarchy.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — RTL/LTR Correctness (Priority: P1)

As a Persian user, I want the Time Picker to display correctly in RTL
mode: hour on the right, minute on the left, separator between them,
all with proper layout direction driven by the system. As an English
user, I want the same Time Picker to work correctly in LTR mode.

**Why this priority**: RTL broken layout is a functional bug that
confuses Persian users. It must be fixed first.

**Independent Test**: Switch between Jalali and Gregorian calendar types,
open the Time Picker in each, and verify the hour/minute fields are
correctly positioned for the active layout direction.

**Acceptance Scenarios**:

1. **Given** the active calendar is Jalali (RTL), **When** the user
   opens the Time Picker, **Then** the layout is RTL: hour on the
   right, minute on the left, separator between them, all flowing
   correctly without string reversal or layout hacks.
2. **Given** the active calendar is Gregorian (LTR), **When** the user
   opens the Time Picker, **Then** the layout is LTR: hour on the
   left, minute on the right, separator between them.
3. **Given** the user switches calendar types while the Time Picker
   is open, **When** the layout direction changes, **Then** the
   Time Picker repositions fields to match the new direction.
4. **Given** either layout direction, **When** the user reads the
   time values, **Then** the numbers are not reversed, mirrored,
   or visually distorted.

---

### User Story 2 — Time Field Layout & Interaction (Priority: P1)

As a user, I want the Time Picker to have clear visual hierarchy
with well-spaced hour and minute fields, a visible separator, and
intuitive interaction for entering or selecting time values.

**Why this priority**: The time fields are the core interaction.
Poor spacing and hierarchy make the picker hard to use.

**Independent Test**: Open the Time Picker, enter a time using both
input and scroll/select methods, verify fields are clearly separated
and easy to tap.

**Acceptance Scenarios**:

1. **Given** the Time Picker is open, **When** the user views the
   layout, **Then** the hour field, separator, and minute field are
   clearly separated with balanced spacing.
2. **Given** the Time Picker is open, **When** the user taps the
   hour field, **Then** it gains focus with a visible focus state
   (border highlight or background change).
3. **Given** the Time Picker is open, **When** the user taps the
   minute field, **Then** it gains focus and the hour field loses
   focus.
4. **Given** the user enters "25" in the hour field, **When** the
   value exceeds 23, **Then** the field shows an error state and
   the value is not accepted until corrected.
5. **Given** the user enters "70" in the minute field, **When** the
   value exceeds 59, **Then** the field shows an error state and
   the value is not accepted until corrected.

---

### User Story 3 — AM/PM or 24-Hour Behavior (Priority: P1)

As a user, I want the Time Picker to correctly handle 24-hour time
format, which is the standard in Persian/Iranian culture. If AM/PM
is shown, it must be correctly positioned and localized.

**Why this priority**: Time format correctness is essential for
the picker to be usable.

**Independent Test**: Enter a time in 24-hour format, verify it
is correctly interpreted and displayed.

**Acceptance Scenarios**:

1. **Given** the Time Picker is in 24-hour mode, **When** the user
   enters hour "14" and minute "30", **Then** the time is accepted
   as 14:30.
2. **Given** the Time Picker shows AM/PM, **When** the active
   locale is Persian, **Then** the labels are localized (ق.ظ / ب.ظ).
3. **Given** the Time Picker shows AM/PM, **When** the layout is
   RTL, **Then** AM/PM is correctly positioned relative to the
   time fields.

---

### User Story 4 — Visual Design & Premium Feel (Priority: P2)

As a user, I want the Time Picker to match the Luma Calendar premium
visual design: Liquid Glass surfaces, correct Vazirmatn typography,
consistent color tokens, and a clean, modern appearance.

**Why this priority**: Visual consistency reinforces the brand
identity and user trust.

**Independent Test**: Compare the Time Picker's visual style with
other Luma Calendar components (event sheet, settings) and verify
consistency.

**Acceptance Scenarios**:

1. **Given** the Time Picker is open, **When** the user observes
   the design, **Then** it uses the Liquid Glass design language
   (dark glass surface, translucent backgrounds, subtle borders).
2. **Given** the Time Picker is open, **When** the user reads the
   time values, **Then** Vazirmatn font is used with correct weight
   hierarchy.
3. **Given** the Time Picker is open, **When** the user observes
   colors, **Then** they come from the centralized design tokens
   (GlassSurface*, Accent*, Text*), not arbitrary values.

---

### User Story 5 — Validation & Error States (Priority: P2)

As a user, I want clear visual feedback when I enter an invalid time
value, so I can correct it without confusion.

**Why this priority**: Error feedback prevents user frustration and
invalid data submission.

**Independent Test**: Enter invalid values (hour > 23, minute > 59,
non-numeric) and verify appropriate error states appear.

**Acceptance Scenarios**:

1. **Given** the user enters an hour value greater than 23, **When**
   the field loses focus, **Then** an error indicator appears (red
   border or error text).
2. **Given** the user enters a minute value greater than 59, **When**
   the field loses focus, **Then** an error indicator appears.
3. **Given** the user enters non-numeric text, **When** the field
   processes input, **Then** the input is rejected or sanitized
   without crash.
4. **Given** the Time Picker has validation errors, **When** the user
   taps the confirm/save button, **Then** the errors are highlighted
   and the time is not saved until corrected.

---

### Edge Cases

- What happens when the user rapidly switches between hour and minute
  fields? Focus should transition cleanly without visual glitches.
- What happens when the device locale changes while the Time Picker
  is open? The picker should adapt to the new locale.
- What happens when the Time Picker is opened with a pre-filled time
  of "00:00"? The fields should display correctly in both RTL and LTR.
- What happens when the user scrolls the hour wheel past 23? It
  should wrap or stop, not show invalid values.
- What happens on very narrow screens? The time fields should not
  overlap or clip.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Time Picker MUST use the active LayoutDirection
  (RTL for Jalali/Hijri, LTR for Gregorian) to position time fields.
  RTL: hour right, minute left. LTR: hour left, minute right.
- **FR-002**: Layout direction MUST be driven by the system's
  LayoutDirection via CompositionLocalProvider, NOT by manual string
  reversal or layout hacks.
- **FR-003**: The hour field MUST accept values 0–23 in 24-hour mode.
  Values outside this range MUST show an error state.
- **FR-004**: The minute field MUST accept values 0–59. Values outside
  this range MUST show an error state.
- **FR-005**: The separator between hour and minute MUST be visually
  clear and correctly positioned for both RTL and LTR layouts.
- **FR-006**: Focus states MUST be visible when a time field is active
  (border highlight, background change, or equivalent).
- **FR-007**: Touch targets MUST be at least 48dp for accessibility.
- **FR-008**: The Time Picker MUST use the Luma Calendar Liquid Glass
  design language (dark glass surface, translucent backgrounds).
- **FR-009**: Typography MUST use Vazirmatn with correct weight
  hierarchy: SemiBold for time values, Medium for labels/buttons.
- **FR-010**: Colors MUST use centralized design tokens. No arbitrary
  color values permitted.
- **FR-011**: User-facing strings MUST come from AppStrings via
  LocalizationManager. No hardcoded strings.
- **FR-012**: AM/PM labels MUST be localized (ق.ظ / ب.ظ for Persian,
  AM/PM for English) when 12-hour mode is active.
- **FR-013**: The Time Picker MUST NOT modify calendar, event,
  notification, or date-conversion functionality.
- **FR-014**: The Time Picker MUST work responsively across narrow,
  standard, and larger phone widths without text overlap.
- **FR-015**: Non-numeric input MUST be rejected or sanitized without
  crash.
- **FR-016**: The Time Picker MUST correctly handle pre-filled times
  (e.g., "00:00", "23:59") in both RTL and LTR modes.

### Key Entities

- **TimePickerLayout**: The visual composition of hour field,
  separator, and minute field. Must adapt to LayoutDirection.
  RTL: [minute] [separator] [hour]. LTR: [hour] [separator] [minute].
- **TimeField**: Individual hour or minute input field with value,
  validation state (valid/error), and focus state.
- **TimeSeparator**: Visual element between hour and minute (e.g.,
  ":" character). Must be correctly positioned for both directions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In RTL mode, hour is on the right and minute is on
  the left in 100% of cases.
- **SC-002**: In LTR mode, hour is on the left and minute is on
  the right in 100% of cases.
- **SC-003**: Layout direction switch (RTL↔LTR) repositions fields
  within 1 frame (no visual flicker).
- **SC-004**: Touch targets are at least 48dp, verified by automated
  accessibility check.
- **SC-005**: Invalid values (hour > 23, minute > 59) show error
  state within 200ms of field losing focus.
- **SC-006**: The Time Picker visually matches the Luma Calendar
  design language when compared side-by-side with other components.
- **SC-007**: Zero crashes from any input (numeric, non-numeric,
  empty, extreme values).
- **SC-008**: Build succeeds and existing tests pass with no regressions.

## Assumptions

- The existing TimePickerDialog or custom Time Picker composable is
  the target for modification.
- The active CalendarType (from NotificationPreferences or
  LocalizationManager) drives the layout direction.
- 24-hour time format is the default for Persian/Iranian locale.
- The existing TimeValidator utility handles time validation and
  can be reused for error state logic.
- Vazirmatn font files are bundled and available for the picker.
- The existing GlassComponents design tokens are reused for
  the picker's visual surface.
