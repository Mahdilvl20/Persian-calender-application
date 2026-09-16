# Tasks: Time Picker RTL/LTR Fix & UI Refinement

**Input**: Design documents from `/specs/004-fix-time-picker/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Audit current state and establish baseline

- [x] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [x] T002 Run `./gradlew test` to establish test baseline
- [x] T003 [P] Inspect LiquidGlassTimePickerDialog.kt in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/LiquidGlassTimePickerDialog.kt — document: parameter list, LayoutDirection handling, time field structure, touch target sizes, focus/error states, hardcoded values

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fix the core RTL bug that blocks all user stories

**CRITICAL**: The static `isRtl: Boolean` parameter must be replaced before visual or interaction work begins

- [x] T004 Change TimePickerDialog parameter from `isRtl: Boolean` to `calendarType: CalendarType` in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/LiquidGlassTimePickerDialog.kt — update function signature (line 95), derive layout direction inside composable using `LocalizationManager.getLayoutDirection(calendarType)`
- [x] T005 Update caller in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/AddEditEventSheet.kt — change from passing `isRtl = LocalizationManager.isRtl(activeCalendarType)` to passing `calendarType = activeCalendarType` directly
- [x] T006 [P] Verify the LayoutDirection derivation in LiquidGlassTimePickerDialog.kt — confirm `CompositionLocalProvider(LocalLayoutDirection provides LocalizationManager.getLayoutDirection(calendarType))` is correct and produces Rtl for JALALI/HIJRI, Ltr for GREGORIAN
- [x] T007 Build and verify RTL layout in Jalali mode — confirm hour on right, minute on left, header flows RTL, cancel/confirm positions correct

**Checkpoint**: RTL bug fixed — layout direction is now dynamic and correct

---

## Phase 3: User Story 1 — RTL/LTR Correctness (Priority: P1) MVP

**Goal**: Time Picker displays correctly in both RTL and LTR with proper field positioning

**Independent Test**: Switch between Jalali and Gregorian, open Time Picker in each, verify correct field positions

### Implementation for User Story 1

- [x] T008 [P] [US1] Verify hour/minute visual order in RTL mode in LiquidGlassTimePickerDialog.kt — confirm Row with source order `[hour][colon][minute][am/pm]` produces visual `[am/pm][minute][colon][hour]` when LayoutDirection is Rtl
- [x] T009 [P] [US1] Verify hour/minute visual order in LTR mode in LiquidGlassTimePickerDialog.kt — confirm visual order is `[hour][colon][minute][am/pm]`
- [x] T010 [US1] Verify Persian digit formatting in LiquidGlassTimePickerDialog.kt — confirm `LocalizationManager.formatDigits()` converts ASCII digits to Persian Unicode digits (۰-۹) in RTL mode, and numbers are NOT reversed
- [x] T011 [US1] Fix colon separator direction in LiquidGlassTimePickerDialog.kt — add explicit `Modifier.layoutDirection(LayoutDirection.Ltr)` or `textDirection = TextDirection.Ltr` to the colon `Text` composable (line 308) to ensure consistent rendering in both RTL and LTR
- [x] T012 [US1] Verify AM/PM positioning in RTL mode — confirm AM/PM column is visually on the left side (end of RTL reading flow) when in 12H mode
- [x] T013 [US1] Test calendar type switch while dialog is open — verify layout updates dynamically without closing the dialog

**Checkpoint**: RTL/LTR correct in both modes, dynamic updates work

---

## Phase 4: User Story 2 — Time Field Layout & Interaction (Priority: P1)

**Goal**: Clear visual hierarchy with well-spaced fields, visible active field highlight, and correct touch targets

**Independent Test**: Open Time Picker, interact with fields, verify visual feedback and touch target sizes

### Implementation for User Story 2

- [x] T014 [US2] Add active field tracking state in LiquidGlassTimePickerDialog.kt — add `var activeField by remember { mutableStateOf<String?>(null) }` where values are "hour", "minute", or null; update on stepper tap
- [x] T015 [US2] Add active field highlight to hour box in LiquidGlassTimePickerDialog.kt — when `activeField == "hour"`, apply brighter border (e.g., AccentElectricBlue at full opacity) and slightly brighter background; when inactive, dim border (e.g., AccentElectricBlue at 50% opacity)
- [x] T016 [US2] Add active field highlight to minute box in LiquidGlassTimePickerDialog.kt — same pattern as hour but with AccentRoyalViolet
- [x] T017 [US2] Increase stepper button touch targets in LiquidGlassTimePickerDialog.kt — change `.size(36.dp)` on IconButton modifiers (lines 265, 296, 327, 359) to `.size(48.dp)` per FR-007 and SC-004
- [x] T018 [US2] Increase quick pick chip touch targets in LiquidGlassTimePickerDialog.kt — ensure 48dp minimum height by adding `Modifier.heightIn(min = 48.dp)` or adjusting padding
- [x] T019 [US2] Increase 12H/24H toggle touch target in LiquidGlassTimePickerDialog.kt — ensure 48dp minimum size
- [x] T020 [US2] Increase AM/PM pill touch targets in LiquidGlassTimePickerDialog.kt — ensure 48dp minimum size
- [x] T021 [US2] Verify spacing between time fields in LiquidGlassTimePickerDialog.kt — confirm colon has adequate padding (currently 10dp horizontal) and fields are visually balanced

**Checkpoint**: Active field highlighted, all touch targets meet 48dp minimum

---

## Phase 5: User Story 3 — AM/PM or 24-Hour Behavior (Priority: P1)

**Goal**: Correct 24-hour and 12-hour time handling with localized labels

**Independent Test**: Toggle between 24H and 12H modes, verify AM/PM labels localized, values correct

### Implementation for User Story 3

- [x] T022 [P] [US3] Verify 24-hour mode in LiquidGlassTimePickerDialog.kt — confirm hours display 0-23, stepper wraps correctly at 0↔23
- [x] T023 [P] [US3] Verify 12-hour mode in LiquidGlassTimePickerDialog.kt — confirm `hour12` is displayed (0→12, 13→1), stepper adjusts correctly
- [x] T024 [US3] Verify AM/PM label localization in LiquidGlassTimePickerDialog.kt — confirm Persian locale shows "ق.ظ" / "ب.ظ", English shows "AM" / "PM" (lines 376-377)
- [x] T025 [US3] Verify AM/PM toggle logic in LiquidGlassTimePickerDialog.kt — AM click subtracts 12 from hour if ≥12, PM click adds 12 if <12

**Checkpoint**: 12H/24H modes correct, AM/PM localized

---

## Phase 6: User Story 4 — Visual Design & Premium Feel (Priority: P2)

**Goal**: Liquid Glass design language preserved, correct typography, no hardcoded values

**Independent Test**: Compare Time Picker with other Luma Calendar components, verify visual consistency

### Implementation for User Story 4

- [x] T026 [P] [US4] Replace hardcoded color in LiquidGlassTimePickerDialog.kt — change `Color(0xFF0D1426)` (line 161) to nearest design token from Color.kt (e.g., `CanvasNavy` or `CanvasSurface`)
- [x] T027 [P] [US4] Verify typography weights in LiquidGlassTimePickerDialog.kt — confirm: title = SemiBold, time values = SemiBold, colon = Medium, labels = Medium, buttons = Medium
- [x] T028 [US4] Move hardcoded strings to AppStrings in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/Localization.kt (AppStrings data class) — add entries for "Quick Minutes"/"دقایق متداول", "Duration from Start"/"مدت زمان از زمان شروع"
- [x] T029 [US4] Update LiquidGlassTimePickerDialog.kt to use localized strings — replace inline RTL/LTR text switches (lines 439, 492) with `strings.quickMinutes` and `strings.durationFromStart`
- [x] T030 [US4] Move hardcoded contentDescription strings to AppStrings — add entries for "Increase Hour"/"افزایش ساعت", "Decrease Hour"/"کاهش ساعت", "Increase Minute"/"افزایش دقیقه", "Decrease Minute"/"کاهش دقیقه" and use in stepper buttons

**Checkpoint**: No hardcoded colors or strings, typography correct

---

## Phase 7: User Story 5 — Validation & Error States (Priority: P2)

**Goal**: Clear validation feedback for edge cases

**Independent Test**: Verify stepper wrapping, safe clamping, no crashes from extreme values

### Implementation for User Story 5

- [x] T031 [P] [US5] Verify TimeValidator safety in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/TimeValidator.kt — confirm `ParsedTime.ofSafe()` clamps hour 0-23, minute 0-59
- [x] T032 [P] [US5] Verify stepper wrapping in LiquidGlassTimePickerDialog.kt — confirm hour wraps at 0↔23, minute wraps at 0↔55 (5-min increments)
- [x] T033 [US5] Test pre-filled time handling in LiquidGlassTimePickerDialog.kt — verify "00:00", "23:59", and "12:34" all display correctly in both RTL and LTR
- [x] T034 [US5] Test rapid field switching — verify no visual glitches when quickly tapping between hour and minute steppers

**Checkpoint**: Validation correct, no crashes from any input

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and regression check

- [x] T035 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [x] T036 [P] Run `./gradlew test` — verify all existing tests pass
- [x] T037 Run quickstart.md validation scenarios V1-V9 on device/emulator — verify all expected outcomes
- [x] T038 Verify non-regression — confirm event creation/editing still works, Daily Notification (ID 1001) unaffected, Event Reminders unaffected

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1-5 (Phase 3-7)**: All depend on Phase 2 completion
  - US1 (RTL/LTR) and US2 (Fields) can run in parallel
  - US3 (AM/PM) depends on US1 (RTL affects AM/PM positioning)
  - US4 (Visual) and US5 (Validation) are independent
- **Polish (Phase 8)**: Depends on all user stories

### User Story Dependencies

- **US1 (P1)**: Depends on Phase 2 (RTL fix) — can start immediately after
- **US2 (P1)**: Depends on Phase 2 — can parallel with US1 (different aspects of same file)
- **US3 (P1)**: Depends on US1 (RTL affects AM/PM layout)
- **US4 (P2)**: Independent — can parallel with US1/US2
- **US5 (P2)**: Independent — can parallel with US1/US2

### Parallel Opportunities

- **Phase 2**: T006 is parallel with T004/T005 (verification vs implementation)
- **Phase 3 (US1)**: T008, T009, T010 are parallelizable (different checks)
- **Phase 4 (US2)**: T014-T016 are sequential (state → hour highlight → minute highlight); T017-T020 are parallelizable (different touch targets)
- **Phase 6 (US4)**: T026, T027 are parallelizable (color vs typography)
- **Phase 7 (US5)**: T031, T032 are parallelizable (TimeValidator vs stepper)
- **Phase 8**: T035, T036 are parallelizable (build vs test)

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1: Setup (audit)
2. Complete Phase 2: Foundational (fix RTL parameter)
3. Complete Phase 3: US1 — RTL/LTR correct
4. Complete Phase 4: US2 — touch targets + active field
5. **STOP and VALIDATE**: Run quickstart V1, V2, V4, V5
6. Core RTL fix + interaction improvements confirmed

### Incremental Delivery

1. Setup + Foundational — RTL bug fixed
2. US1 (RTL/LTR) — correct positioning
3. US2 (Fields) — interaction + touch targets
4. US3 (AM/PM) — 12H/24H modes
5. US4 (Visual) — design polish
6. US5 (Validation) — edge cases
7. Polish — full sign-off

### Total: 38 tasks across 8 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 1 |
| Foundational | 4 | 1 |
| US1 RTL/LTR | 6 | 2 |
| US2 Fields | 8 | 3 |
| US3 AM/PM | 4 | 2 |
| US4 Visual | 5 | 2 |
| US5 Validation | 4 | 2 |
| Polish | 4 | 2 |
