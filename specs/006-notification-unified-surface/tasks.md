# Tasks: Daily Notification Unified Surface

**Input**: Design documents from `/specs/006-notification-unified-surface/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Verify current state and confirm root cause

- [ ] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [ ] T002 Run `./gradlew test` to establish test baseline
- [ ] T003 [P] Verify DecoratedCustomViewStyle is the source of the outer container — confirm `.setStyle(NotificationCompat.DecoratedCustomViewStyle())` exists in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt (line ~316)
- [ ] T004 [P] Verify custom header rows are hidden — confirm `android:visibility="gone"` on header LinearLayouts in both app/src/main/res/layout/notification_luma_calendar.xml and notification_luma_calendar_expanded.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Remove DecoratedCustomViewStyle and eliminate the system container

**CRITICAL**: The system container from DecoratedCustomViewStyle is the outer rectangle. It must be removed before visual refinement begins.

- [ ] T005 Remove `.setStyle(NotificationCompat.DecoratedCustomViewStyle())` from the NotificationCompat.Builder in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — this eliminates the Android system container around custom RemoteViews (FR-001, FR-003, research.md Option A)
- [ ] T006 [P] Un-hide the custom header row in app/src/main/res/layout/notification_luma_calendar.xml — change `android:visibility="gone"` to `android:visibility="visible"` on the header LinearLayout, style it to show app icon + "Luma Calendar" + timestamp (replaces the system header lost by removing DecoratedCustomViewStyle)
- [ ] T007 [P] Un-hide the custom header row in app/src/main/res/layout/notification_luma_calendar_expanded.xml — same change as T006 for the expanded layout
- [ ] T008 Build and visually verify on device/emulator — confirm the system container is gone, the notification is now one unified surface with the custom header visible

**Checkpoint**: System container eliminated — notification is now a single unified surface

---

## Phase 3: User Story 1 — Unified Notification Surface (Priority: P1) MVP

**Goal**: Notification looks like ONE cohesive Luma Calendar surface with correct constants and ongoing behavior

**Independent Test**: Pull down notification shade, verify single unified surface, no system container, ID 1001, ongoing

### Implementation for User Story 1

- [ ] T009 [P] [US1] Verify notification constants in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — confirm NOTIFICATION_ID_DAILY = 1001, CHANNEL_ID_DAILY = "luma_calendar_daily", setOngoing(true), setAutoCancel(false)
- [ ] T010 [P] [US1] Verify no setSubText() call exists in LumaNotificationManager.kt — app name must appear only once (in the custom header, not duplicated)
- [ ] T011 [US1] Style the custom header in both notification XML layouts — ensure the header shows: small app icon (ic_notification_luma), "Luma Calendar" text (from string resource), and timestamp. Use existing design tokens for colors.
- [ ] T012 [US1] Verify the notification is ongoing and non-dismissible — tap the notification, confirm it does NOT dismiss
- [ ] T013 [US1] Verify single instance — notification ID 1001 exists exactly once in the shade

**Checkpoint**: Unified surface verified — one cohesive notification, correct identity

---

## Phase 4: User Story 2 — Date Accuracy & Readability (Priority: P1)

**Goal**: Primary date fully readable, secondary dates visible, all three calendars synchronized

**Independent Test**: Verify primary date not truncated, secondary dates show other two calendars, same real-world day

### Implementation for User Story 2

- [ ] T014 [P] [US2] Verify date population in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — confirm primary date uses DateUtils.getRealDeviceLocalDate()
- [ ] T015 [P] [US2] Verify all three calendar conversions in LumaNotificationManager.kt — confirm Jalali, Gregorian, and Hijri are computed from the same real-world day via CalendarConverter
- [ ] T016 [US2] Fix primary date truncation in app/src/main/res/layout/notification_luma_calendar.xml — evaluate: available width, mini tile width, padding, font size, line count. Ensure primary date is readable and complete. May need to reduce mini tile width or allow text wrapping.
- [ ] T017 [US2] Fix primary date truncation in app/src/main/res/layout/notification_luma_calendar_expanded.xml — same evaluation for expanded layout
- [ ] T018 [US2] Verify mini calendar tile population — confirm tile_month and tile_day use the active calendar type
- [ ] T019 [US2] Test calendar switching — change active calendar type in Settings, verify notification updates correctly

**Checkpoint**: Dates fully readable, no truncation, three calendars synchronized

---

## Phase 5: User Story 3 — Actions, Message & Layout (Priority: P2)

**Goal**: Compact actions, secondary message, both collapsed and expanded states intentionally designed

**Independent Test**: Verify actions compact, message secondary, both states polished

### Implementation for User Story 3

- [ ] T020 [P] [US3] Simplify action button backgrounds in app/src/main/res/drawable/notification_action_btn_primary_bg.xml — reduce gradient opacity, reduce stroke from 1dp to 0.5dp or remove, ensure buttons are subtle glass controls
- [ ] T021 [P] [US3] Simplify action button backgrounds in app/src/main/res/drawable/notification_action_btn_bg.xml — same treatment for secondary action buttons
- [ ] T022 [US3] Verify action button styling in app/src/main/res/layout/notification_luma_calendar_expanded.xml — confirm equal visual weight (weight=1, consistent height), readable labels, proper spacing
- [ ] T023 [US3] Verify daily message in LumaNotificationManager.kt — confirm it comes from AppStrings via LocalizationManager, not hardcoded
- [ ] T024 [US3] Verify collapsed layout in notification_luma_calendar.xml — confirm it shows primary date + secondary date, no excessive content, no truncation
- [ ] T025 [US3] Verify expanded layout in notification_luma_calendar_expanded.xml — confirm it shows full date info, secondary dates, message, and actions on one coherent surface
- [ ] T026 [US3] Verify RTL layout in Jalali mode — confirm header, dates, and actions all flow RTL correctly

**Checkpoint**: Actions compact, message secondary, both states polished

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cleanup and final verification

- [ ] T027 [P] Delete dead code — remove app/src/main/res/drawable/notification_glass_bg.xml (no longer referenced by any layout)
- [ ] T028 [P] Verify tile background — confirm notification_calendar_tile_bg.xml uses subtle #0DFFFFFF fill with 8dp corners, no heavy gradient or stroke
- [ ] T029 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [ ] T030 [P] Run `./gradlew test` — verify all existing tests pass
- [ ] T031 Run quickstart.md validation scenarios V1-V10 on device/emulator — verify all outcomes
- [ ] T032 Verify non-regression — confirm Daily Notification (ID 1001) persists, Event Reminders unaffected, midnight update works, reboot restore works

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user stories. The DecoratedCustomViewStyle removal is the critical fix.
- **Phase 3 (US1)**: Depends on Phase 2 — unified surface must exist first
- **Phase 4 (US2)**: Depends on Phase 2 — can parallel with US3 (different files: XML dates vs XML actions)
- **Phase 5 (US3)**: Depends on Phase 2 — can parallel with US4
- **Phase 6 (Polish)**: Depends on all user stories

### Parallel Opportunities

- **Phase 1**: T003, T004 are parallelizable (different verification checks)
- **Phase 2**: T006, T007 are parallelizable (different XML files)
- **Phase 3**: T009, T010 are parallelizable (different checks)
- **Phase 4**: T014, T015 are parallelizable (different date checks)
- **Phase 5**: T020, T021 are parallelizable (different drawable files)
- **Phase 6**: T027, T028, T029, T030 are parallelizable (different files/tasks)

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1: Setup (audit)
2. Complete Phase 2: Foundational (remove DecoratedCustomViewStyle)
3. Complete Phase 3: US1 — unified surface + header
4. Complete Phase 4: US2 — date readability
5. **STOP and VALIDATE**: Run quickstart V1, V2, V3, V8
6. Core visual fix confirmed

### Incremental Delivery

1. Setup + Foundational — system container eliminated
2. US1 (Unified Surface) — header styled, identity correct
3. US2 (Date Readability) — no truncation, three calendars
4. US3 (Actions & Layout) — compact controls, both states
5. Polish — dead code removed, full sign-off

### Total: 32 tasks across 6 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 4 | 2 |
| Foundational | 4 | 2 |
| US1 Unified Surface | 5 | 2 |
| US2 Date Readability | 6 | 2 |
| US3 Actions & Layout | 7 | 2 |
| Polish | 6 | 4 |
