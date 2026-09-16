# Tasks: Widget Visual Refinement

**Input**: Design documents from `/specs/003-widget-visual-refinement/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Audit current widget and establish baseline

- [ ] T001 Run `./gradlew clean assembleDebug` to verify current build state
- [ ] T002 Run `./gradlew test` to establish test baseline
- [ ] T003 [P] Inspect current widget layout in app/src/main/res/layout/widget_luma_calendar.xml — document element hierarchy, text sizes, weights, spacing, and view IDs

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Ensure widget date population infrastructure is ready for the new layout

- [ ] T004 Verify CalendarConverter has functions to get Jalali month name from device date: check app/src/main/java/com/aistudio/lumacalendar/vtxk/util/CalendarConverter.kt for gregorianToJalali() and getMonthName() — confirm they return localized Jalali month names
- [ ] T005 [P] Verify CalendarConverter can produce Hijri day number: check gregorianToHijri() in CalendarConverter.kt — confirm it returns year/month/day triple that can be formatted with toPersianDigits()
- [ ] T006 [P] Verify DateUtils.getRealDeviceLocalDate() returns LocalDate in device timezone — confirm no UTC dependency in app/src/main/java/com/aistudio/lumacalendar/vtxk/util/DateUtils.kt

**Checkpoint**: Date infrastructure verified — Jalali month, Jalali day, Gregorian day, and Hijri day are all obtainable from existing functions

---

## Phase 3: User Story 1 — Spacious Widget Layout (Priority: P1) MVP

**Goal**: Transform the crowded widget into a spacious three-level layout with Persian month, large day number, and secondary calendar values

**Independent Test**: Place widget on home screen, verify Persian month at top, large Jalali day in center, two secondary values at bottom with clear spacing between all levels

### Implementation for User Story 1

- [ ] T007 [P] [US1] Redesign widget_luma_calendar.xml root structure in app/src/main/res/layout/widget_luma_calendar.xml — add `android:layoutDirection="locale"` to root LinearLayout for RTL support, increase content padding from 8dp to 10dp
- [ ] T008 [US1] Replace header strip month text in app/src/main/res/layout/widget_luma_calendar.xml — change widget_month_text from Gregorian locale month to placeholder for Jalali month (text will be populated by WidgetProvider), change fontWeight from bold to normal (Medium equivalent in XML)
- [ ] T009 [US1] Increase spacing between month and day number in app/src/main/res/layout/widget_luma_calendar.xml — add `android:layout_marginTop="10dp"` to widget_day_number, or adjust header bottom padding
- [ ] T010 [US1] Increase spacing between day number and secondary area in app/src/main/res/layout/widget_luma_calendar.xml — change widget_day_number marginBottom or secondary area marginTop to ~10dp (currently 2dp)
- [ ] T011 [US1] Replace weekday and event text with secondary calendar values in app/src/main/res/layout/widget_luma_calendar.xml — repurpose widget_weekday_text as widget_secondary_left (Gregorian day, 14sp, regular weight, #B3FFFFFF), repurpose widget_event_text as widget_secondary_right (Hijri day, 14sp, regular weight, #B3FFFFFF), add horizontal LinearLayout wrapper with balanced center spacing
- [ ] T012 [US1] Adjust day number text size if needed in app/src/main/res/layout/widget_luma_calendar.xml — verify 40sp is dominant enough relative to 12sp month and 14sp secondary values

**Checkpoint**: Widget XML layout restructured — three visual levels with clear spacing

---

## Phase 4: User Story 2 — Date Accuracy & Calendar Consistency (Priority: P1)

**Goal**: Widget displays correct Jalali date from device time with all three calendar systems synchronized

**Independent Test**: Compare widget values with device date converted to all three calendar systems; verify auto-update at midnight

### Implementation for User Story 2

- [ ] T013 [US2] Update WidgetProvider date population in app/src/main/java/com/aistudio/lumacalendar/vtxk/widget/LumaCalendarWidgetProvider.kt — replace Gregorian month extraction (`now.month.getDisplayName()`) with Jalali month via CalendarConverter: convert now to Jalali, get month name via CalendarConverter.getMonthName()
- [ ] T014 [US2] Update WidgetProvider day number in app/src/main/java/com/aistudio/lumacalendar/vtxk/widget/LumaCalendarWidgetProvider.kt — change widget_day_number to display Jalali day number (from CalendarConverter.gregorianToJalali()), format with CalendarConverter.toPersianDigits()
- [ ] T015 [US2] Add secondary calendar values in app/src/main/java/com/aistudio/lumacalendar/vtxk/widget/LumaCalendarWidgetProvider.kt — populate widget_secondary_left with Gregorian day (now.dayOfMonth, formatted with toPersianDigits()), populate widget_secondary_right with Hijri day (from CalendarConverter.gregorianToHijri(), formatted with toPersianDigits())
- [ ] T016 [US2] Verify all three values represent the same real-world day — confirm Jalali, Gregorian, and Hijri values are all derived from DateUtils.getRealDeviceLocalDate() via CalendarConverter JDN pathway
- [ ] T017 [US2] Verify holiday color logic still works in app/src/main/java/com/aistudio/lumacalendar/vtxk/widget/LumaCalendarWidgetProvider.kt — confirm day number turns red (#FF453A) on holidays, event text (now secondary) color is appropriate

**Checkpoint**: Widget shows correct Jalali date with synchronized secondary calendars

---

## Phase 5: User Story 3 — Typography & Visual Identity (Priority: P2)

**Goal**: Correct Vazirmatn weight hierarchy and preserved visual identity

**Independent Test**: Verify month=Medium, day=Bold, secondary=Regular; verify existing dark surface style preserved

### Implementation for User Story 3

- [ ] T018 [P] [US3] Verify typography weights in app/src/main/res/layout/widget_luma_calendar.xml — confirm month uses `android:textStyle="normal"` (Medium equivalent), day uses `android:textStyle="bold"`, secondary uses `android:textStyle="normal"` (Regular)
- [ ] T019 [P] [US3] Verify visual identity preserved — confirm widget_glass_bg background (dark #E60D1120, 22dp corners, indigo stroke) is unchanged, widget_header_bg gradient is unchanged
- [ ] T020 [US3] Verify text colors match spec — month: #FFFFFFFF, day: #FFFFFFFF (normal) / #FF453A (holiday), secondary: #B3FFFFFF

**Checkpoint**: Typography correct, visual identity preserved

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and regression check

- [ ] T021 [P] Run `./gradlew clean assembleDebug` — verify build succeeds
- [ ] T022 [P] Run `./gradlew test` — verify all existing tests pass
- [ ] T023 Visual verification on device/emulator — place widget on home screen, verify: Persian month at top, large Jalali day centered, two secondary values at bottom, clear spacing between all three levels, no crowded appearance
- [ ] T024 Verify widget auto-update — wait for midnight or change device timezone, confirm widget updates automatically
- [ ] T025 Verify non-regression — confirm Daily Notification (ID 1001) unaffected, Event Reminders unaffected, DynamicIconManager shortcut icon still updates, widget click still opens MainActivity

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS user stories
- **US1 (Phase 3)**: Depends on Phase 2 — primary layout work
- **US2 (Phase 4)**: Depends on Phase 2 — date population (can parallel with US1 since US1 modifies XML and US2 modifies Kotlin)
- **US3 (Phase 5)**: Depends on US1 (layout must exist to verify typography)
- **Polish (Phase 6)**: Depends on all user stories

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2 — XML layout changes
- **US2 (P1)**: Can start after Phase 2 — Kotlin date population (different file than US1)
- **US3 (P2)**: Depends on US1 (verifying typography on the new layout)

### Parallel Opportunities

- **Phase 1**: T003 is standalone
- **Phase 2**: T005, T006 are parallelizable (different checks)
- **Phase 3 (US1)**: T007 is first, T008-T012 build on it sequentially
- **Phase 4 (US2)**: T013-T017 are sequential (all in same Kotlin file)
- **US1 + US2**: Can run in parallel (different files: XML vs Kotlin)
- **Phase 5 (US3)**: T018, T019, T020 are parallelizable (different verification checks)
- **Phase 6**: T021, T022 are parallelizable

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1: Setup (audit)
2. Complete Phase 2: Foundational (verify date infrastructure)
3. Complete Phase 3: US1 — XML layout restructured
4. Complete Phase 4: US2 — Kotlin date population updated
5. **STOP and VALIDATE**: Place widget on home screen, verify visual + date accuracy
6. Core layout + data confirmed

### Incremental Delivery

1. Setup + Foundational — infrastructure verified
2. US1 (Layout) — XML restructured
3. US2 (Date Accuracy) — correct Jalali dates
4. US3 (Typography) — weights verified
5. Polish — full sign-off

### Total: 25 tasks across 6 phases

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Setup | 3 | 1 |
| Foundational | 3 | 2 |
| US1 Layout | 6 | 1 |
| US2 Dates | 5 | 0 |
| US3 Typography | 3 | 3 |
| Polish | 5 | 2 |
