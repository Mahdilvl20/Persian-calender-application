# Research: Fix UI Audit Issues

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

All facts below were verified directly from the current source.

## Decision 1: Category filtering via a reactive combined flow

**Decision**: Add a `visibleEvents: StateFlow<List<CalendarEvent>>` in
`LumaViewModel` that `combine`s `allEvents` with the three visibility flags
(`_calendarPersonalVisible`, `_calendarWorkVisible`, `_calendarHolidaysVisible`),
filtering by the event `calendarType` field. Pass `visibleEvents` to
`CalendarScreen` in `MainActivity` in place of `allEvents`.

**Rationale**: `LumaViewModel` already uses this exact pattern for
`selectedDateEvents` and `filteredSearchResults` (StateFlow `combine` +
`stateIn`). Filtering by `calendarType` matches the field the toggles are named
after ("Personal"/"Work"/"Holidays" — CalendarEvent.kt:19). Reactive by
construction (FR-002).

**Alternatives considered**:
- Filter inside CalendarScreen composable — rejected: pushes business logic into
  UI, violates single-ViewModel simplicity.
- Filter by `category` field — rejected: `category` holds Work/Personal/Health/
  Social/Finance; the toggles are Personal/Work/Holidays, matching `calendarType`.

**Note**: `selectedDateEvents` currently derives from `allEvents`. It should
derive from `visibleEvents` too so the day-detail list respects visibility,
consistent with FR-001 ("all calendar views"). Search's `filteredSearchResults`
stays on `allEvents` (FR-002a).

## Decision 2: Persistence via NotificationPreferences

**Decision**: Extend the existing `NotificationPreferences` object
(`EventNotifications.kt:27`) with keys + get/set for: accent color index (Int),
first-day-Monday (Boolean), show-week-numbers (Boolean), theme name (String),
and the three category-visibility booleans. `LumaViewModel` reads them in field
initializers (like `_snoozeMinutes` already does) and each setter writes through
`NotificationPreferences` then updates the StateFlow.

**Rationale**: `NotificationPreferences` is the project's single SharedPreferences
mechanism (file `notification_preferences`), already storing calendar type and
snooze. Reusing it satisfies FR-005 (no second mechanism). The
getSnoozeMinutes/setSnoozeMinutes pair is the exact template.

**Alternatives considered**:
- Jetpack DataStore — rejected: would introduce a second persistence mechanism
  (FR-005 forbids).
- A new SharedPreferences file — rejected: same objection; unnecessary.

**Defaults** (preserve current behavior on first run): accent index `0`,
firstDayMonday `false`, showWeekNumbers `false`, theme `"Liquid Glass (Dark)"`,
all three category flags `true`.

## Decision 3: Persian loading state

**Decision**: Remove the unused `isPersianLoading` parameter path OR surface a
minimal design-consistent indicator. The spec (US5, P3, FR-011) accepts either.
Recommend **surfacing a subtle indicator** only if it's low-risk; otherwise
remove the dead parameter. Final choice deferred to implementation, but the
lower-risk default is removal of the dead parameter (no behavioral regression).

**Rationale**: `isPersianLoading` is declared/passed but never read
(CalendarScreen.kt:112). Either path satisfies FR-011; removal is the smallest
correct change.

## Decision 4: Confirmation dialogs

**Decision**: Reuse the existing confirmation dialog pattern already used by the
event delete flow in `EventDetailSheet.kt` (glass-styled dialog with
Cancel/Confirm). Gate `resetToSampleData` and `clearAllData` behind it in
`SettingsScreen`.

**Rationale**: FR-008 requires the existing Liquid Glass dialog pattern; the
delete-confirm dialog is the in-repo precedent. Localized labels via AppStrings.

## Decision 5: Blank-title validation

**Decision**: In `AddEditEventSheet`, block save when `title.isBlank()`
(covers empty + whitespace, including Unicode/Persian whitespace via Kotlin
`isBlank()`), show localized inline validation feedback, and remove the
`ifBlank { strings.newEvent }` substitution (AddEditEventSheet.kt:226). Trim the
title before passing to `onSave`.

**Rationale**: FR-009/FR-010, VB-001..003. Kotlin `String.isBlank()` treats all
Unicode whitespace as blank, satisfying the Persian-whitespace edge case.

## Decision 6: Snooze description

**Decision**: Align the descriptive label (SettingsScreen.kt:341, lists
`15,30,45,60,90,120,180,240,480`) with the rendered chips (SettingsScreen.kt:407,
`15,30,60,120,240,480`). Change the description text to match the chips (do not
change persistence). Keep it simple — no logic change (FR-012).

**Rationale**: Cosmetic mismatch only. Editing the description string is the
minimal fix and avoids altering the working snooze persistence.
