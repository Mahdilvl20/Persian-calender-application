# Preferences & State Contract

**Date**: 2026-09-22
**Spec**: [spec.md](../spec.md)

## NotificationPreferences additions

Each new preference follows the existing `getSnoozeMinutes`/`setSnoozeMinutes`
contract: a getter returning stored-or-default, a setter writing via
`.edit().apply()`. No second persistence mechanism.

```
getAccentColorIndex(context): Int            // default 0
setAccentColorIndex(context, index: Int)

getFirstDayMonday(context): Boolean          // default false
setFirstDayMonday(context, value: Boolean)

getShowWeekNumbers(context): Boolean         // default false
setShowWeekNumbers(context, value: Boolean)

getThemeName(context): String                // default "Liquid Glass (Dark)"
setThemeName(context, name: String)

getCategoryPersonalVisible(context): Boolean // default true
setCategoryPersonalVisible(context, value: Boolean)
getCategoryWorkVisible(context): Boolean     // default true
setCategoryWorkVisible(context, value: Boolean)
getCategoryHolidaysVisible(context): Boolean // default true
setCategoryHolidaysVisible(context, value: Boolean)
```

**Invariants**:
- First run (no stored value) → returns the documented default.
- Setters persist immediately; a subsequent get returns the written value.
- Existing keys (calendar type, snooze, notification enables) unchanged.

## LumaViewModel state contract

**Invariants**:
- Each appearance/visibility StateFlow is initialized from its preference getter.
- Each setter/toggle: (1) writes through NotificationPreferences, (2) updates the
  StateFlow. Order such that a restart reproduces the last in-session value.
- `visibleEvents` MUST reflect the three visibility flags reactively (no restart).
- `selectedDateEvents` derives from `visibleEvents`.
- `filteredSearchResults` derives from `allEvents` (unfiltered by visibility).

## CalendarScreen wiring contract (MainActivity)

- `CalendarScreen(events = visibleEvents, ...)` — was `allEvents`.
- Category-visibility flags are consumed by the ViewModel filter, not passed to
  CalendarScreen for rendering decisions.

## AddEditEventSheet validation contract

- Save action is blocked (no `onSave` invocation) when `title.isBlank()`.
- On blank attempt: show localized inline validation feedback.
- On valid save: pass `title.trim()`; the `ifBlank { newEvent }` substitution is
  removed.
- Applies identically to Add and Edit.

## SettingsScreen dialog contract

- "Reset sample data" and "Clear all data" each open a confirmation dialog
  (existing glass pattern) before invoking their callback.
- Cancel/dismiss → no callback invoked, no data change.
- Confirm → existing `resetToSampleData` / `clearAllData` invoked unchanged.

## Snooze description contract

- The description text names exactly the selectable chip values. No change to
  snooze persistence or selectable set.
