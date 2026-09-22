# Appearance & Theme Contract

**Date**: 2026-09-22
**Spec**: [spec.md](../spec.md) · **Data**: [data-model.md](../data-model.md)

This is the UI contract for the Appearance & Theme controls: what the persistence layer
promises, how a stored choice becomes rendered color, and what must never change.

## 1. Preference contract (reused, not extended)

Existing `NotificationPreferences` getters/setters, unchanged names and behavior:

```
getThemeName(context): String            // default "Liquid Glass (Dark)"
setThemeName(context, name: String)

getAccentColorIndex(context): Int        // default 0
setAccentColorIndex(context, index: Int)
```

**Invariants**
- First run (no stored value) → returns the documented default.
- A setter persists immediately; a subsequent get returns exactly the written value
  (no clamping or rewriting inside the getter).
- Existing keys — calendar type, snooze, notification enables, category visibility,
  first-day, week-numbers — are never written by this feature.
- Resolution reads these two values and writes nothing.

## 2. Appearance resolution contract

```
resolveAppearance(themeName: String, accentIndex: Int) -> LumaAppearance
```

**Invariants**
- **Pure**: no Context, no I/O, no ambient state; same inputs → same output (VA-003).
- **Non-mutating**: resolving for one call never alters a previously returned value.
- **Never fails**: unknown `themeName` → `LIQUID_GLASS` (VA-001); out-of-range `accentIndex`
  → clamped into `0..AccentPresets.lastIndex` (VA-002).
- **Baseline-stable**: `resolveAppearance("Liquid Glass (Dark)", 0)` yields tokens
  identical to today's hardcoded constants — upgrading users with default settings see no
  visual change (FR-013, SC-006).
- Output carries: theme, accent primary, accent secondary, canvas tokens, glass surface
  tokens, glass border tokens, ambient glow intensity.

## 3. Composition/reactivity contract

- Exactly **one** provider exists, inside `LumaCalendarTheme`, fed by the values `MainActivity`
  already collects from `themeName` / `accentColorIndex`.
- The provider is read during composition, so a StateFlow change recomposes every consumer.
- **No consumer may read an appearance value from anywhere else** — no second provider, no
  cached copy, no local `remember` of a color (FR-011).
- Consequences guaranteed by the above: a change is visible on the current screen immediately,
  on other already-rendered screens without navigation, and on any open sheet/dialog
  (FR-004, FR-009, FR-012).

## 4. Token role contract

**Theme-dependent** (value depends on the resolved theme):
`CanvasBlack`, `CanvasNavy`, `CanvasSurface`,
`GlassSurface{UltraLight,Default,Highlight,Elevated,Pressed}`,
`GlassBorder{Subtle,Default,Bright}`, ambient glow intensity.

**Accent-dependent** (value depends on the resolved accent):

| Role | Replaces | Typical use |
|------|----------|-------------|
| accent primary | `AccentElectricBlue` | icons, text accents, selected borders/rings, FAB, tab highlight, chip selection |
| accent secondary | `AccentRoyalViolet` | gradient partner in primary buttons/FAB, container tint, ambient default |

**Never themed** (fixed regardless of selection):
`Category{Work,Personal,Health,Social,Finance,Special}` used as category markers,
`CategorySpecial` used as the destructive/error color, and all `TextWhite*` text colors.

**Rule**: an accent-bearing render site reads the themed token. A fixed token that is
semantic stays literal. A site that reads `AccentElectricBlue`/`AccentRoyalViolet` directly
after this change is a defect.

## 5. Selected-state contract

- Exactly **one** theme option and exactly **one** accent circle show a selected indicator at
  all times, and each matches what is actually rendered (FR-003, FR-008, SC-005).
- Indicators update as part of the same recomposition as the appearance change — never
  lagging it.
- Every displayed accent circle is clickable and performs a selection (FR-006, SC-002).

## 6. Non-regression contract

- No new storage, key, migration, or database (FR-015).
- RTL/LTR direction and localized strings unchanged (FR-017).
- Settings layout, labels, order, and control positions unchanged (FR-018).
- Calendar navigation, Today-vs-selected, notifications, reminders, and event
  create/edit/delete unchanged (FR-018, SC-009).
- `AccentPresets` roster and theme chip labels/order unchanged.
