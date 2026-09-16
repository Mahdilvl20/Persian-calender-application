# Feature Specification: Time Picker Hour/Minute Ordering Fix

**Feature Branch**: `005-time-picker-ordering-fix`

**Created**: 2026-09-17

**Status**: Draft

**Input**: Fix the Time Picker so that Hour → Separator → Minute
ordering is always stable from left to right, regardless of the
surrounding RTL layout direction. Isolate the time-control row from
global RTL while keeping the rest of the Persian UI RTL.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Stable Time Ordering in All Modes (Priority: P1)

As a Persian user, I want the Time Picker to always show
[Hour] : [Minute] from left to right, even when the rest of the
app is in RTL. The time fields must not be reversed by the layout
direction.

**Why this priority**: This is a functional bug — reversed time
fields confuse users and can lead to entering the wrong time.

**Independent Test**: Open Time Picker in Jalali mode, verify
hour is on the left, minute is on the right, separator between
them. Switch to Gregorian, verify same ordering.

**Acceptance Scenarios**:

1. **Given** the active calendar is Jalali (RTL), **When** the user
   opens the Time Picker, **Then** the time-control row displays
   Hour → Separator → Minute from left to right.
2. **Given** the active calendar is Gregorian (LTR), **When** the
   user opens the Time Picker, **Then** the time-control row
   displays Hour → Separator → Minute from left to right.
3. **Given** the Time Picker is open in Jalali mode, **When** the
   user taps the hour plus button, **Then** the hour value changes
   (not the minute).
4. **Given** the Time Picker is open in Jalali mode, **When** the
   user taps the minute plus button, **Then** the minute value
   changes (not the hour).
5. **Given** the Time Picker is open, **When** the user observes
   the separator, **Then** it is positioned between Hour and Minute
   with balanced spacing on both sides.

---

### User Story 2 — Surrounding RTL Preserved (Priority: P1)

As a Persian user, I want the Time Picker's header, labels, buttons,
and surrounding app to remain in RTL while only the time-control
row is forced to LTR ordering.

**Why this priority**: Forcing the entire dialog to LTR would break
the Persian experience. Only the numeric time fields need isolation.

**Independent Test**: Open Time Picker in Jalali mode, verify
header text flows RTL, buttons are in RTL positions, but time
fields are Hour → Minute from left to right.

**Acceptance Scenarios**:

1. **Given** the active calendar is Jalali, **When** the Time Picker
   opens, **Then** the header (title, toggle, icon) flows RTL.
2. **Given** the Time Picker is open in Jalali mode, **When** the
   user looks at the Cancel/Confirm buttons, **Then** they are in
   RTL positions (Confirm on left, Cancel on right).
3. **Given** the Time Picker is open in Jalali mode, **When** the
   user looks at the time-control row, **Then** it displays
   Hour → Separator → Minute from left to right (isolated from RTL).

---

### User Story 3 — Visual Refinement (Priority: P2)

As a user, I want the Time Picker to have balanced spacing, clear
visual hierarchy between Hour and Minute, and a polished premium
appearance consistent with the Liquid Glass design language.

**Why this priority**: Visual refinement improves usability and
reinforces the premium brand identity.

**Independent Test**: Compare the Time Picker with other Luma
Calendar components and verify visual consistency and improved
spacing.

**Acceptance Scenarios**:

1. **Given** the Time Picker is open, **When** the user observes
   the layout, **Then** spacing between Hour and Minute fields is
   balanced and the separator is visually centered between them.
2. **Given** the Time Picker is open, **When** the user interacts
   with a time field, **Then** there is a visible active/focused
   state on the field being adjusted.
3. **Given** the Time Picker is open, **When** the user taps
   stepper buttons, **Then** touch targets are at least 48dp and
   easy to tap accurately.
4. **Given** the Time Picker is open, **When** the user observes
   the overall design, **Then** it uses Liquid Glass design tokens
   with no arbitrary colors or hardcoded strings.

---

### Edge Cases

- What happens when the user rapidly taps between hour and minute
  steppers? The active field highlight should switch cleanly.
