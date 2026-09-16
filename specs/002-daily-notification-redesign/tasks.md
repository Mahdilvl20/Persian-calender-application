# Tasks: Daily Notification Redesign

**Input**: Design documents from `/specs/002-daily-notification-redesign/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Audit current state and establish baseline

- [x] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [x] T002 Run `./gradlew test` to establish test baseline
- [x] T003 [P] Inspect current notification XML layouts to identify exact nesting layers: app/src/main/res/layout/notification_luma_calendar.xml and app/src/main/res/layout/notification_luma_calendar_expanded.xml — document which drawables create the inner card (notification_glass_bg, notification_calendar_tile_bg)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core fixes that block all visual and behavioral improvements

**CRITICAL**: User story implementation cannot begin until the root cause fix (removing nested backgrounds) is complete

- [x] T004 Remove `android:background="@drawable/notification_glass_bg"` from the root LinearLayout in app/src/main/res/layout/notification_luma_calendar.xml — set `android:background="@null"` instead, per notification-layout-contract.md root layout rule
- [x] T005 [P] Remove `android:background="@drawable/notification_glass_bg"` from the root LinearLayout in app/src/main/res/layout/notification_luma_calendar_expanded.xml — set `android:background="@null"` instead
- [x] T006 [P] Simplify the mini calendar tile background in app/src/main/res/drawable/notification_calendar_tile_bg.xml — replace heavy gradient+stroke with a subtle translucent surface: `#0DFFFFFF` solid fill, 8dp corners, no stroke
- [x] T007 Build and visually verify the notification on device/emulator — confirm the "rectangle inside rectangle" appearance is eliminated and content sits directly on the system notification surface

**Checkpoint**: Root cause fixed — notification is now a single unified surface. Visual verification required before proceeding.

---

## Phase 3: User Story 1 — Unified Notification Surface (Priority: P1) MVP

**Goal**: Ensure the notification is one cohesive Luma Calendar surface with correct constants and ongoing behavior

**Independent Test**: Pull down notification shade, verify single unified dark surface with no nested cards, ID 1001, ongoing, non-dismissible

### Implementation for User Story 1

- [x] T008 [P] [US1] Verify notification constants in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — confirm NOTIFICATION_ID_DAILY = 1001, CHANNEL_ID_DAILY = "luma_calendar_daily", setOngoing(true), setAutoCancel(false)
- [x] T009 [P] [US1] Verify DecoratedCustomViewStyle is used in LumaNotificationManager.kt — confirm .setStyle(NotificationCompat.DecoratedCustomViewStyle()) is present and setCustomContentView/setCustomBigContentView are set
- [x] T010 [US1] Verify no setSubText() call exists in LumaNotificationManager.kt — app name duplication must not occur
- [x] T011 [US1] Visual verification: build app, enable notification, verify ONE unified surface with Luma Calendar design language (dark glass feel, no inner card), ongoing behavior (tap does not dismiss)

**Checkpoint**: Unified surface verified — notification looks like one cohesive Luma Calendar surface

---

## Phase 4: User Story 2 — Date Display & Calendar Tile (Priority: P1)

**Goal**: Correct date display in all three calendar systems with integrated mini tile

**Independent Test**: Verify primary date matches device date in active calendar, secondary dates show other two, mini tile shows correct month/day

### Implementation for User Story 2

- [x] T012 [P] [US2] Verify date population logic in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — confirm primary date uses DateUtils.getRealDeviceLocalDate(), not selectedDate
- [x] T013 [P] [US2] Verify all three calendar conversions in LumaNotificationManager.kt — confirm Jalali, Gregorian, and Hijri dates are all computed from the same real-world day via CalendarConverter
- [x] T014 [US2] Verify mini tile population in LumaNotificationManager.kt — confirm tile_month and tile_day use the active calendar type's month name and day number
- [x] T015 [US2] Verify typography in notification XML layouts — confirm primary date uses `android:textStyle="bold"` (SemiBold equivalent), secondary date uses `android:textStyle="normal"`, text sizes match contract (14sp collapsed/16sp expanded for primary)
- [x] T016 [US2] Test calendar switching: change active calendar type in Settings, pull down notification, verify primary/secondary dates update correctly and tile reflects new calendar

