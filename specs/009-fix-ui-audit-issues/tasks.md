# Tasks: Fix UI Audit Issues

**Input**: Design documents from `/specs/009-fix-ui-audit-issues/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

- [X] T001 Run `./gradlew test` and `./gradlew assembleDebug` to establish a green baseline before changes

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Extend the shared preferences layer that US1, US2, and (indirectly) all persistence stories depend on

**CRITICAL**: These preference keys/getters/setters block US1 (category persistence) and US2 (appearance persistence)

- [X] T002 Add appearance + category-visibility preference keys, getters, and setters to `NotificationPreferences` in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/EventNotifications.kt — following the existing getSnoozeMinutes/setSnoozeMinutes pattern. Add: `accent_color_index` (Int, default 0), `first_day_monday` (Boolean, default false), `show_week_numbers` (Boolean, default false), `theme_name` (String, default "Liquid Glass (Dark)"), `category_personal_visible` / `category_work_visible` / `category_holidays_visible` (Boolean, default true). Do NOT modify existing keys.

**Checkpoint**: Preferences layer ready; existing keys untouched

---

## Phase 3: User Story 1 — Category Visibility Filters Calendar (Priority: P1) MVP

**Goal**: Category toggles actually hide/show events in calendar views and persist across restarts

**Independent Test**: Create Personal/Work/Holidays events; toggle each off/on; confirm calendar views update reactively and state survives restart; Search is unaffected

### Implementation for User Story 1

- [X] T003 [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/viewmodel/LumaViewModel.kt, initialize `_calendarPersonalVisible`, `_calendarWorkVisible`, `_calendarHolidaysVisible` from the new NotificationPreferences getters (default true) instead of hardcoded `true`
- [X] T004 [US1] In LumaViewModel.kt, make `toggleCalendarPersonal/Work/Holidays` persist via NotificationPreferences.setCategory*Visible then update the StateFlow (mirror setSnoozeMinutes)
- [X] T005 [US1] In LumaViewModel.kt, add `visibleEvents: StateFlow<List<CalendarEvent>>` = `combine(allEvents, _calendarPersonalVisible, _calendarWorkVisible, _calendarHolidaysVisible)` filtering by event `calendarType`: "Personal"→personal flag, "Work"→work flag, "Holidays"→holidays flag, else→visible. Use `stateIn(WhileSubscribed(5000), emptyList())` per existing pattern
- [X] T006 [US1] In LumaViewModel.kt, change `selectedDateEvents` to derive from `visibleEvents` instead of `allEvents` (so day-detail respects visibility). Leave `filteredSearchResults` on `allEvents` (FR-002a)
- [X] T007 [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt, collect `visibleEvents` and pass it to `CalendarScreen(events = ...)` in place of `allEvents` (line ~291). Do not change the SearchScreen wiring
- [X] T008 [US1] Test category filtering + persistence in app/src/test/java/com/aistudio/lumacalendar/vtxk/ — verify the filter logic produces the correct set for each visibility combination, unknown calendarType stays visible, and category-visibility prefs round-trip (set → get)

**Checkpoint**: Category toggles filter calendar views reactively and persist

---

## Phase 4: User Story 2 — Appearance Settings Persist (Priority: P1)

**Goal**: Accent, first-day, week-numbers, theme survive restart

**Independent Test**: Change each setting, restart, confirm retained; first-run uses defaults

### Implementation for User Story 2

- [X] T009 [US2] In LumaViewModel.kt, initialize `_accentColorIndex`, `_firstDayMonday`, `_showWeekNumbers`, `_themeName` from the new NotificationPreferences getters instead of hardcoded defaults
- [X] T010 [US2] In LumaViewModel.kt, make `setAccentColorIndex`, `setFirstDayMonday`, `setShowWeekNumbers`, `setThemeName` persist via NotificationPreferences before/with updating the StateFlow
- [X] T011 [US2] Add a guard on accent index read (clamp to valid AccentPresets range) and theme name (fall back to default if unknown) per data-model VR-001/VR-002
- [X] T012 [US2] Test appearance persistence round-trip in app/src/test/ — set → restore for accent, first-day, week-numbers, theme, plus first-run defaults

**Checkpoint**: All four appearance settings persist across restart

---

## Phase 5: User Story 3 — Destructive-Action Confirmation (Priority: P1)

**Goal**: Reset/Clear require confirmation

**Independent Test**: Tap each; dialog appears; Cancel = no change; Confirm = executes; dismiss = no action

### Implementation for User Story 3

- [X] T013 [P] [US3] Add localized strings for the two confirmation dialogs (title/body/confirm/cancel) to AppStrings in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/Localization.kt (Persian + English)
- [X] T014 [US3] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/SettingsScreen.kt, gate the "Reset sample data" action (line ~749) behind a confirmation dialog reusing the existing glass dialog pattern (as used in EventDetailSheet delete). Confirm → existing `onResetSampleData`; Cancel/dismiss → no action
- [X] T015 [US3] In SettingsScreen.kt, gate the "Clear all data" action (line ~778) behind the same confirmation dialog pattern. Confirm → existing `onClearAllData`; Cancel/dismiss → no action

**Checkpoint**: Both destructive actions require confirmation

---

## Phase 6: User Story 4 — Blank Title Validation (Priority: P2)

**Goal**: Blank/whitespace titles rejected with localized feedback in Add and Edit

**Independent Test**: Empty and whitespace titles blocked with feedback; valid titles save trimmed

### Implementation for User Story 4

- [X] T016 [P] [US4] Add a localized "title required" validation string to AppStrings in Localization.kt (Persian + English)
- [X] T017 [US4] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/AddEditEventSheet.kt, remove the `ifBlank { strings.newEvent }` substitution (line ~226). Block save when `title.isBlank()` (covers whitespace incl. Unicode), show localized inline validation feedback, and pass `title.trim()` on valid save. Applies to both Add and Edit (same sheet)
- [X] T018 [US4] Test blank-title validation in app/src/test/ — empty and whitespace-only titles rejected; valid title passes and is trimmed (validate the pure predicate `String.isBlank()` usage on representative inputs)

**Checkpoint**: Blank titles cannot be saved; valid ones save trimmed

---

## Phase 7: User Story 5 — Persian Loading State (Priority: P3)

**Goal**: Resolve the unused loading parameter

**Independent Test**: No dead/unused loading state; calendar renders normally

### Implementation for User Story 5

- [X] T019 [US5] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/CalendarScreen.kt, resolve the unused `isPersianLoading` parameter (line ~112): either surface a subtle design-consistent loading indicator gated on it, or remove the dead parameter and its pass-site in MainActivity.kt (line ~304). No behavioral regression either way

**Checkpoint**: No unused loading parameter remains

---

## Phase 8: User Story 6 — Snooze Description Matches Options (Priority: P3)

**Goal**: Snooze description names exactly the selectable values

**Independent Test**: Description text matches the rendered chips

### Implementation for User Story 6

- [X] T020 [US6] In SettingsScreen.kt, align the snooze description text (line ~341) with the rendered chip values (line ~407: 15,30,60,120,240,480). Do not change snooze persistence or the selectable set

**Checkpoint**: Description matches options

---

## Phase 9: Polish & Cross-Cutting Concerns

- [X] T021 [P] Run `./gradlew test` — verify all new + existing tests pass
- [X] T022 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [X] T023 Run quickstart.md scenarios V1-V10 on device/emulator — verify all outcomes
- [X] T024 Verify non-regression (SC-006) — Daily Notification (ID 1001) + event reminders unaffected, calendar switching/RTL/LTR/Today-vs-selected intact, no Room migration triggered

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: none — start immediately
- **Foundational (Phase 2)**: after Setup — BLOCKS US1 and US2 (needs the pref getters/setters)
- **US1 (Phase 3)** and **US2 (Phase 4)**: after Phase 2. Both touch LumaViewModel.kt so run sequentially (same file)
- **US3 (Phase 5)**, **US4 (Phase 6)**, **US5 (Phase 7)**, **US6 (Phase 8)**: independent of Phase 2; can proceed in parallel with each other and with US1/US2 where files differ
- **Polish (Phase 9)**: after all stories

### Parallel Opportunities

- T013 (US3 strings), T016 (US4 strings) are [P] but both edit Localization.kt → serialize them
- US3 (SettingsScreen), US4 (AddEditEventSheet), US5 (CalendarScreen), US6 (SettingsScreen) touch mostly different files; US3 and US6 both edit SettingsScreen.kt → serialize those two
- T021, T022 parallel (test vs build)

### File-contention notes

- `LumaViewModel.kt`: T003-T007, T009-T011 — sequential
- `SettingsScreen.kt`: T014, T015, T020 — sequential
- `Localization.kt`: T013, T016 — sequential

---

## Implementation Strategy

### MVP (US1 + US2 + US3 — the three P1 stories)

1. Phase 1: baseline
2. Phase 2: preferences layer
3. Phase 3: category filtering + persistence (US1)
4. Phase 4: appearance persistence (US2)
5. Phase 5: destructive confirmation (US3)
6. **STOP and VALIDATE**: quickstart V1-V6
7. P1 defects resolved

### Total: 24 tasks across 9 phases

| Phase | Story | Tasks |
|-------|-------|-------|
| Setup | — | 1 |
| Foundational | — | 1 |
| US1 Category filter | US1 (P1) | 6 |
| US2 Appearance persist | US2 (P1) | 4 |
| US3 Confirm dialogs | US3 (P1) | 3 |
| US4 Title validation | US4 (P2) | 3 |
| US5 Loading state | US5 (P3) | 1 |
| US6 Snooze description | US6 (P3) | 1 |
| Polish | — | 4 |
