# Tasks: Time Picker Hour/Minute Ordering Fix

**Input**: Design documents from `/specs/005-time-picker-ordering-fix/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Verify current state

- [ ] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [ ] T002 Run `./gradlew test` to establish test baseline
- [ ] T003 [P] Inspect the time-control Row in LiquidGlassTimePickerDialog.kt — find the Row at line ~247 that contains Hour Column, Colon, Minute Column, AM/PM Column and confirm it inherits the global LayoutDirection

---

## Phase 2: User Story 1 — Stable Time Ordering (Priority: P1) MVP

**Goal**: Force Hour → Separator → Minute ordering to always be left-to-right regardless of surrounding RTL

**Independent Test**: Open Time Picker in Jalali mode, verify Hour on left, Minute on right. Switch to Gregorian, verify same ordering.

### Implementation for User Story 1

- [ ] T004 [US1] Wrap the time-control Row in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)` in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/LiquidGlassTimePickerDialog.kt — find the Row containing Hour/Colon/Minute/AM/PM (line ~247), wrap it so LTR is forced on this Row only while the rest of the dialog retains RTL
- [ ] T005 [US1] Verify the fix in Jalali (RTL) mode — confirm visual order is [Hour] [Colon] [Minute] from left to right, header/buttons remain RTL
- [ ] T006 [US1] Verify the fix in Gregorian (LTR) mode — confirm visual order is [Hour] [Colon] [Minute] from left to right (unchanged from before)
- [ ] T007 [US1] Verify Hour plus/minus controls modify hour (not minute) in both RTL and LTR modes
- [ ] T008 [US1] Verify Minute plus/minus controls modify minute (not hour) in both RTL and LTR modes
- [ ] T009 [US1] Verify colon separator remains centered between Hour and Minute with balanced spacing

**Checkpoint**: Time ordering is stable — Hour → Minute always left-to-right

---

## Phase 3: User Story 2 — Surrounding RTL Preserved (Priority: P1)

**Goal**: Dialog header, labels, and buttons remain in correct RTL/LTR while time row is forced LTR

**Independent Test**: Open Time Picker in Jalali mode, verify header flows RTL, buttons in RTL positions, time row is LTR

### Implementation for User Story 2

- [ ] T010 [P] [US2] Verify dialog header (title, clock icon, 12H/24H toggle) flows RTL in Jalali mode in LiquidGlassTimePickerDialog.kt
- [ ] T011 [P] [US2] Verify Cancel/Confirm buttons are in RTL positions in Jalali mode (Confirm on left, Cancel on right)
- [ ] T012 [US2] Verify quick minute pick chips flow RTL in Jalali mode
- [ ] T013 [US2] Verify AM/PM pills are correctly positioned relative to the LTR time row in 12H mode

**Checkpoint**: Surrounding RTL preserved, only time row is LTR

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Final verification

- [ ] T014 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [ ] T015 [P] Run `./gradlew test` — verify all existing tests pass
- [ ] T016 Run quickstart.md validation scenarios V1-V7 — verify all outcomes
- [ ] T017 Verify non-regression — confirm event creation/editing works, Daily Notification (ID 1001) unaffected, Event Reminders unaffected

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (US1)**: Depends on Phase 1 — the core fix
- **Phase 3 (US2)**: Depends on Phase 2 — verification of surrounding RTL
- **Phase 4 (Polish)**: Depends on Phases 2 and 3

### Parallel Opportunities

- **Phase 1**: T003 is standalone
- **Phase 2**: T005-T009 are sequential verification steps
- **Phase 3**: T010, T011 are parallelizable (different sections of same file)
- **Phase 4**: T014, T015 are parallelizable (build vs test)

---

## Implementation Strategy

### This is a surgical fix

1. Phase 1: Verify current state (2 min)
2. Phase 2: Wrap one Row in CompositionLocalProvider (5 min)
3. Phase 3: Verify surrounding RTL (5 min)
4. Phase 4: Build + test (2 min)

**Total: ~14 tasks, ~5 minutes of implementation**

### Total: 17 tasks across 4 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 1 |
| US1 Stable Ordering | 6 | 0 |
| US2 Surrounding RTL | 4 | 2 |
| Polish | 4 | 2 |
