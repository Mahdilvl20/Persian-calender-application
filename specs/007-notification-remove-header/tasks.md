# Tasks: Remove Redundant Notification Header

**Input**: Design documents from `/specs/007-notification-remove-header/`

**Prerequisites**: plan.md, spec.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Phase 1: Setup

- [x] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [x] T002 [P] Inspect notification_luma_calendar.xml — find the internal header LinearLayout containing ImageView (app icon) and TextView ("Luma Calendar"), note its exact line numbers and view IDs
- [x] T003 [P] Inspect notification_luma_calendar_expanded.xml — find the same internal header LinearLayout, note its exact line numbers and view IDs

---

## Phase 2: User Story 1 — Remove Redundant Header (Priority: P1) MVP

**Goal**: Remove the internal "Luma Calendar" header from both notification layouts

**Independent Test**: Verify "Luma Calendar" appears only in system header, not inside RemoteViews

### Implementation for User Story 1

- [x] T004 [US1] Remove the internal header LinearLayout (ImageView + TextView "Luma Calendar") from app/src/main/res/layout/notification_luma_calendar.xml — delete the entire header section, keep all date/message/action content
- [x] T005 [US1] Remove the internal header LinearLayout (ImageView + TextView "Luma Calendar") from app/src/main/res/layout/notification_luma_calendar_expanded.xml — same removal
- [x] T006 [US1] Verify the primary date is now the first element in both layouts
- [x] T007 [US1] Verify no "Luma Calendar" text or app icon exists inside the RemoteViews content area

**Checkpoint**: Redundant header removed — content starts with primary date

---

## Phase 3: Polish

- [x] T008 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [x] T009 [P] Run `./gradlew test` — verify all tests pass
- [x] T010 Visual verification on device — confirm: system header shows "Luma Calendar · now", body starts with primary date, no duplicate app identity
- [x] T011 Verify non-regression — notification ID 1001 persists, ongoing, Event Reminders unaffected

---

## Dependencies

- Phase 1 → Phase 2 → Phase 3 (sequential)
- T002 and T003 are parallelizable
- T008 and T009 are parallelizable

### Total: 11 tasks across 3 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 2 |
| US1 Remove Header | 4 | 0 |
| Polish | 4 | 2 |
