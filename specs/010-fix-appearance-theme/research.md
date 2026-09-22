# Research: Fix Appearance & Theme Controls

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

All facts below were verified directly from the current source.

## Finding 0 — Where the chain actually breaks (pre-work investigation)

Traced `SettingsScreen → LumaViewModel → NotificationPreferences → theme tokens → rendered UI`:

| Stage | Status |
|-------|--------|
| `SettingsScreen` calls `onThemeSelect` / `onAccentColorSelect` | ✅ works |
| `LumaViewModel.setThemeName` / `setAccentColorIndex` write prefs, update StateFlow | ✅ works (added in feature 009, verified on device) |
| `MainActivity` collects `themeName`, `accentColorIndex` | ✅ works |
| Selection reaches the **rendered** theme | ❌ **broken here** |

Three concrete disconnects:

1. **`Theme.kt` supplies one static `LumaDarkColorScheme`.** It is built once from fixed
   constants (`primary = AccentElectricBlue`, `primaryContainer = AccentRoyalViolet`,
   `background = CanvasBlack`, `surface = CanvasNavy`) and `LumaCalendarTheme` takes no
   appearance parameters. It is called exactly once (`MainActivity.kt:86`).
2. **`MaterialTheme.colorScheme` is read 0 times anywhere in `app/src/main`.** So even a
   parameterized `colorScheme` would currently change nothing — every component reads fixed
   constants instead.
3. **The selection reaches only the background.** `themeName` → `AmbientBackground(isOled=…)`
   (glow alpha `0.35 → 0.18` + flattens a mid-screen `CanvasNavy` band). `accentIndex` →
   `AmbientBackground(accentGlow=…)` (one radial orb). Meanwhile the accent is hardcoded at
   the render sites below, ignoring the selection entirely:

| Token | Total refs | Render sites (excl. `Color.kt` definitions & `AccentPresets` list) |
|-------|-----------|---------------------------------------------------------------------|
| `AccentElectricBlue` | 100 | SettingsScreen 19, LiquidGlassTimePicker 17, GlassComponents 15, ManualDateInput 13, AddEditEventSheet 10, CalendarScreen 8, EventDetailSheet 7, SearchScreen 3, Type.kt 2, Theme.kt 1 |
| `AccentRoyalViolet` | 32 | GlassComponents 11, CalendarScreen 4, LiquidGlassTimePicker 3, AddEditEventSheet 3, ManualDateInput 2, EventDetailSheet 2, Theme.kt 1 |
| `Canvas*` + `GlassSurface*` | ~90 | GlassComponents 21, LiquidGlassTimePicker 15, CalendarScreen 13, EventDetailSheet 11, AddEditEventSheet 11, ManualDateInput 7, ConfirmActionDialog 4, SettingsScreen 2, SearchScreen 2 |
| `GlassBorder*` | ~74 | CalendarScreen 14, SettingsScreen 11, LiquidGlassTimePicker 11, GlassComponents 10, EventDetailSheet 10, AddEditEventSheet 10, ManualDateInput 9, ConfirmActionDialog 4 |

The selected indicators *do* already exist (2dp white ring on accent circles, tinted chip on
the active theme), so the user-visible symptom is that the controls render nothing — not that
the selection is unreadable.

---

## Decision 1 — One CompositionLocal appearance provider (not MaterialTheme, not mutable globals)

**Decision**: Resolve `themeName` + `accentColorIndex` through a **pure function**
`resolveAppearance(themeName, accentIndex): LumaAppearance`, expose the result via a single
`CompositionLocal`, and have consumers read themed token properties instead of the fixed
`Color.kt` constants. `LumaCalendarTheme` gains the two appearance inputs and provides the
local; `MainActivity` passes the StateFlow values it already collects.

**Rationale**:
- Reactive by construction: StateFlow → recomposition → local read → every consumer updates
  with no navigation or restart (FR-004, FR-009, FR-012).
- The glass token vocabulary (`GlassSurfaceUltraLight/Default/Highlight/Elevated/Pressed`,
  `GlassBorderSubtle/Default/Bright`) has **no Material 3 equivalent**, so
  `MaterialTheme.colorScheme` alone cannot carry the theme (FR-001/FR-002).
- A pure resolver is directly unit-testable without a Compose harness (see Decision 6) and
  keeps constitution VI: no new ViewModel, reuses the existing two StateFlows (FR-011).

**Alternatives considered**:
- **Parameterize `MaterialTheme.colorScheme` only** — rejected: it is read **0 times** today,
  so this would change nothing on screen while appearing to fix the bug. It also has no slots
  for the 5 glass surfaces or 3 glass borders.
- **Mutable global color `var`s** written when appearance changes (`var AccentElectricBlue by
  mutableStateOf(...)`) — rejected: zero call-site edits is tempting, but it makes appearance
  process-global mutable state outside Compose's ownership, is hostile to test isolation
  (values leak across tests), and is the kind of clever shortcut that breaks silently. Boring
  and explicit wins.
- **A second theme state / new preferences** — rejected outright by FR-011/FR-015 and because
  the existing state already works.

## Decision 2 — Accent mapping: preset primary → primary role, preset secondary → gradient partner

**Decision**: `AccentPreset.primary` replaces the `AccentElectricBlue` role (icons, text
accents, selected borders, rings, FAB, tab highlight, chip selection) and
`AccentPreset.secondary` replaces the `AccentRoyalViolet` role (gradient partner in
`GlassButton`, `AmbientBackground` default, `primaryContainer`). `AccentCyan` / `AccentDeepViolet`
are only referenced by the preset list itself and need no render-site mapping.

