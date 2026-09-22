# Tasks: Fix Appearance & Theme Controls

**Input**: Design documents from `/specs/010-fix-appearance-theme/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/appearance-contract.md, quickstart.md

**Tests**: Included — explicitly requested by spec SC-008 and the feature request
("Add/update tests proving: Liquid Glass changes the effective theme, OLED Deep changes the
effective theme, accent changes the effective accent, both survive restart, UI reacts
immediately, existing settings unaffected").

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Single-module Android app. All source paths are under
`app/src/main/java/com/aistudio/lumacalendar/vtxk/` and all tests under
`app/src/test/java/com/aistudio/lumacalendar/vtxk/`. Paths below are written in full.

---

## Phase 1: Setup

**Purpose**: Establish a green baseline before touching appearance code.

- [X] T001 Run `./gradlew test` and `./gradlew assembleDebug` from the repository root to establish a green baseline before any changes

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the single appearance mechanism that US1 and US2 both consume.

**CRITICAL**: No user story work can begin until this phase is complete. Theme and accent
share one resolver and one provider (contract §2, §3) — building it twice would create the
second state that FR-011 forbids.

> **NOTE: Write the tests FIRST and confirm they FAIL before implementing.**

- [X] T002 [P] Create resolver tests in app/src/test/java/com/aistudio/lumacalendar/vtxk/AppearanceTest.kt asserting (a) `resolveAppearance("Liquid Glass (Dark)", 0)` and `resolveAppearance("OLED Deep", 0)` produce **different** canvas/surface/border/glow values, (b) each of the 5 accent indices produces a distinct accent primary equal to `AccentPresets[i].primary`, with `secondary` equal to `AccentPresets[i].secondary`, (c) VA-003 purity — the same inputs always yield equal output and resolving one input never changes a previously resolved value, (d) baseline stability — index 0 reproduces today's hardcoded constants exactly. Must fail until T004 exists.
- [X] T003 [P] Extend app/src/test/java/com/aistudio/lumacalendar/vtxk/UiAuditFixesTest.kt with an appearance round-trip assertion: write `theme_name` and `accent_color_index` through `NotificationPreferences`, read them back through the resolver, and assert the effective appearance matches, plus assert first-run defaults (`"Liquid Glass (Dark)"`, `0`) and that `active_calendar_type`, `daily_notification_snooze_minutes`, `daily_calendar_notification_enabled`, `event_reminders_enabled`, `notification_permission_requested`, `category_personal_visible`, `category_work_visible`, `category_holidays_visible`, `first_day_monday`, `show_week_numbers` are all unchanged (FR-016, SC-006)
- [X] T004 Create app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/theme/Appearance.kt containing: `LumaAppearance` (theme, accentPrimary, accentSecondary, canvas tokens, glass surface tokens, glass border tokens, ambient glow intensity); `resolveAppearance(themeName: String, accentIndex: Int): LumaAppearance` implementing VA-001 (unknown theme → `LIQUID_GLASS`), VA-002 (accent index clamped to `0..AccentPresets.lastIndex`), Decision 3 (OLED Deep = near-black canvas, surfaces one step lower, borders subtler, glow `0.18`; Liquid Glass = current values, glow `0.35`), Decision 4 (category/destructive/text colors excluded), Decision 2 (`primary`→`AccentElectricBlue` role, `secondary`→`AccentRoyalViolet` role); and `LocalLumaAppearance` CompositionLocal defaulted to `resolveAppearance("Liquid Glass (Dark)", 0)` (depends on T002)
- [X] T005 Update app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/theme/Theme.kt so `LumaCalendarTheme` takes `themeName: String` and `accentColorIndex: Int`, calls `resolveAppearance`, and wraps content in `CompositionLocalProvider(LocalLumaAppearance provides …)`; also build `LumaDarkColorScheme` from the resolved accent (`primary` = accentPrimary, `primaryContainer` = accentSecondary) so any Material color-scheme read is correct (FR-012, depends on T004)
- [X] T006 Update the `LumaCalendarTheme` call in app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt (line ~86) to pass the already-collected `themeName` and `accentIndex`; leave the existing `AmbientBackground(accentGlow = currentAccent, isOled = isOledTheme)` call untouched until T007 (depends on T005)

**Checkpoint**: Appearance mechanism in place and compile-clean; existing screens still render exactly as before (no consumer reads the local yet). Resolver tests now pass.

---

## Phase 3: User Story 1 — Theme Mode Actually Applies (Priority: P1) 🎯 MVP

**Goal**: Every theme-dependent token (canvas, glass surfaces, glass borders, ambient glow) is read from the resolved appearance, so Liquid Glass and OLED Deep are visibly different across the app.

**Independent Test**: quickstart **V1** — Settings → Appearance & Theme → tap OLED Deep → backdrop goes near-black and glow visibly reduces on that same screen, no restart; tap Liquid Glass → gradient and stronger glow return; exactly one chip shows the selected indicator.

**Constraint quoted from data-model.md / research Decision 3 & 4**: theme-dependent tokens are `CanvasBlack`, `CanvasNavy`, `CanvasSurface`, `GlassSurface{UltraLight,Default,Highlight,Elevated,Pressed}`, `GlassBorder{Subtle,Default,Bright}` and ambient glow intensity. **`Category{Work,Personal,Health,Social,Finance,Special}` used as category markers, `CategorySpecial` used as the destructive/error color, and all `TextWhite*` text colors MUST NOT be themed.**

### Implementation for User Story 1

- [X] T007 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/GlassComponents.kt read `LocalLumaAppearance.current` and replace every themed `Canvas*`/`GlassSurface*`/`GlassBorder*` reference inside `AmbientBackground`, `GlassCard`, `GlassButton`, `GlassIconButton`, `FloatingGlassActionButton`, `CalendarCell`, `EventCard`, `HolidayCard`, `GlassInput`, `CategoryChip`, `GlassTabBar`, `GlassToggle`, `SectionHeader` with the appearance token; drive `AmbientBackground`'s glow intensity from the appearance instead of the `isOled` parameter and drop the now-dead `accentGlow`/`isOled` parameters, updating the call site in app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt accordingly (FR-012, FR-018)
- [X] T008 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/CalendarScreen.kt replace the 13 themed `Canvas*`/`GlassSurface*` and 14 `GlassBorder*` references with appearance tokens (FR-001/FR-002)
- [X] T009 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/SettingsScreen.kt replace the themed `GlassSurface*` reference (line ~428) and the 11 `GlassBorder*` references with appearance tokens; do not move, relabel, or restyle any control (FR-018)
- [X] T010 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/SearchScreen.kt replace the themed `GlassSurfaceUltraLight` reference (line ~134, passed to `surfaceColor`) with the appearance token (FR-001/FR-002)
- [X] T011 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/AddEditEventSheet.kt replace the 11 themed `Canvas*`/`GlassSurface*` and 10 `GlassBorder*` references with appearance tokens (FR-001/FR-002)
- [X] T012 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/EventDetailSheet.kt replace the 11 themed `Canvas*`/`GlassSurface*` and 10 `GlassBorder*` references with appearance tokens, leaving `CategorySpecial` as the destructive color (FR-001/FR-002, Decision 4)
- [X] T013 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/ConfirmActionDialog.kt replace the 4 themed `Canvas*`/`GlassSurface*` and 4 `GlassBorder*` references with appearance tokens, leaving `CategorySpecial` as the destructive color (FR-001/FR-002, Decision 4)
- [X] T014 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/LiquidGlassTimePickerDialog.kt replace the 15 themed `Canvas*`/`GlassSurface*` and 11 `GlassBorder*` references with appearance tokens (FR-001/FR-002)
- [X] T015 [P] [US1] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/ManualDateInputDialog.kt replace the 7 themed `Canvas*`/`GlassSurface*` and 9 `GlassBorder*` references with appearance tokens (FR-001/FR-002)

**Checkpoint**: Theme switching is visibly effective app-wide (quickstart V1) with no accent change yet.

---

## Phase 4: User Story 2 — Accent Color Actually Applies (Priority: P1)

**Goal**: Every accent-bearing render site reads the resolved accent pair instead of a fixed constant, so selecting a circle changes the app's accent everywhere, with a clear selected indicator.

**Independent Test**: quickstart **V2** — tap each of the 5 circles; the rendered accent on Settings, Calendar, and Search changes immediately; exactly one circle shows the selected indicator; all 5 respond (zero inert controls).

**Constraint quoted from research Decision 2 & data-model.md**: `AccentPreset.primary` replaces the `AccentElectricBlue` role (icons, text accents, selected borders/rings, FAB, tab highlight, chip selection); `AccentPreset.secondary` replaces the `AccentRoyalViolet` role (gradient partner in primary buttons/FAB, container tint). Index 0 must reproduce today's appearance exactly. **Category colors and the destructive `CategorySpecial` color MUST NOT follow the accent.**

> Sequential dependency: US2 touches the same files as US1, so it runs after Phase 3.

### Implementation for User Story 2

- [X] T016 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/GlassComponents.kt replace the 15 `AccentElectricBlue` and 11 `AccentRoyalViolet` references with the appearance's accentPrimary/accentSecondary — including the `GlassButton` primary gradient, `FloatingGlassActionButton`, `GlassTabBar` highlight, `GlassToggle`, `CalendarCell` selection, `EventCard`, `HolidayCard`, `GlassInput`, `CategoryChip` selection, `SectionHeader` (FR-007)
- [X] T017 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/CalendarScreen.kt replace the 8 `AccentElectricBlue` and 4 `AccentRoyalViolet` references with the appearance's accent pair (FR-007)
- [X] T018 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/SettingsScreen.kt replace the 19 `AccentElectricBlue` and 4 `AccentRoyalViolet` references with the appearance's accent pair, and strengthen the selected accent circle's indicator (currently a 2dp white ring on a 26dp circle) into a clearly readable ring plus outer halo without changing the row layout, labels, order, or positions (FR-007, FR-008, Decision 5, FR-018)
- [X] T019 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/screens/SearchScreen.kt replace the `AccentElectricBlue` references at lines ~110 and ~179 with the appearance's accentPrimary (FR-007)
- [X] T020 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/AddEditEventSheet.kt replace the 10 `AccentElectricBlue` and 3 `AccentRoyalViolet` references with the appearance's accent pair (FR-007)
- [X] T021 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/EventDetailSheet.kt replace the 7 `AccentElectricBlue` and 2 `AccentRoyalViolet` references with the appearance's accent pair, leaving `CategorySpecial` as the destructive color (FR-007, Decision 4)
- [X] T022 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/LiquidGlassTimePickerDialog.kt replace the 17 `AccentElectricBlue` and 3 `AccentRoyalViolet` references with the appearance's accent pair (FR-007)
- [X] T023 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/ManualDateInputDialog.kt replace the 13 `AccentElectricBlue` and 2 `AccentRoyalViolet` references with the appearance's accent pair (FR-007)
- [X] T024 [P] [US2] In app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/theme/Type.kt the two accent-colored styles `VazirmatnTypography.importantDate` and `VazirmatnTypography.highlightedInfo` still hardcode `color = AccentElectricBlue`; these are plain non-composable `TextStyle` values and cannot read a CompositionLocal, so apply the appearance's accentPrimary at their use sites (or convert them to a composable accessor) — do not leave them reading the fixed token and do not change any other typography property (FR-007, FR-012, FR-018)

**Checkpoint**: Accent switching is visibly effective app-wide (quickstart V2); theme switching from US1 still works.

---

## Phase 5: User Story 3 — Immediate App-Wide Propagation (Priority: P2)

**Goal**: Guarantee no consumer still reads a fixed token and nothing caches an appearance value, so changes propagate immediately to every already-rendered screen and open surface.

**Independent Test**: quickstart **V3** — change accent in Settings, switch **directly** to Calendar and Search without returning through Settings, and confirm the new accent is already applied; open the Add Event sheet, the time picker, and the Clear-all confirmation dialog while changed and confirm they follow.

### Implementation for User Story 3

- [X] T025 [P] [US3] Audit `app/src/main` for remaining fixed reads of `AccentElectricBlue`, `AccentRoyalViolet`, `CanvasBlack`, `CanvasNavy`, `CanvasSurface`, `GlassSurface*`, `GlassBorder*` outside app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/theme/Color.kt, outside Appearance.kt, and outside the `AccentPresets` list in app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/viewmodel/LumaViewModel.kt; convert any remaining render site to the appearance token, and for any intentional exception record it as a Decision 4 semantic color (category marker, destructive/error color, or `TextWhite*`) (FR-012, SC-004)
- [X] T026 [P] [US3] Audit `app/src/main` for appearance values cached outside composition — any `remember`/top-level `val`/object property holding a resolved `Color` or `LumaAppearance` that is not recomputed when the selection changes; stale caches would break immediate propagation (FR-012, SC-004)
- [X] T027 [US3] Verify in app/src/main/java/com/aistudio/lumacalendar/vtxk/MainActivity.kt that the `LumaCalendarTheme` provider encloses the whole tab `Crossfade` and therefore every `Dialog` tree (Add/Edit sheet, event detail, confirmation, time picker, manual date) so open surfaces receive the current appearance (FR-012, SC-004)

**Checkpoint**: quickstart V3 passes — propagation needs no navigation or restart.

---

## Phase 6: User Story 4 — No Regression to Existing Behavior (Priority: P3)

**Goal**: Prove appearance work touched nothing outside it.

**Independent Test**: quickstart **V7**, **V8**, **V9** — other settings retain values, RTL/LTR and localization unchanged, calendar/notifications/reminders/events behave as before.

### Implementation for User Story 4

- [X] T028 [P] [US4] Review `git diff` and confirm no changes under app/src/main/java/com/aistudio/lumacalendar/vtxk/data/, app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/, app/src/main/java/com/aistudio/lumacalendar/vtxk/util/Localization.kt, app/src/main/java/com/aistudio/lumacalendar/vtxk/util/CalendarConverter.kt, app/src/main/java/com/aistudio/lumacalendar/vtxk/util/DateUtils.kt, or app/src/main/java/com/aistudio/lumacalendar/vtxk/widget/; no new strings added to `AppStrings` and no `LocalLayoutDirection`/RTL change (FR-017, FR-018, SC-007, SC-009)
- [X] T029 [P] [US4] Review `git diff` and confirm app/src/main/AndroidManifest.xml, app/build.gradle, and .github/ are untouched and that no new storage, key, migration, or database was introduced (FR-015, FR-016, SC-006)

**Checkpoint**: Scope confirmed limited to appearance wiring.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T030 [P] Run `./gradlew test` from the repository root and verify all new appearance tests plus every pre-existing test pass (SC-008)
- [X] T031 [P] Run `./gradlew clean assembleDebug` from the repository root and verify the build succeeds (SC-008)
- [X] T032 Run quickstart.md scenarios V1–V10 on a device or emulator and record each outcome; note that "UI reacts immediately" (FR-004/FR-009) is verified here as V3 because no Compose UI-test harness exists (research Decision 6)
- [X] T033 Produce the implementation report required by the feature request: root cause, files changed, tests added/updated, test results, build result

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: none — start immediately
- **Foundational (Phase 2)**: after Setup — **BLOCKS every user story** (one resolver + one provider for both US1 and US2; building them separately would create the second state FR-011 forbids)
- **US1 (Phase 3)**: after Foundational. All 9 tasks are on distinct files → parallel.
- **US2 (Phase 4)**: after US1 — **same files as US1**, so it must be serialized behind Phase 3. All 9 tasks are on distinct files → parallel within the phase.
- **US3 (Phase 5)**: after US2 — it audits the whole tree for leftovers from both stories.
- **US4 (Phase 6)**: after US2 (it reviews the completed diff). Independent of US3.
- **Polish (Phase 7)**: after all stories.

### User Story Dependencies

- **US1 (P1)**: starts after Foundational; no dependency on other stories.
- **US2 (P1)**: starts after Foundational *logically*, but shares files with US1 → execute after US1. Independently testable once its own sites are converted.
- **US3 (P2)**: after US2; it is a verification story over the combined result.
- **US4 (P3)**: after US2; independent of US3.

### Within Each User Story

- Tests are written and confirmed failing before the implementation they cover (T002/T003 → T004)
- Provider before consumers
- Theme consumers before accent consumers (file contention)
- Per-story audit before moving to the next priority

### Parallel Opportunities

- **T002 ∥ T003** — different test files
- **T007–T015 all in parallel** — nine distinct files in US1
- **T016–T024 all in parallel** — nine distinct files in US2 (after Phase 3 completes)
- **T025 ∥ T026** — independent audits in US3
- **T028 ∥ T029** — independent diff reviews in US4
- **T030 ∥ T031** — test vs. build

### File-contention notes (why some phases cannot overlap)

- `GlassComponents.kt`: T007 (US1) → T016 (US2)
- `CalendarScreen.kt`: T008 → T017
- `SettingsScreen.kt`: T009 → T018
- `SearchScreen.kt`: T010 → T019
- `AddEditEventSheet.kt`: T011 → T020
- `EventDetailSheet.kt`: T012 → T021
- `LiquidGlassTimePickerDialog.kt`: T014 → T022
- `ManualDateInputDialog.kt`: T015 → T023
- `MainActivity.kt`: T006 (Foundational) → T007 (US1) — no US2 task touches it
- `Type.kt`: T024 only (US2) — no contention
- `ConfirmActionDialog.kt`: T013 only (US1 has no accent sites — verified)

---

## Parallel Example: User Story 1

```bash
# Launch all nine theme-consumer conversions together (distinct files):
Task: "GlassComponents.kt themed Canvas*/GlassSurface*/GlassBorder* + AmbientBackground"
Task: "CalendarScreen.kt 13 surface + 14 border"
Task: "SettingsScreen.kt surface + 11 border"
Task: "SearchScreen.kt GlassSurfaceUltraLight"
Task: "AddEditEventSheet.kt 11 surface + 10 border"
Task: "EventDetailSheet.kt 11 surface + 10 border"
Task: "ConfirmActionDialog.kt 4 surface + 4 border"
Task: "LiquidGlassTimePickerDialog.kt 15 surface + 11 border"
Task: "ManualDateInputDialog.kt 7 surface + 9 border"
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Complete Phase 1: baseline green
2. Complete Phase 2: Foundational — one resolver, one provider, tests passing (CRITICAL)
3. Complete Phase 3: US1
4. **STOP AND VALIDATE**: quickstart V1 — theme visibly changes, one chip selected, persists per V4
5. Ship/demo if ready — the app now has a working theme switcher

### Incremental Delivery

1. Setup + Foundational → mechanism ready, appearance unchanged (index 0 = today's colors)
2. Add US1 → validate V1 → **MVP: theme works**
3. Add US2 → validate V2 + V6 → **accent works, selected state clear**
4. Add US3 → validate V3 → **propagation guaranteed**
5. Add US4 → validate V7–V9 → **non-regression proven**
6. Polish → V1–V10 + T033 report

### Known scope note

~198 call-site replacements are mechanical but unavoidable: `MaterialTheme.colorScheme` is
read 0 times today, so no single indirection point exists (research Decision 1). Do not
"optimize" this by mutating global color vars — that alternative was evaluated and rejected.

---

## Notes

- [P] = different files, no dependencies on incomplete tasks
- [Story] label maps task to a user story for traceability
- Each story is independently completable and testable via its quickstart scenario
- Commit after each task or logical group; stop at any checkpoint to validate a story alone
- Avoid: vague tasks, same-file conflicts, cross-story dependencies that break independence
