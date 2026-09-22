# Feature Specification: Fix UI Audit Issues

**Feature Branch**: `009-fix-ui-audit-issues`

**Created**: 2026-09-22

**Status**: Draft

**Input**: Fix six confirmed UI functionality issues found during the Luma
Calendar UI audit: non-functional category visibility toggles, unpersisted
appearance settings, an unused loading state, missing confirmation on
destructive actions, silent empty-title substitution, and a snooze
description/options mismatch.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Category Visibility Actually Filters Events (Priority: P1)

As a user, when I turn off a category (Personal, Work, or Holidays) in
Settings, events belonging to that category disappear from the calendar; when
I turn it back on, they reappear — without restarting the app.

**Why this priority**: This is the clearest functional defect — a prominent
Settings control that currently does nothing.

**Independent Test**: Create events tagged Personal, Work, and Holidays. Toggle
each category off/on in Settings and confirm the calendar view reflects the
change immediately.

**Acceptance Scenarios**:

1. **Given** events exist in all three categories, **When** the user turns off
   "Work", **Then** Work events are hidden from the calendar grid, week, and day
   views while Personal and Holidays events remain visible.
2. **Given** a category was turned off, **When** the user turns it back on,
   **Then** its events reappear immediately without an app restart.
3. **Given** all three categories are off, **When** the user views the calendar,
   **Then** no user events are shown (empty-day states render normally).
4. **Given** a category is turned off, **When** the user opens the Search
   screen, **Then** Search results are unaffected — Search remains a
   full-catalog search with its own independent category-filter chips.

---

### User Story 2 — Appearance Settings Persist Across Restart (Priority: P1)

As a user, my chosen accent color, first-day-of-week, week-numbers toggle, and
theme are remembered after I close and reopen the app.

**Why this priority**: Settings that silently reset on every launch feel broken
and erode trust.

**Independent Test**: Change each of the four settings, fully restart the app,
and confirm each retains its chosen value.

**Acceptance Scenarios**:

1. **Given** the user selects a non-default accent color, **When** the app is
   restarted, **Then** the same accent color is applied.
2. **Given** the user sets first-day-of-week to Monday, **When** the app is
   restarted, **Then** the calendar still starts weeks on Monday.
3. **Given** the user enables week numbers, **When** the app is restarted,
   **Then** week numbers remain enabled.
4. **Given** the user selects a theme (e.g., an OLED variant), **When** the app
   is restarted, **Then** the same theme is applied.
5. **Given** a first run with no saved preferences, **When** the app starts,
   **Then** the current default values are used (no crash, no blank state).

---

### User Story 3 — Confirmation Before Destructive Actions (Priority: P1)

As a user, before "Reset sample data" or "Clear all data" wipes my events, I am
asked to confirm, so a single accidental tap cannot destroy my data.

**Why this priority**: Irreversible data loss from one tap is a serious risk.

**Independent Test**: Tap each destructive action and confirm a dialog appears;
verify Cancel aborts with no data change and Confirm performs the action.

**Acceptance Scenarios**:

1. **Given** the user taps "Clear all data", **When** the confirmation dialog
   appears and the user taps Cancel, **Then** no data is deleted.
2. **Given** the confirmation dialog is shown, **When** the user confirms,
   **Then** the action executes exactly as it does today.
3. **Given** the user taps "Reset sample data", **When** the dialog appears and
   is confirmed, **Then** sample data is restored as before.
4. **Given** either dialog is open, **When** the user dismisses it (back / tap
   outside), **Then** it closes with no action taken.

---

### User Story 4 — Blank Event Titles Are Rejected (Priority: P2)

As a user, if I try to save an event without a title, I get clear feedback and
the event is not silently auto-named "New Event".

**Why this priority**: Silent substitution produces surprising, mislabeled data.

**Independent Test**: In both Add and Edit, clear the title and attempt to save;
confirm the save is blocked with localized validation feedback.

**Acceptance Scenarios**:

1. **Given** the Add Event sheet with an empty title, **When** the user attempts
   to save, **Then** the save is blocked and localized validation feedback is
   shown.