**Checkpoint**: Date display accurate across all three calendars, tile integrated

---

## Phase 5: User Story 3 — Actions & Behavior (Priority: P1)

**Goal**: Three compact action buttons working correctly with configurable snooze

**Independent Test**: Tap Today → opens today's date; New Event → opens Add Event; Remind Later → snoozes with configurable duration using same ID 1001

### Implementation for User Story 3

- [x] T017 [P] [US3] Add snooze duration preference to app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/NotificationReceivers.kt (NotificationPreferences object) — add `getSnoozeMinutes()` and `setSnoozeMinutes()` methods, key "daily_notification_snooze_minutes", type Int, default 60, range 15–480 (clamped)
- [x] T018 [US3] Modify app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationActionReceiver.kt — replace hardcoded 60-minute snooze with `NotificationPreferences.getSnoozeMinutes(context)`, compute alarm trigger as `elapsedRealtime() + (snoozeMinutes * 60 * 1000L)`
- [x] T019 [US3] Update Toast message in LumaNotificationActionReceiver.kt to reflect actual snooze duration — use localized strings from AppStrings: "Snoozed for {X} minutes" (English) / "به مدت {X} دقیقه یادآوری شد" (Persian)
- [x] T020 [US3] Verify Today action opens device-local today's date in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationActionReceiver.kt — confirm it uses DateUtils.getRealDeviceDate() and sets viewModel.setCurrentTab(0) + viewModel.selectDate(today)
- [x] T021 [US3] Verify New Event action opens Add Event screen — confirm intent opens MainActivity with EXTRA_ACTION = "NEW_EVENT"
- [x] T022 [US3] Verify Remind Later uses same notification ID 1001 — confirm the snooze alarm targets MidnightUpdateReceiver with SNOOZE_WAKEUP action, and updateNotification() re-posts with ID 1001 (no new notification)
- [x] T023 [US3] Verify action button styling in expanded XML — confirm three actions have equal visual weight (weight=1, 34dp height), consistent background, and localized labels

**Checkpoint**: All three actions working correctly, snooze configurable

---

## Phase 6: User Story 4 — Expanded & Collapsed States (Priority: P2)

**Goal**: Both notification states intentionally designed with unified visual language

**Independent Test**: Collapsed shows identity + date + tile; expanded shows full info + message + actions. Both visually consistent.

### Implementation for User Story 4

- [x] T024 [P] [US4] Verify collapsed layout in app/src/main/res/layout/notification_luma_calendar.xml — confirm it shows: app icon, primary date, secondary date, mini tile. No excessive content.
- [x] T025 [P] [US4] Verify expanded layout in app/src/main/res/layout/notification_luma_calendar_expanded.xml — confirm it shows: primary date, secondary dates, daily message, mini tile, three action buttons
- [x] T026 [US4] Verify both layouts have NO root background drawable (from Phase 2 fix) — confirm visual consistency between states
- [x] T027 [US4] Verify mini tile is visually consistent in both layouts — same integrated treatment, correct size proportions (collapsed: 50x54dp, expanded: 60x64dp)

**Checkpoint**: Both states polished and visually consistent

---

## Phase 7: User Story 5 — Content & Typography (Priority: P2)

**Goal**: Daily message from string resources, correct typography weights, proper RTL/LTR

**Independent Test**: Message not hardcoded, typography follows weight hierarchy, layout direction matches active calendar

### Implementation for User Story 5

- [x] T028 [P] [US5] Verify daily message is sourced from AppStrings in LumaNotificationManager.kt — confirm notification_daily_message text comes from LocalizationManager/AppStrings, NOT hardcoded in XML or Kotlin
- [x] T029 [P] [US5] Add localization strings for snooze duration in app/src/main/res/values/strings.xml — add "Snooze Duration" / "مدت یادآوری" and duration format strings for English and Persian
- [x] T030 [US5] Verify RTL layout in notification_luma_calendar.xml — confirm android:layoutDirection is set appropriately based on active calendar type (RTL for Jalali/Hijri)
- [x] T031 [US5] Verify typography weight hierarchy across both notification layouts — SemiBold (bold) for primary date and day number, Medium (normal) for action labels, Regular for secondary text and message

**Checkpoint**: Content and typography correct, localization working

---