**Rationale**: This is exactly how the presets are already composed — e.g. "Electric Blue" is
`(AccentElectricBlue, AccentRoyalViolet)` and "Royal Violet" is
`(AccentRoyalViolet, AccentDeepViolet)` — so index 0 reproduces today's appearance byte-for-byte,
guaranteeing **no visual regression on upgrade** for users who never touched the picker
(FR-013, SC-006).

**Alternatives considered**:
- Map `primary` onto *every* accent reference, dropping the gradient partner — rejected:
  collapses the two-tone `GlassButton`/FAB gradients into a flat fill, a visible regression.
- Derive `secondary` algorithmically (e.g. darken `primary`) — rejected: the presets already
  ship deliberate pairings; deriving would discard them.

## Decision 3 — What each theme changes

**Decision**:
- **Liquid Glass** = today's values exactly: `CanvasBlack`/`CanvasNavy` gradient backdrop,
  current glass surface/border alphas, glow alpha `0.35`.
- **OLED Deep** = near-black backdrop (`CanvasBlack`/`CanvasNavy`→`#000000`), glass surfaces
  one step lower in lift, borders one step subtler, glow alpha `0.18`.

**Rationale**: The spec's Assumptions freeze the definition to the direction the code already
implies (`AmbientBackground(isOled)`), so this strengthens an existing intent rather than
inventing a look (constitution III). Darkening canvas *and* surfaces is what makes the
difference read "throughout the app" (FR-001/FR-002) instead of surviving only behind the
ambient layer. White-alpha glass over pure black already darkens naturally, so surface tokens
need a modest trim, not a redesign.

**Alternatives considered**:
- Keep surfaces identical and only flatten the backdrop (current behavior) — rejected: this is
  precisely the reported bug; the difference is invisible behind opaque content.
- Introduce an entirely new OLED surface palette — rejected: constitution III requires using
  existing glass tokens, and inventing new translucency values is prohibited.

## Decision 4 — Which tokens are themed, which are fixed

**Themed (follow appearance)**: `CanvasBlack`, `CanvasNavy`, `CanvasSurface`,
`GlassSurface{UltraLight,Default,Highlight,Elevated,Pressed}`,
`GlassBorder{Subtle,Default,Bright}`, and the accent pair (Decision 2), plus `AmbientBackground`
glow alphas.

**Fixed (never themed)**:
- **Category colors** `CategoryWork/Personal/Health/Social/Finance/Special` — semantic, must
  not follow the accent (spec Assumption). Note `CategoryHealth`/`CategorySpecial` also appear
  as `AccentPreset` *values* (presets "Emerald Mint", "Radiant Coral"); that is palette data,
  not a render site, and is left alone.
- **Destructive/error color** `CategorySpecial` in `ConfirmActionDialog` and the
  `EventDetailSheet` delete dialog — semantic, stays rose.
- **Text colors** `TextWhite*` — changing them risks contrast regressions and is out of scope
  (FR-018).

**Alternatives considered**: theme text colors for OLED — rejected as scope creep with real
accessibility risk; spec asks for appearance change, not a contrast redesign.

## Decision 5 — Selected-state indicators

**Decision**: Keep both existing indicators and strengthen the accent one — selected circle
gets a thicker contrasting ring plus an outer halo so it reads at a glance; the theme chip
keeps its tinted background + border. No layout, label, or position changes.

**Rationale**: FR-003/FR-008 require a *clear* selected indicator; the theme chip already
clearly qualifies, the 2dp white ring on a 26dp circle is marginal. FR-018 forbids redesigning
the Settings UI, so the change stays inside the existing control.

## Decision 6 — Test strategy

**Decision**: Verify at three levels, all without a Compose UI-test harness:
1. **Pure resolution tests** — `resolveAppearance(...)` returns distinct, stable token sets for
   the two themes and for each of the 5 accent indices; index 0 equals today's constants.
2. **Preference round-trip tests** — set theme/accent via `NotificationPreferences`, read back
   through the resolver, assert the effective appearance matches; plus first-run defaults and
   that unrelated keys are untouched.
3. **Isolation test** — resolving a different appearance for one call does not alter a
   previously resolved value (guards Decision 1 against accidental global mutation).

Immediate propagation (FR-004/FR-009/SC-001/SC-002) is a Compose recomposition property:
covered structurally by reading the local inside composition, and confirmed manually in
[quickstart.md](quickstart.md) V1–V4.

**Rationale**: `MaterialTheme.colorScheme` has **0** existing readers and no Roborazzi golden
tests are in use (`captureGolden` appears nowhere in `app/src/test`), so screenshot assertions
would be new infrastructure. A pure resolver gives real assertions for every spec bullet that
can be expressed without a device, at a fraction of the cost.

**Alternatives considered**:
- Add a Compose UI-test harness (`createComposeRule`) to assert on-screen colors — rejected
  for this feature: it is new test infrastructure for a mechanical wiring change, and the
  resolver already proves the logic. Worth adding later if visual regressions recur.
- Roborazzi golden screenshots — same objection; none exist today.

## Decision 7 — No new storage, keys, or state

**Decision**: Reuse `theme_name` and `accent_color_index` exactly as shipped in feature 009,
including their defaults (`"Liquid Glass (Dark)"`, `0`) and the existing guards (unknown theme
falls back to default; accent clamped to `AccentPresets` range).

**Rationale**: FR-015/FR-016 require it, the keys are already proven on device, and inventing
parallel keys is the exact "second state" FR-011 forbids.

**Alternatives considered**: any new key or storage — rejected by the spec.
