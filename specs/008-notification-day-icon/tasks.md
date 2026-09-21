# Tasks: Notification Small Icon — Dynamic Persian Day Number

**Input**: Design documents from `/specs/008-notification-day-icon/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Establish baseline and confirm technical constraints

- [ ] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [ ] T002 Run `./gradlew test` to establish test baseline
- [x] T003 [P] Confirm the bell drawable is referenced by BOTH the daily notification (LumaNotificationManager.kt:299) and event reminders (EventNotifications.kt:300) — the daily notification icon changes, EventNotifications.kt MUST stay untouched (FR-015, research.md Decision 5)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the monochrome small-icon renderer that all stories depend on

**CRITICAL**: The small-icon renderer must exist before the notification can use it

- [x] T004 Add `generateSmallIcon(context: Context, dayText: String, sizePx: Int): Bitmap` to app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationIconGenerator.kt — render a monochrome alpha-mask bitmap: opaque white (#FFFFFFFF) calendar silhouette + centered day number on a fully transparent background, NO gradient/color (system tints it, per research.md Decision 1). Reuse Vazirmatn typeface loading and the single/double-digit text-size adaptation pattern from generateIcon()

**Checkpoint**: Small-icon renderer produces a white-on-transparent day-number bitmap

---

## Phase 3: User Story 1 — Recognizable Luma Identity (Priority: P1) MVP

**Goal**: Replace the bell small icon on the daily notification with the compact Luma calendar day-number icon

**Independent Test**: Open notification shade, verify bell is gone and icon reads as a Luma calendar mark, legible on light/dark backgrounds

### Implementation for User Story 1

- [x] T005 [US1] Replace `.setSmallIcon(R.drawable.ic_notification_luma)` (line 299) in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt with `.setSmallIcon(IconCompat.createWithBitmap(smallIconBitmap))` where smallIconBitmap is produced by LumaNotificationIconGenerator.generateSmallIcon(). Add the `androidx.core.graphics.drawable.IconCompat` import
- [x] T006 [US1] Verify EventNotifications.kt:300 still uses `.setSmallIcon(R.drawable.ic_notification_luma)` — confirm it was NOT changed (FR-015)
- [ ] T007 [US1] Build and visually verify on device — bell gone from daily notification, calendar day-number icon present, legible on light + dark system UI

**Checkpoint**: Daily notification shows the Luma calendar day-number small icon; event reminders unchanged

---

## Phase 4: User Story 2 — Current Jalali Day Number (Priority: P1)

**Goal**: The small icon shows the real device-local Jalali day number

**Independent Test**: Compare the icon's number against the app's primary Jalali date; confirm match and that selection/viewed date does not affect it

### Implementation for User Story 2

- [x] T008 [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt, compute the small-icon day text from the existing device-local Jalali day (`j.day` from `CalendarConverter.gregorianToJalali(todayDateStr)`, already computed at line 150). Pass the Jalali day value to generateSmallIcon() — NOT the selected/viewed date, event date, or tileDayText derived from a non-Jalali calendar type
- [x] T009 [US2] Verify the day source is `DateUtils.getRealDeviceLocalDate()` → todayDateStr → gregorianToJalali (device-time authority, FR-003) with no UTC / ±1-day workaround (FR-016)
- [ ] T010 [US2] Test: navigate calendar to another date, select events elsewhere, confirm the small icon still shows the real current Jalali day

**Checkpoint**: Icon shows correct real-current Jalali day regardless of viewed/selected date

---

## Phase 5: User Story 3 — Automatic Day-Change Refresh (Priority: P2)

**Goal**: Icon updates to the new Jalali day on date change without relaunch, no duplicate notification

**Independent Test**: Advance device date across midnight, confirm icon updates and only one notification (ID 1001) exists

### Implementation for User Story 3

- [x] T011 [US3] Verify the small icon is regenerated inside `updateNotification()` so every refresh (midnight alarm, ReminderRescheduleReceiver boot/time/timezone/package-update, foreground) re-renders it with the current day — no new code path needed, but confirm generateSmallIcon() is called on the refresh path, not cached across dates
- [x] T012 [US3] Verify notification is re-posted with ID 1001 (NOTIFICATION_ID_DAILY) in place — no duplicate notification created (FR-010, SC-006)
- [ ] T013 [US3] Test day-change: advance device date across midnight, reboot, and timezone change; confirm icon reflects new Jalali day each time

**Checkpoint**: Icon self-refreshes on all date-change triggers, single notification maintained

---

## Phase 6: User Story 4 — Localized Digit Rendering (Priority: P2)

**Goal**: Day number uses Persian digits under Jalali localization, Latin under Gregorian/English; single & double digits fit

**Independent Test**: Switch localization, observe digits across single/double-digit days

### Implementation for User Story 4

- [x] T014 [US4] In LumaNotificationManager.kt, format the small-icon day text via `CalendarConverter.toPersianDigits()` when calendarType is JALALI (matching jDayPersian at line 152); use Latin digits for GREGORIAN/English (FR-007). No hardcoded per-digit strings
- [ ] T015 [US4] Verify generateSmallIcon() text-size adaptation keeps single-digit (۹) and double-digit (۳۱) days fully within bounds, no clipping (FR-008, SC-004)
- [ ] T016 [US4] Test: single-digit day (۹), double-digit day (۳۱), and Gregorian mode (Latin "26") all render correctly

**Checkpoint**: Localized digits correct, all days 1–31 fit

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Tests and final verification

- [x] T017 [P] Extend app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt with a unit test for the day-number derivation (device-local date → Jalali day → localized digits) as a pure function of inputs (constitution Principle VII)
- [ ] T018 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [ ] T019 [P] Run `./gradlew test` — verify all tests pass
- [ ] T020 Run quickstart.md validation scenarios V1-V7 on device/emulator — verify all outcomes
- [ ] T021 Verify non-regression — Daily Notification ID 1001 persists, ongoing/non-dismissible, body layout unchanged (FR-012), event reminders keep bell icon (FR-015)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — the renderer BLOCKS all stories
- **Phase 3 (US1)**: Depends on Phase 2 — wires the renderer into the notification
- **Phase 4 (US2)**: Depends on US1 — day source correctness
- **Phase 5 (US3)**: Depends on US1/US2 — refresh behavior
- **Phase 6 (US4)**: Depends on US1/US2 — digit localization
- **Phase 7 (Polish)**: Depends on all stories

### Parallel Opportunities

- **Phase 1**: T003 is standalone
- **Phase 7**: T017, T018, T019 are parallelizable

Note: Most implementation tasks touch the same two files
(LumaNotificationManager.kt, LumaNotificationIconGenerator.kt), so they
run largely sequentially.

---

## Implementation Strategy

### MVP (US1 + US2)

1. Phase 1: Setup + confirm event-reminder constraint
2. Phase 2: Build generateSmallIcon() (monochrome renderer)
3. Phase 3: Wire it into the daily notification (US1)
4. Phase 4: Feed the real Jalali day (US2)
5. **STOP and VALIDATE**: quickstart V1, V2
6. Core icon behavior confirmed

### Total: 21 tasks across 7 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 1 |
| Foundational | 1 | 0 |
| US1 Brand Icon | 3 | 0 |
| US2 Jalali Day | 3 | 0 |
| US3 Auto Refresh | 3 | 0 |
| US4 Localized Digits | 3 | 0 |
| Polish | 5 | 3 |
