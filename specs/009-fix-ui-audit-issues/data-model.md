# Data Model: Fix UI Audit Issues

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

## Persisted Preferences (NotificationPreferences, SharedPreferences)

File: `notification_preferences` (existing). New keys added alongside the
existing calendar-type and snooze keys.

| Preference | Key | Type | Default | Rule |
|------------|-----|------|---------|------|
| Accent color index | `accent_color_index` | Int | 0 | FR-004, SP-003 |
| First day Monday | `first_day_monday` | Boolean | false | FR-004, SP-003 |
| Show week numbers | `show_week_numbers` | Boolean | false | FR-004, SP-003 |
| Theme name | `theme_name` | String | "Liquid Glass (Dark)" | FR-004, SP-003 |
| Category Personal visible | `category_personal_visible` | Boolean | true | FR-002, SP-002 |
| Category Work visible | `category_work_visible` | Boolean | true | FR-002, SP-002 |
| Category Holidays visible | `category_holidays_visible` | Boolean | true | FR-002, SP-002 |

Each gets a `get<Name>(context)` returning the stored value or default, and a
`set<Name>(context, value)` that writes via `.edit().apply()` — mirroring
`getSnoozeMinutes`/`setSnoozeMinutes`.

## ViewModel State (LumaViewModel)

| State | Change |
|-------|--------|
| `_accentColorIndex` | init reads `getAccentColorIndex`; setter persists |
| `_firstDayMonday` | init reads `getFirstDayMonday`; setter persists |
| `_showWeekNumbers` | init reads `getShowWeekNumbers`; setter persists |
| `_themeName` | init reads `getThemeName`; setter persists |
| `_calendarPersonalVisible` | init reads pref; toggle persists |
| `_calendarWorkVisible` | init reads pref; toggle persists |
| `_calendarHolidaysVisible` | init reads pref; toggle persists |
| `visibleEvents` (NEW) | `combine(allEvents, personal, work, holidays)` → filtered list |
| `selectedDateEvents` | derive from `visibleEvents` (was `allEvents`) |
| `filteredSearchResults` | UNCHANGED — stays on `allEvents` (FR-002a) |

### visibleEvents filter logic

```
combine(allEvents, personalVisible, workVisible, holidaysVisible) { events, p, w, h ->
  events.filter { event ->
    when (event.calendarType) {
      "Personal" -> p
      "Work"     -> w
      "Holidays" -> h
      else       -> true   // unknown/other types remain visible
    }
  }
}
```

**Note**: Events with a `calendarType` outside the three known values remain
visible (defensive default) so no events silently vanish.

## Entities (unchanged schema)

- **CalendarEvent** (Room): `calendarType` field ("Personal"/"Work"/"Holidays")
  drives visibility filtering; `title` subject to non-blank validation. No Room
  schema change — no migration.

## Validation Rules

| Rule | Field | Validation | Source |
|------|-------|------------|--------|
| VB-001 | event title | `title.isBlank()` (empty or whitespace, incl. Unicode) → reject | FR-009 |
| VB-002 | validation message | localized via AppStrings | FR-009 |
| VB-003 | valid title | trimmed, saved normally | FR-010 |
| VR-001 | accent index | clamp/guard to valid AccentPresets range on read | FR-006 |
| VR-002 | theme name | fall back to default if unknown | FR-006 |
