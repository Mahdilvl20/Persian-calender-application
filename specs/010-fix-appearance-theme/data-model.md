# Data Model: Fix Appearance & Theme Controls

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

## Persisted Preferences (unchanged — reused from feature 009)

File: `notification_preferences` (existing SharedPreferences). **No keys added, none renamed.**

| Preference | Key | Type | Default | Rule |
|------------|-----|------|---------|------|
| Theme name | `theme_name` | String | `Liquid Glass (Dark)` | FR-005, FR-014, FR-016 |
| Accent color index | `accent_color_index` | Int | `0` | FR-010, FR-014, FR-016 |

Read/Write contract is the existing `getX(context)` / `setX(context, value)` pair that
returns stored-or-default and persists via `.edit().apply()` — same as `getSnoozeMinutes`.
Invariant: **a subsequent get returns exactly what was written** (so the resolver never has to
guess), and first run returns the documented default.

Untouched sibling keys that must survive any appearance change: `active_calendar_type`,
`daily_notification_snooze_minutes`, `daily_calendar_notification_enabled`,
`event_reminders_enabled`, `notification_permission_requested`,
`category_personal_visible`, `category_work_visible`, `category_holidays_visible`,
`first_day_monday`, `show_week_numbers`.

## Derived Appearance (not stored — resolved on read)

| Field | Source | Values |
|-------|--------|--------|
| Theme | `theme_name` | `LIQUID_GLASS` \| `OLED_DEEP` |
| Accent primary | `accent_color_index` → `AccentPresets[i].primary` | one of 5 |
| Accent secondary | `accent_color_index` → `AccentPresets[i].secondary` | one of 5 |
| Canvas tokens | theme | Liquid Glass = existing `Canvas*`; OLED Deep = near-black |
| Glass surface tokens | theme | Liquid Glass = existing alphas; OLED Deep = one step lower lift |
| Glass border tokens | theme | Liquid Glass = existing alphas; OLED Deep = one step subtler |
| Ambient glow intensity | theme | Liquid Glass = `0.35`; OLED Deep = `0.18` |

Derived values are **never written to storage** — they are recomputed from the two stored
values, so storage stays the single source of truth (FR-011, FR-015).

### Accent preset roster (existing, unchanged)

| Index | Name | primary → | secondary → |
|-------|------|-----------|-------------|
| 0 | Electric Blue | `AccentElectricBlue` | `AccentRoyalViolet` |
| 1 | Royal Violet | `AccentRoyalViolet` | `AccentDeepViolet` |
| 2 | Neon Cyan | `AccentCyan` | `AccentElectricBlue` |
| 3 | Emerald Mint | `CategoryHealth` | `AccentCyan` |
| 4 | Radiant Coral | `CategorySpecial` | `AccentRoyalViolet` |

Index 0 must reproduce today's hardcoded appearance exactly (no visual regression on upgrade).

### Theme option roster (existing labels, unchanged)

| Stored value | Resolves to |
|--------------|-------------|
| `Liquid Glass` / `Liquid Glass (Dark)` / anything containing `Liquid Glass` | `LIQUID_GLASS` |
| `OLED Deep` / anything containing `OLED` | `OLED_DEEP` |
| anything else | `LIQUID_GLASS` (default, FR-014) |

## Fixed (non-themed) tokens

These are semantic and MUST NOT follow theme or accent selection:

- **Category colors**: `CategoryWork`, `CategoryPersonal`, `CategoryHealth`, `CategorySocial`,
  `CategoryFinance`, `CategorySpecial` — when used as a *category* marker.
- **Destructive/error color**: `CategorySpecial` in `ConfirmActionDialog` and the
  `EventDetailSheet` delete confirmation.
- **Text colors**: `TextWhitePrimary`, `TextWhiteSecondary`, `TextWhiteMuted`,
  `TextDarkPlaceholder`.

Caveat: `CategoryHealth` and `CategorySpecial` also appear as `AccentPreset` values (rows 3–4
above). That is palette data, not a render site, and is left as-is.

## Validation Rules

| Rule | Field | Validation | Source |
|------|-------|-----------|--------|
| VA-001 | theme name | unknown → default `LIQUID_GLASS` | FR-014, FR-013 |
| VA-002 | accent index | outside `0..AccentPresets.lastIndex` → clamp to nearest valid | FR-014, FR-013 |
| VA-003 | resolution purity | same inputs always yield the same appearance; resolving one input never mutates a previously resolved value | FR-011, SC-005 |
| VA-004 | stored key set | resolution writes nothing; `set` round-trips to the same `get` | FR-015, FR-016 |

## Entities (no schema change)

- **`AccentPreset`** (existing, `LumaViewModel.kt`): `name`, `primary`, `secondary`. Unchanged.
- **`CalendarEvent`** (Room): untouched — no migration.
- **Appearance preferences**: two existing keys above. No new entity, no new storage.