2. **Given** a title of only whitespace, **When** the user attempts to save,
   **Then** it is treated as empty and rejected.
3. **Given** the Edit Event sheet, **When** the user clears an existing title and
   attempts to save, **Then** the same validation applies.
4. **Given** a valid non-blank title, **When** the user saves, **Then** the event
   saves exactly as today (title trimmed of surrounding whitespace).

---

### User Story 5 — Persian Calendar Loading Feedback (Priority: P3)

As a user, I either see a clear loading indicator while Persian calendar data
loads, or the app shows no misleading dead state.

**Why this priority**: Lowest impact — data fills in regardless; this is about
polish and removing dead code.

**Independent Test**: Observe the calendar while Persian data loads; confirm
either a real loading indicator appears or the unused parameter is removed with
no behavioral regression.

**Acceptance Scenarios**:

1. **Given** Persian calendar data is loading, **When** the loading state is
   active, **Then** either a design-consistent loading indicator is shown, or
   (if removed) the calendar renders normally with no dead/unused loading
   parameter remaining.
2. **Given** Persian data has loaded, **When** the load completes, **Then** no
   loading indicator remains visible.

---

### User Story 6 — Snooze Description Matches Options (Priority: P3)

As a user, the snooze duration description in Settings accurately reflects the
values I can actually select.

**Why this priority**: Cosmetic/informational mismatch; selectable values work.

**Independent Test**: Compare the snooze description text against the rendered
selectable chips; confirm they match.

**Acceptance Scenarios**:

1. **Given** the Settings snooze section, **When** the user reads the
   description, **Then** every value it names is selectable, and every
   selectable value is represented.

---

### Edge Cases

- Category visibility with zero events in a category: toggling has no visible
  effect but must not crash.
- Category visibility must not affect the daily notification or event reminders
  (those are independent systems).
- Appearance persistence on first run (no saved values) must fall back to
  current defaults.
- Whitespace-only and RTL/Persian-whitespace titles must be treated as blank.
- Confirmation dialogs must respect RTL layout and use localized button labels.
- Restart-persistence must not interfere with the already-persisted calendar
  type and snooze duration.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Category visibility state (Personal, Work, Holidays) MUST filter
  which events are displayed in all calendar views (month, week, day). The
  displayed event set MUST exclude categories whose visibility is off.
- **FR-002**: Category visibility changes MUST take effect reactively without an
  app restart, and MUST persist across restarts via the existing
  `NotificationPreferences` SharedPreferences mechanism (default: all visible).
- **FR-002a**: Category visibility MUST affect the calendar views only
  (Month/Week/Day). The Search screen remains a full-catalog search with its own
  independent category-filter chips, unaffected by visibility toggles.
- **FR-003**: Category filtering MUST map to the event's category field
  (the `calendarType` field on the event: "Personal", "Work", "Holidays").
- **FR-004**: Accent color, first-day-of-week, week-numbers, and theme selections
  MUST be persisted and restored across app restarts.
- **FR-005**: Persistence MUST use the project's existing SharedPreferences
  mechanism (`NotificationPreferences`). No second persistence mechanism may be
  introduced.
- **FR-006**: On first run with no stored values, the current default values
  MUST be used.
- **FR-007**: "Reset sample data" and "Clear all data" MUST each require an
  explicit confirmation before executing; Cancel/dismiss MUST perform no action.
- **FR-008**: Confirmation dialogs MUST use the existing Liquid Glass dialog
  design pattern (as used by the event delete confirmation) and localized labels.
- **FR-009**: Saving an event with a blank or whitespace-only title MUST be
  blocked in both Add and Edit flows, with localized validation feedback.
- **FR-010**: The silent `ifBlank { "New Event" }` substitution MUST be removed
  from the save path; valid titles MUST be trimmed of surrounding whitespace.
- **FR-011**: The Persian calendar loading state MUST either drive a
  design-consistent loading indicator or be removed as dead code, with no
  behavioral regression either way.
