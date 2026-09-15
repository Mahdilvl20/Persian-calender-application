# Tasks: Luma Calendar Project Rules

**Input**: Design documents from `/specs/001-project-rules/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish audit baseline and verify existing code conformance

- [ ] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [ ] T002 Run `./gradlew test` to establish test baseline and record results
- [ ] T003 [P] Verify CalendarEvent entity fields match data-model.md constraints in app/src/main/java/com/aistudio/lumacalendar/vtxk/data/CalendarEvent.kt (reminderMinutes 0-1440, date "YYYY-MM-DD" format, startTime "HH:mm" format)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Fix gaps identified in research.md that block rule compliance

**CRITICAL**: User story verification cannot be certified until foundation gaps are fixed

- [ ] T004 Add idempotency guard to EventNotificationScheduler.createChannel() in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/EventNotifications.kt at lines 140-154 — add `if (manager.getNotificationChannel(CHANNEL_ID) != null) return` before `createNotificationChannel()`, matching the pattern at LumaNotificationManager.kt:68 (FR-016)
- [ ] T005 [P] Add structured logging to LumaNotificationManager.kt with tag "LumaDailyNotification" — log: update started, permission check result, channel existence and importance, notification built, notify() called, notify() success/failure, notification cancelled with reason, receiver fired (FR-020, rule 23)
- [ ] T006 [P] Add structured logging to EventNotifications.kt with tag "LumaEventReminder" — log: alarm scheduled, alarm cancelled, notification fired, event loaded from DB, event not found abort (FR-020)
- [ ] T007 [P] Add logging to MidnightUpdateReceiver.kt with tag "LumaDailyNotification" — log: receiver fired, notification update triggered, midnight alarm scheduled (FR-020)
- [ ] T008 [P] Add logging to ReminderRescheduleReceiver.kt (NotificationReceivers.kt) with tag "LumaDailyNotification" — log: receiver fired, all events rescheduled, notification updated (FR-020)

**Checkpoint**: Foundation gaps fixed — notification system is now observable and idempotent

---

## Phase 3: User Story 1 — Calendar Core & Date Accuracy (Priority: P1) MVP

**Goal**: Verify and enforce that all date operations use device local timezone with correct JDN conversions

**Independent Test**: Launch app, verify today's date matches device in all 3 calendar systems, switch between calendars, change timezone, confirm no off-by-one errors

### Implementation for User Story 1

- [ ] T009 [P] [US1] Audit DateUtils.getRealDeviceDate() in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/DateUtils.kt — confirm all "today" calls route through getDeviceZoneId(), no LocalDate.now() without ZoneId, no Calendar.getInstance() without explicit timezone for user-facing logic
- [ ] T010 [P] [US1] Audit CalendarConverter JDN pathway in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/CalendarConverter.kt — confirm all conversions go through gregorianToJdn/jalaliToJdn/hijriToJdn, no direct Gregorian↔Jalali bypass
- [ ] T011 [P] [US1] Audit LumaViewModel.kt lines 100-120 — confirm selectedDate is never used as today, initialDeviceDate is set from DateUtils.getRealDeviceDate() at startup
- [ ] T012 [US1] Verify round-trip correctness: run CalendarAndHolidayTest.kt tests confirming gregorianToJdn→jdnToGregorian and gregorianToJdn→jdnToJalali produce consistent results
- [ ] T013 [US1] Add edge case test in app/src/test/java/com/aistudio/lumacalendar/vtxk/CalendarAndHolidayTest.kt for timezone change scenario: verify getRealDeviceDate() returns correct date after ZoneId shift

**Checkpoint**: Calendar accuracy verified — device time is authoritative, JDN pathway enforced, timezone changes handled

---

## Phase 4: User Story 2 — Premium Liquid Glass UI (Priority: P1)

**Goal**: Verify consistent use of Liquid Glass design tokens and Vazirmatn typography across all screens

**Independent Test**: Navigate all screens, verify glass surfaces, ambient glow, typography weights, and no generic Material defaults

### Implementation for User Story 2

- [ ] T014 [P] [US2] Audit GlassComponents.kt in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/GlassComponents.kt — confirm all components use GlassSurface*/GlassBorder* tokens from Color.kt, no hardcoded color alpha values for glass surfaces (exception: intentional per-component shading is acceptable)
- [ ] T015 [P] [US2] Audit Color.kt in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/theme/Color.kt — verify all token categories exist: Canvas*, GlassSurface*, GlassBorder*, Accent*, Category*, AmbientGlow*, Text*
- [ ] T016 [P] [US2] Audit typography weights across screens — verify: FontWeight.Regular for body text, FontWeight.Medium for buttons/navigation/tabs/weekdays, FontWeight.SemiBold for titles/section headers, no unjustified FontWeight.Bold usage in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/
- [ ] T017 [US2] Verify AmbientBackground usage in app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt line 245 — confirm accent glow and OLED variant are correctly applied

**Checkpoint**: Visual consistency verified — design system tokens used throughout, typography weights correct

---

## Phase 5: User Story 3 — Daily Persistent Notification (Priority: P1)

**Goal**: Verify single-instance daily notification with correct constants, persistence, and lifecycle behavior

**Independent Test**: Enable notification, verify ID 1001 exists, restart app/reboot device, confirm no duplicates, disable and re-enable

### Implementation for User Story 3

- [ ] T018 [P] [US3] Audit notification constants in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — verify: NOTIFICATION_ID_DAILY = 1001, CHANNEL_ID_DAILY = "luma_calendar_daily", setOngoing(true), setAutoCancel(false), setOnlyAlertOnce(true)
- [ ] T019 [P] [US3] Audit Mutex-based duplicate prevention in LumaNotificationManager.kt — verify notificationMutex is acquired before all updateNotification calls, preventing concurrent duplicate posts
- [ ] T020 [P] [US3] Audit channel creation idempotency in LumaNotificationManager.kt lines 65-81 — verify getNotificationChannel() != null guard exists before createNotificationChannel()
- [ ] T021 [US3] Trace notification update chain for midnight path: MidnightUpdateReceiver.onReceive → updateNotification + scheduleMidnightUpdate — verify updateNotification does NOT call scheduleMidnightUpdate (no loop), scheduleMidnightUpdate does NOT call updateNotification
- [ ] T022 [US3] Trace notification update chain for reboot path: ReminderRescheduleReceiver.onReceive → updateNotification + scheduleMidnightUpdate + rescheduleEvents — verify no circular call chain
- [ ] T023 [US3] Trace notification update chain for snooze path: LumaNotificationActionReceiver → updateNotification + scheduleSnooze → MidnightUpdateReceiver(SNOOZE_WAKEUP) → updateNotification + scheduleMidnightUpdate — verify chain terminates
- [ ] T024 [US3] Audit disableNotifications() in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/viewmodel/LumaViewModel.kt lines 451-460 — verify cancel(NOTIFICATION_ID_DAILY) is called, cancelMidnightUpdate is called, NotificationPreferences.setEnabled(false) is set
- [ ] T025 [US3] Audit enableNotifications() in LumaViewModel.kt — verify it calls updateNotificationAsync then scheduleMidnightUpdate, creating exactly one notification with ID 1001
- [ ] T026 [US3] Add notification lifecycle test in app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt — verify that posting with NOTIFICATION_ID_DAILY twice results in exactly one notification (no duplicate)

**Checkpoint**: Daily notification is rock-solid — single instance, no loops, correct lifecycle

---

## Phase 6: User Story 4 — Event Reminder Notifications (Priority: P2)

**Goal**: Verify event reminders use separate channel, fire at correct time, and are properly cancelled on edit/delete

**Independent Test**: Create event with 1-min reminder, verify fire time, edit event and verify old alarm cancelled, delete event and verify no stale notification

### Implementation for User Story 4

- [ ] T027 [P] [US4] Audit event notification constants in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/EventNotifications.kt — verify CHANNEL_ID = "event_reminders" (not "luma_calendar_daily"), notification IDs are event-derived (not 1001), ONGOING = false
- [ ] T028 [P] [US4] Audit cancel-then-schedule pattern in EventNotifications.kt schedule() method — verify cancel(context, event.id) is called BEFORE scheduling new alarm
- [ ] T029 [P] [US4] Audit deleteEvent flow in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/viewmodel/LumaViewModel.kt lines 408-414 — verify EventNotificationScheduler.cancel() is called before repository deleteEvent()
- [ ] T030 [US4] Audit event load from DB in EventNotifications.kt fire handler — verify event is loaded from Room by eventId, notification is NOT created if event does not exist (FR-011)
- [ ] T031 [US4] Verify requestCode derivation in EventNotifications.kt line 214 — confirm it produces unique int from event ID and never equals 1001
- [ ] T032 [US4] Add event reminder timing test in app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/EventNotificationSchedulerTest.kt — verify trigger-at-millis calculation: eventTime - reminderMinutes produces correct alarm time in device timezone

**Checkpoint**: Event reminders are independent, correctly timed, and properly managed

---

## Phase 7: User Story 5 — RTL/LTR Localization (Priority: P2)

**Goal**: Verify RTL layout for Jalali/Hijri and LTR for Gregorian with no manual text reversal

**Independent Test**: Switch to Jalali, verify RTL layout; switch to Gregorian, verify LTR; confirm no reversed text strings

### Implementation for User Story 5

- [ ] T033 [P] [US5] Audit LocalizationManager in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/LocalizationManager.kt (Localization.kt) — verify isRtl() returns true for JALALI and HIJRI only, getLayoutDirection() returns RTL/LTR accordingly
- [ ] T034 [P] [US5] Audit CompositionLocalProvider in app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt lines 239-243 — verify LocalLayoutDirection is provided from LocalizationManager.getLayoutDirection(calendarType)
- [ ] T035 [P] [US5] Search codebase for `.reversed()` calls on string data — verify zero instances of manual text reversal for RTL simulation in app/src/main/java/
- [ ] T036 [US5] Verify AppStrings data class has complete Persian and English variants — confirm all user-facing strings go through LocalAppStrings CompositionLocal

**Checkpoint**: RTL/LTR correctly driven by calendar type, no reversed text

---

## Phase 8: User Story 6 — Error Resilience (Priority: P2)

**Goal**: Verify no crashes from invalid input, network failure, permission denial, or calendar edge cases

**Independent Test**: Enter invalid dates, disconnect network, deny permissions, switch calendars rapidly — confirm zero crashes

### Implementation for User Story 6

- [ ] T037 [P] [US6] Audit DateValidator in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/DateValidator.kt — verify all public methods catch exceptions and return safe defaults (Valid/Invalid sealed class), no NumberFormatException or DateTimeParseException can escape
- [ ] T038 [P] [US6] Audit TimeValidator in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/TimeValidator.kt — verify all public methods catch exceptions and return safe ParsedTime defaults
- [ ] T039 [P] [US6] Audit notification permission handling in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt — verify SecurityException is caught and logged when posting notification without permission, no crash
- [ ] T040 [US6] Run existing test suite: CalendarAndHolidayTest, TimeValidatorTest, DateValidatorTest, EventNotificationSchedulerTest, LumaNotificationTest — verify all pass
- [ ] T041 [US6] Add edge case test in app/src/test/java/com/aistudio/lumacalendar/vtxk/CalendarAndHolidayTest.kt for invalid JDN input (day 0, negative JDN) — verify safe fallback behavior

**Checkpoint**: Error resilience verified — no crashes from any user input or system condition

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and rule compliance sign-off

- [ ] T042 Verify no parallel implementations exist: confirm LumaNotificationManager is the ONLY daily notification manager, EventNotificationScheduler is the ONLY event reminder scheduler (FR-024)
- [ ] T043 Verify no hardcoded business data: search app/src/main/java/ for demo dates, test events, or hardcoded "2026-" strings outside of test files (FR-022)
- [ ] T044 Verify no foreground service: confirm no `<service>` tag with foregroundServiceType in app/src/main/AndroidManifest.xml, no startForeground() calls (FR-013)
- [ ] T045 Verify no ActivityAlias: confirm zero `<activity-alias>` tags in AndroidManifest.xml, DynamicIconManager uses ShortcutManager not component switching (FR-019)
- [ ] T046 Run full build: `./gradlew clean assembleDebug` — verify success
- [ ] T047 Run full test suite: `./gradlew test` — verify all tests pass with no regressions
- [ ] T048 Run quickstart.md validation scenarios V1-V8 manually on device/emulator — verify all expected outcomes

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1-6 (Phase 3-8)**: All depend on Phase 2 completion; stories are independent of each other
- **Polish (Phase 9)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: No dependencies on other stories — can start after Phase 2
- **US2 (P1)**: No dependencies on other stories — can start after Phase 2
- **US3 (P1)**: No dependencies on other stories — can start after Phase 2
- **US4 (P2)**: Should follow US3 (notification foundation), but independently testable
- **US5 (P2)**: No dependencies on other stories — can start after Phase 2
- **US6 (P2)**: Benefits from US1-US5 being audited first, but independently testable

### Parallel Opportunities

- **Phase 1**: T003 is parallelizable
- **Phase 2**: T005, T006, T007, T008 are parallelizable (different files)
- **Phase 3**: T009, T010, T011 are parallelizable (different files)
- **Phase 4**: T014, T015, T016 are parallelizable (different files)
- **Phase 5**: T018, T019, T020 are parallelizable (different sections of same file but independent checks)
- **Phase 6**: T027, T028, T029 are parallelizable (different files)
- **Phase 7**: T033, T034, T035 are parallelizable (different files)
- **Phase 8**: T037, T038, T039 are parallelizable (different files)
- **Cross-story**: Phases 3-8 can run in parallel if team capacity allows

---

## Implementation Strategy

### MVP First (US1 + US3 Only)

1. Complete Phase 1: Setup (build baseline)
2. Complete Phase 2: Foundational (fix 2 gaps)
3. Complete Phase 3: US1 — Calendar accuracy verified
4. Complete Phase 5: US3 — Notification lifecycle verified
5. **STOP and VALIDATE**: Run quickstart V1, V2, V4, V8
6. Core rules enforced — ship confidence established

### Incremental Delivery

1. Setup + Foundational — gaps fixed, logging added
2. US1 (Calendar) — date accuracy certified
3. US3 (Daily Notification) — notification integrity certified
4. US4 (Event Reminders) — event notification certified
5. US2 (UI Design) — visual consistency certified
6. US5 (RTL) — localization certified
7. US6 (Error Resilience) — crash-free certified
8. Polish — full sign-off

### Total: 48 tasks across 9 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 1 |
| Foundational | 5 | 4 |
| US1 Calendar | 5 | 3 |
| US2 UI Design | 4 | 3 |
| US3 Daily Notification | 9 | 3 |
| US4 Event Reminders | 6 | 3 |
| US5 RTL/LTR | 4 | 3 |
| US6 Error Resilience | 5 | 3 |
| Polish | 7 | 0 |
