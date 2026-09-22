# Tasks: Notification Icon Is Day Number Only

**Input**: Design documents from `/specs/011-notification-icon-number-only/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/day-icon-contract.md, quickstart.md

**Tests**: Included — explicitly requested by spec TR-001..TR-007 and the feature request
("Add/update tests for single-digit Jalali day, double-digit Jalali day, proper centering,
no clipping, readable rendering at actual notification icon size").

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Single-module Android app. Source paths are under
`app/src/main/java/com/aistudio/lumacalendar/vtxk/`; tests under
`app/src/test/java/com/aistudio/lumacalendar/vtxk/`. Paths are written in full.

**This feature touches exactly two files** — one function body in
`notification/LumaNotificationIconGenerator.kt` and one test class in
`notification/LumaNotificationTest.kt`. Everything therefore runs mostly in sequence; the
parallel opportunities that do exist are listed under *Parallel Opportunities* below.

---

## Phase 1: Setup

**Purpose**: Establish a green baseline before touching the icon.

- [X] T001 Run `./gradlew test` and `./gradlew assembleDebug` from the repository root to establish a green baseline before any changes

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Give every geometry assertion in US1 and US2 a shared way to measure what was
actually drawn.

**CRITICAL**: Both user stories assert against the measured ink bounding box, so no story
test can be written until this exists. No production code changes in this phase.

- [X] T002 Add an ink-bounds helper to app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt that takes a rendered icon and returns the `left/top/right/bottom` of its non-transparent pixels plus the count of fully-opaque white pixels, and confirm the class still carries `@GraphicsMode(GraphicsMode.Mode.NATIVE)` (currently line 22) so `drawText` actually rasterizes — otherwise the measured box is empty and every assertion below is meaningless. Encode these constraints from data-model.md verbatim in the helpers' documentation or companion constants: safe radius = `0.46 × size`; centering tolerance = ink center within `1% of size` of the canvas center on both axes; fill = `>= 60% of the safe area along its constraining dimension`; fit = `half-diagonal(ink box) <= safeRadius`

**Checkpoint**: geometry assertions have a seam; production code untouched; existing 7 tests in this class still pass.

---

## Phase 3: User Story 1 — Icon Shows Only the Day Number (Priority: P1) 🎯 MVP

**Goal**: Delete the calendar frame so the daily notification icon contains nothing but the day number.

**Independent Test**: quickstart **V1** — refresh the daily notification and inspect it: no rounded outline, no header separator line, no binder tabs, no container; the background outside the glyphs is fully transparent.

**Constraint quoted from data-model.md / contract §1 (FR-001, VI-001)**: "the returned image contains the day-number glyphs and nothing else. No calendar outline, border, square, page, header bar, binder tab, rounded container, or background tile is drawn."

> **NOTE: Write the test FIRST and confirm it FAILS before implementing.**

### Tests for User Story 1

- [X] T003 [US1] Add a frame-absence test in app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt asserting VI-001/TR-006 for both `generateSmallIcon(context, "۳۱", 96)` and `generateSmallIcon(context, "۹", 96)`: **every** opaque pixel lies inside the ink box returned by the T002 helper (proving the calendar outline, header separator, and both binder tabs are gone) and at least one fully-opaque white pixel exists (VI-005). Must fail before T004

### Implementation for User Story 1

- [X] T004 [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationIconGenerator.kt, delete from `generateSmallIcon()` the calendar outline (`canvas.drawRoundRect(calRect, ...)`, line ~188), the header separator (`canvas.drawLine(left, headerY, ...)`, line ~197), and both binder-tab draws (lines ~210 and ~217), together with the now-unused locals `left`, `top`, `right`, `bottom`, `calRadius`, `calRect`, `whitePaint`, `headerY`, `tabPaint`, `tabW`, `tabH`, `tabY` — leave only the day-number draw (FR-001, VI-001). Do not touch `generateIcon()` or any other function in the file (FR-010, Research Decision 6)

**Checkpoint**: US1 complete on its own — quickstart V1 passes with the current (still small, still header-anchored) number.

---

## Phase 4: User Story 2 — Number Is Large, Centered, and Never Clipped (Priority: P1)

**Goal**: The day number becomes the largest thing that fits, sits dead-center on the icon's own axes, and never clips — for single or double digits, in either glyph style.

**Independent Test**: quickstart **V2** (single digit), **V3** (double digit), **V4** (legible at real notification size).

**Constraints quoted from data-model.md**:
- Safe area: circle centered on the icon, `radius = 0.46 × canvas size`
- Fit rule: `half-diagonal(ink box) <= safeRadius`
- Center rule: `|inkCenterX − canvasCenterX| <= 0.01 × size` and `|inkCenterY − canvasCenterY| <= 0.01 × size`
- Size: ink box `>= 60%` of the safe area along its constraining dimension

> **NOTE: Write these tests FIRST and confirm they FAIL before implementing.**

### Tests for User Story 2

- [X] T005 [US2] Add a centering test in app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt asserting TR-003/VI-002 for `generateSmallIcon(context, "۹", 96)` and `generateSmallIcon(context, "۳۱", 96)`: the ink box center is within `0.01 * size` of the canvas center on **both** axes (SC-002 requires 100% of days 1–31)
- [X] T006 [US2] Add a no-clipping test in app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt asserting TR-004/VI-003 for `generateSmallIcon(context, "۹", 96)` and `generateSmallIcon(context, "۳۱", 96)`: the ink box lies fully inside the image bounds **and** `half-diagonal(ink box) <= 0.46 * size`, so no glyph pixel can be cut by the image edge or by the system's circular mask (SC-003)
- [X] T007 [US2] Add a size-and-legibility test in app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationTest.kt asserting TR-005/VI-004/VI-005 for `generateSmallIcon(context, "۹", 96)` and `generateSmallIcon(context, "۳۱", 96)`: the ink box spans `>= 60%` of the safe area along its constraining dimension and the glyph contains fully-opaque white pixels forming an unbroken mark (SC-004, SC-006)

### Implementation for User Story 2

- [X] T008 [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationIconGenerator.kt, replace inside `generateSmallIcon()` the fixed sizing `textSize = if (dayText.length > 1) size * 0.34f else size * 0.42f` and the header-relative anchor `cy = headerY + (bottom - headerY) * 0.5f - bounds.exactCenterY()` with measure-then-fit plus ink-box centering per research Decisions 1 and 3: keep the bundled bold face, start large, measure the ink bounds, shrink until `half-diagonal <= 0.46f * size`, then place with `cx = size/2 - bounds.exactCenterX()` and `cy = size/2 - bounds.exactCenterY()` (FR-002..FR-006). Keep `sizePx = 96` as the default and do not change the caller (Research Decision 7)

**Checkpoint**: US2 complete — quickstart V2, V3, V4 pass; single and double digits are large, centered, and unclipped.

---

## Phase 5: User Story 3 — Nothing Else Changes (Priority: P2)

**Goal**: Prove the change reached only the daily icon's artwork — the day, its refresh triggers, every other icon, and all notification metadata are untouched.

**Independent Test**: quickstart **V5–V9** — correct real local Jalali day, updates on date change, survives reboot/timezone/restart, Event Reminder icon unchanged, launcher icon unchanged, notification text/channel unchanged.

### Verification for User Story 3

- [X] T009 [P] [US3] Review `git diff` and confirm app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationManager.kt is **empty** (caller, the `smallIconDayText` digit rule at line 228, `setSmallIcon` at line 305, and every regeneration trigger untouched) and that `generateIcon()` in app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/LumaNotificationIconGenerator.kt has no changes (FR-007, FR-008, FR-010, VI-007)
- [X] T010 [P] [US3] Review `git diff` and confirm no changes to app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/EventNotifications.kt (Event Reminder icon stays `R.drawable.ic_notification_luma`), to the launcher icon and its day variants or `ic_notification_luma` under app/src/main/res/, to app/src/main/AndroidManifest.xml, or to any notification channel, identifier, priority, title, text, or scheduling code (FR-011..FR-013, VI-007, SC-008)

**Checkpoint**: all three stories independently verified.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T011 [P] Run `./gradlew test` from the repository root and verify the new geometry tests plus all 7 pre-existing tests in LumaNotificationTest.kt pass (TR-007, SC-009)
- [X] T012 [P] Run `./gradlew clean assembleDebug` from the repository root and verify the build succeeds (SC-009)
- [ ] T013 Run quickstart.md scenarios V1–V10 on a device or emulator and record each outcome, noting V4 (legible at actual notification size) and V5–V7 (real local day, date change, reboot/timezone/restart) are device-only
- [X] T014 Produce the implementation report required by the feature request: root cause, files changed, tests added/updated, test results, build result

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: none — start immediately
- **Foundational (Phase 2)**: after Setup — **BLOCKS both user stories** (every geometry test reads the T002 ink-bounds helper)
- **US1 (Phase 3)**: after Foundational
- **US2 (Phase 4)**: after Foundational; executes after US1 because **both phases edit the same function body** in `LumaNotificationIconGenerator.kt`
- **US3 (Phase 5)**: after US2 (it reviews the completed diff)
- **Polish (Phase 6)**: after all stories

### User Story Dependencies

- **US1 (P1)**: starts after Foundational; no dependency on other stories
- **US2 (P1)**: starts after Foundational *logically*, but shares `generateSmallIcon()` with US1 → execute after US1; independently testable once its own assertions pass
- **US3 (P2)**: after US2 — a verification story over the combined result
- No story is blocked by another *functionally*; the serialization is file contention only

### Within Each User Story

- Tests are written and confirmed failing before the implementation they cover (T003 → T004; T005–T007 → T008)
- Frame removal before sizing/centering (they share one function body)
- Verification before polish; final report last

### Parallel Opportunities

This feature is small and touches two files, so parallelism is limited — stated honestly rather than implied:

- **T009 ∥ T010** — two independent read-only `git diff` reviews over disjoint paths
- **T011 ∥ T012** — test run vs. build run
- **T004 ∥ (T005, T006, T007)** — cross-phase: T004 edits the source file while T005–T007 edit the test file, and T005–T007 depend only on T002. T005/T006/T007 themselves are **sequential** (same file).

### File-contention notes

- `notification/LumaNotificationIconGenerator.kt`: T004 → T008 (same function, sequential)
- `notification/LumaNotificationTest.kt`: T002 → T003 → T005 → T006 → T007 (same class, sequential)
- `LumaNotificationManager.kt`, `EventNotifications.kt`, `app/src/main/res/`, `AndroidManifest.xml`: **never edited** — only read by T009/T010

---

## Parallel Example: User Story 3

```bash
# Launch both scope verifications together (disjoint paths, read-only):
Task: "git diff review — LumaNotificationManager.kt + generateIcon() untouched"
Task: "git diff review — Event Reminder icon, launcher icon, res/, manifest, channel/ID/schedule untouched"
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Complete Phase 1: baseline green
2. Complete Phase 2: Foundational — ink-bounds helper (blocks all tests)
3. Complete Phase 3: US1
4. **STOP AND VALIDATE**: quickstart V1 — the icon is the number only
5. Ship/demo if ready: frame removed, even though the number is still small

### Incremental Delivery

1. Setup + Foundational → measurement seam ready, nothing rendered differently yet
2. Add US1 → validate V1 → **MVP: frame gone**
3. Add US2 → validate V2–V4 → **number large, centered, unclipped**
4. Add US3 → validate V5–V9 → **scope proven clean**
5. Polish → V1–V10 + T014 report

### Known scope note

Only one function body changes in production. Do **not** "helpfully" extract a sizing helper,
add a second generator, bump the default canvas size, or touch `generateIcon()` — research
Decisions 5–7 evaluated and rejected each of those.

---

## Notes

- [P] = different files, no dependencies on incomplete tasks
- [Story] label maps task to a user story for traceability
- Each user story is independently completable and testable via its quickstart scenario
- Confirm tests fail before implementing; commit after each task or logical group
- Stop at any checkpoint to validate a story alone
- Avoid: vague tasks, same-file conflicts, cross-story dependencies that break independence