## Phase 8: User Story 6 — Snooze Duration Setting (Priority: P2, from clarification)

**Goal**: User-configurable snooze duration in Settings screen

**Independent Test**: Change snooze duration in Settings, verify new duration takes effect on next Remind Later tap

### Implementation for User Story 6

- [ ] T032 [P] [US6] Add snooze duration setting row to app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/SettingsScreen.kt — add a new settings row below the notification toggle, displaying current snooze duration with a picker/dropdown (15, 30, 45, 60, 90, 120, 180, 240, 480 minutes)
- [ ] T033 [US6] Add snooze duration state to app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/viewmodel/LumaViewModel.kt — add `_snoozeMinutes` StateFlow, read from NotificationPreferences on init, expose setter that writes to NotificationPreferences
- [ ] T034 [US6] Wire snooze duration state from LumaViewModel through LumaApp composable to SettingsScreen in app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt — pass snoozeMinutes and onSnoozeMinutesChange to SettingsScreen
- [ ] T035 [US6] Verify snooze setting persistence — change duration, force-stop app, relaunch, confirm setting is preserved

**Checkpoint**: Snooze duration configurable and persistent

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and regression check

- [x] T036 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [x] T037 [P] Run `./gradlew test` — verify all existing tests pass
- [x] T038 Verify notification lifecycle: enable → appears, force-stop → persists (no duplicate), change timezone → updates, change calendar type → updates, disable → cancels, re-enable → exactly one
- [x] T039 Run quickstart.md validation scenarios V1-V9 on device/emulator — verify all expected outcomes
- [x] T040 Verify Event Reminder independence — create event with reminder, verify event notification uses "event_reminders" channel with event-specific ID, does NOT affect daily notification 1001

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1-5 (Phase 3-7)**: All depend on Phase 2 completion; US1/US2/US3 are P1 and should be done first
- **US6 (Phase 8)**: Depends on Phase 2 + US3 (snooze preference infrastructure)
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2 — no dependencies on other stories
- **US2 (P1)**: Can start after Phase 2 — independent of US1
- **US3 (P1)**: Can start after Phase 2 — independent of US1/US2
- **US4 (P2)**: Benefits from US1 + US2 being complete (visual verification)
- **US5 (P2)**: Benefits from US2 + US3 being complete (content verification)
- **US6 (P2)**: Depends on US3 (snooze preference must exist in NotificationPreferences)

### Parallel Opportunities

- **Phase 2**: T004, T005, T006 are parallelizable (different XML files)
- **Phase 3**: T008, T009, T010 are parallelizable (different checks)
- **Phase 4**: T012, T013 are parallelizable (different date checks)
- **Phase 5**: T017 is independent; T018-T023 depend on T017
- **Phase 6**: T024, T025 are parallelizable (different layouts)
- **Phase 7**: T028, T029 are parallelizable (different files)
- **Phase 8**: T032 is independent; T033 depends on T017; T034 depends on T032+T033
- **Phase 9**: T036, T037 are parallelizable (different gradle tasks)

---

## Implementation Strategy

### MVP First (US1 + US2 + US3)

1. Complete Phase 1: Setup (audit)
2. Complete Phase 2: Foundational (remove nested backgrounds)
3. Complete Phase 3: US1 — unified surface verified
4. Complete Phase 4: US2 — date display correct
5. Complete Phase 5: US3 — actions working
6. **STOP and VALIDATE**: Run quickstart V1, V2, V3, V8
7. Core visual fix + functional behavior confirmed

### Incremental Delivery

1. Setup + Foundational — root cause fixed
2. US1 (Unified Surface) — visual fix certified
3. US2 (Date Display) — dates correct
4. US3 (Actions + Snooze) — interactions working
5. US6 (Snooze Setting) — configurable snooze
6. US4 (Expanded/Collapsed) — both states polished
7. US5 (Content/Typography) — localization complete
8. Polish — full sign-off

### Total: 40 tasks across 9 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 1 |
| Foundational | 4 | 2 |
| US1 Unified Surface | 4 | 2 |
| US2 Date Display | 5 | 2 |
| US3 Actions | 7 | 1 |
| US4 States | 4 | 2 |
| US5 Content | 4 | 2 |
| US6 Snooze Setting | 4 | 1 |
| Polish | 5 | 2 |
