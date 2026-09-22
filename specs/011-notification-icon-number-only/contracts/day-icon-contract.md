# Daily Notification Day-Icon Contract

**Date**: 2026-09-22
**Spec**: [spec.md](../spec.md) · **Geometry**: [data-model.md](../data-model.md)

The UI contract for the routine that draws the daily notification's small icon: what it is
given, what it must return, and what it must never do.

## 1. Generation contract

```
generateSmallIcon(context, dayText, sizePx = 96) -> square white-on-transparent image
```

**Invariants**

- **Content (FR-001)**: the returned image contains the day-number glyphs and nothing else.
  No calendar outline, border, square, page, header bar, binder tab, rounded container, or
  background tile is drawn. Everywhere outside the glyphs the image is fully transparent.
- **Color (FR-015)**: glyphs are opaque white; the system supplies the tint. No colour, no
  gradient, no translucency on the glyph itself.
- **Typeface (FR-002)**: the bundled bold face, falling back to the platform's default bold.
- **Centering (FR-004)**: the ink bounding box center coincides with the image center on both
  axes — `|inkCenter − canvasCenter| ≤ 1% of size` per axis (SC-002).
- **Fit (FR-003, FR-006)**: `half-diagonal(ink box) ≤ 0.46 × size`, so every glyph pixel is
  inside both the image bounds and the system's circular mask (SC-003). Double-digit input is
  scaled down automatically until this holds; single-digit input is scaled up until it holds.
- **Size (FR-003)**: subject to the fit rule, the number is as large as it can be — the ink
  box spans ≥60% of the safe area along its constraining dimension (SC-004).
- **Faithfulness (FR-007)**: the glyphs render exactly the supplied `dayText` — never blanked,
  never substituted, never padded with a placeholder.
- **Determinism**: the same `(dayText, sizePx, face)` always yields the same image; the routine
  reads no clock, no locale, and no preference (Research Decision 7).
- **Shape**: output stays square and keeps the default size unless a caller passes another.

## 2. Caller contract (unchanged)

`LumaNotificationManager` supplies `dayText` and reads the result:

```
smallIconDayText = if (activeCalendarType == JALALI) jalaliDayInPersianDigits
                    else jalaliDay.toString()
```

**Invariants**

- The day is always the **real current local Jalali day**; only the glyph style varies with
  the active calendar type (FR-007). This rule is **not** modified by this feature.
- The caller passes `dayText` through untouched — no trimming, reformatting, or re-seeding.
- Regeneration triggers (midnight update, timezone/date change, reboot reschedule, app
  restart) are unchanged (FR-008, FR-009).

## 3. Non-goals — things this contract must not reach

- **Event Reminder icon** — a separate static drawable; unchanged (FR-011, SC-008).
- **Launcher icon and its day variants** — unchanged (FR-012, SC-008).
- **Notification text, layout, channel, identifier, priority, schedule** — unchanged
  (FR-013, SC-008).
- **`generateIcon`** (the coloured glassmorphism day icon, wired to no notification) —
  untouched (Research Decision 6).
- **No second icon path**: this routine is modified in place; no parallel generator, no new
  asset, no new font (FR-014).

## 4. Verification contract

Geometry is asserted against the pixels the routine actually produces — the test class already
runs with native graphics, so the ink bounding box is measured rather than inferred.

| Rule | Asserts |
|------|---------|
| VI-001 | no opaque pixel outside the measured ink box (frame is gone) |
| VI-002 | ink center within 1% of canvas center, both axes |
| VI-003 | ink box inside the safe circle and inside the image |
| VI-004 | ink box ≥60% of the safe area on its constraining dimension |
| VI-005 | ≥1 fully-opaque white pixel forming an unbroken glyph |
| VI-006 | rendered glyphs match the supplied `dayText` |
| VI-007 | Event Reminder icon, launcher icon, and notification metadata unchanged |

Covered by TR-001..TR-007 in the spec; existing tests must keep passing (TR-007).