- What happens when the Time Picker is opened with a pre-filled
  time of "00:00"? The fields should display correctly with
  Hour → Minute ordering.
- What happens when the user switches calendar type while the
  Time Picker is open? The time-control row should remain
  Hour → Minute regardless.
- What happens in 12H mode with AM/PM? AM/PM should be correctly
  positioned relative to the LTR time-control row.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The time-control Row MUST always display
  Hour → Separator → Minute from left to right, regardless of
  the surrounding LayoutDirection.
- **FR-002**: The time-control Row MUST be isolated from the
  global RTL direction using an explicit LayoutDirection override
  (e.g., LTR forced on the time-control Row only).
- **FR-003**: The surrounding dialog content (header, labels,
  buttons, AM/PM) MUST remain in the correct RTL/LTR direction
  based on the active calendar type.
- **FR-004**: Hour plus/minus controls MUST modify the hour value.
  Minute plus/minus controls MUST modify the minute value. RTL
  mirroring MUST NOT swap these associations.
- **FR-005**: The separator MUST always appear between Hour and
  Minute with balanced spacing and proper vertical alignment.
- **FR-006**: Active/focused field state MUST be visible when a
  time field is being interacted with.
- **FR-007**: Touch targets for stepper buttons MUST be at least
  48dp.
- **FR-008**: The Time Picker MUST use the Liquid Glass design
  language with existing design tokens.
- **FR-009**: Typography MUST use Vazirmatn with correct weight
  hierarchy.
- **FR-010**: No hardcoded colors, strings, or manual string
  reversal permitted.
- **FR-011**: The time model (validation, parsing, canonical
  formatting) MUST NOT be modified.
- **FR-012**: Event scheduling, reminder calculation, and
  AlarmManager MUST NOT be affected.
- **FR-013**: No unrelated application UI MUST be changed.
- **FR-014**: The fix MUST NOT cause regression in Gregorian/LTR
  mode.
- **FR-015**: The Time Picker MUST work correctly in both 24H
  and 12H modes with the stable Hour → Minute ordering.
- **FR-016**: Persian digit display MUST work correctly within
  the LTR-isolated time-control row.

### Key Entities

- **TimeControlRow**: The horizontal composition containing
  Hour control, Separator, and Minute control. MUST be forced
  to LTR layout direction regardless of surrounding RTL.
  Source order: [Hour] [Separator] [Minute]. Always renders
  left-to-right.

- **TimeField**: Individual Hour or Minute control containing
  Plus button, Value display, and Minus button arranged
  vertically. Each field is associated with its correct time
  component (hour or minute).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In Jalali/RTL mode, the time-control row displays
  Hour → Separator → Minute from left to right in 100% of cases.
- **SC-002**: In Gregorian/LTR mode, the time-control row displays
  Hour → Separator → Minute from left to right in 100% of cases.
- **SC-003**: Hour plus/minus controls modify hour values (never
  minute) in both RTL and LTR modes.
- **SC-004**: Minute plus/minus controls modify minute values
  (never hour) in both RTL and LTR modes.
- **SC-005**: The separator remains between Hour and Minute with
  balanced spacing in both modes.
- **SC-006**: Touch targets are at least 48dp for all interactive
  elements.
- **SC-007**: No string reversal or string manipulation is used
  to fix the ordering.
- **SC-008**: No regression in Gregorian/LTR mode — existing
  behavior preserved.
- **SC-009**: Build succeeds and existing tests pass.
- **SC-010**: The Time Picker remains visually consistent with
  the Liquid Glass design language.

## Assumptions

- The existing `LiquidGlassTimePickerDialog.kt` is the target
  for modification.
- The fix involves isolating the time-control Row with
  `CompositionLocalProvider(LocalLayoutDirection provides
  LayoutDirection.Ltr)` or equivalent.
- The surrounding dialog retains the calendar-type-driven
  LayoutDirection.
- The existing TimeValidator and event scheduling architecture
  are reused without modification.
- Vazirmatn font and centralized color tokens are reused.