- **FR-012**: The snooze duration description MUST accurately match the
  selectable snooze values shown in Settings.
- **FR-013**: All changes MUST preserve RTL/LTR behavior, Jalali/Gregorian/Hijri
  calendar behavior, real device-local date/timezone handling, and the
  distinction between Today and the selected browsing date.
- **FR-014**: Notification architecture, release signing, GitHub Actions, ABI
  configuration, and applicationId MUST remain unchanged.

### State & Persistence Requirements

- **SP-001**: Persisted keys live in the existing `notification_preferences`
  SharedPreferences file alongside the current calendar-type and snooze keys.
- **SP-002**: Category visibility state (Personal, Work, Holidays) MUST persist
  across app restarts using the same `NotificationPreferences` SharedPreferences
  file. On first run with no stored values, all three categories default to
  visible.
- **SP-003**: Each appearance setter MUST write to preferences; the ViewModel
  init MUST read the stored value (or default) on startup.

### Validation Behavior

- **VB-001**: A title is "blank" if it is empty or contains only whitespace
  (including Unicode/Persian whitespace) after trimming.
- **VB-002**: Validation feedback MUST be localized via the existing AppStrings
  mechanism.
- **VB-003**: Validation MUST NOT block saving otherwise-valid events.

### Key Entities

- **CalendarEvent**: Existing Room entity. Relevant field: `calendarType`
  ("Personal" / "Work" / "Holidays") used for category-visibility filtering;
  `title` subject to non-blank validation.
- **Appearance preferences**: accent color index (Int), first-day-Monday
  (Boolean), show-week-numbers (Boolean), theme name (String) — to be persisted.
- **Category visibility state**: three booleans (Personal, Work, Holidays)
  controlling the displayed event set.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Turning a category off removes 100% of that category's events from
  all calendar views, and turning it on restores them, with no restart.
- **SC-002**: All four appearance settings retain their chosen values across 100%
  of app restarts.
- **SC-003**: Both destructive actions require confirmation; zero data loss
  occurs from a Cancel/dismiss.
- **SC-004**: An event cannot be saved with a blank/whitespace title in either
  Add or Edit; the user always sees validation feedback in that case.
- **SC-005**: The snooze description names exactly the selectable snooze values.
- **SC-006**: No behavioral regression in calendar accuracy, notifications,
  reminders, RTL/LTR, or calendar switching.
- **SC-007**: Build succeeds and the test suite passes, including new tests for
  category filtering, appearance persistence, and title validation.

## Testing Requirements

- **TR-001**: Unit test category filtering — events across categories with each
  visibility combination produce the correct displayed set for calendar views,
  and category-visibility state round-trips through persistence (set → restore).
- **TR-002**: Unit test appearance persistence — set → restore round-trip for
  accent, first-day, week-numbers, theme, plus first-run defaults.
- **TR-003**: Unit test empty-title validation — empty and whitespace-only
  titles are rejected; valid titles pass and are trimmed.
- **TR-004**: Existing tests MUST continue to pass (no regression).

## Clarifications

### Session 2026-09-22

- Q: Should category visibility also filter Search results, or only the calendar
  views? → A: Calendar views only. Search remains a full-catalog search with its
  own independent category-filter chips.
- Q: Should category visibility persist across app restarts? → A: Yes. Persist
  Personal/Work/Holidays visibility via the existing SharedPreferences-backed
  `NotificationPreferences`; no second persistence mechanism.

## Assumptions

- The category-visibility toggles map to the event `calendarType` field
  ("Personal"/"Work"/"Holidays"), which is the field the toggles are named after.
  Holiday *display* sourced from the holiday service is a separate concern and
  is out of scope unless it shares that field.
- "Existing preferences mechanism" refers to the `NotificationPreferences`
  SharedPreferences singleton (file `notification_preferences`), already used for
  calendar type and snooze duration.
- The confirmation dialog pattern to reuse is the one already used for event
  deletion in the event detail sheet.
- No changes to the daily notification or event reminder systems are needed for
  any of these fixes.
