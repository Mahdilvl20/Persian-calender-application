# Research: Notification Icon Is Day Number Only

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

All facts below were verified directly from the current source.

## Finding 0 — What actually draws the daily icon (pre-work investigation)

| Routine | Wired to | Verdict |
|---------|----------|---------|
| `generateSmallIcon(context, dayText, sizePx = 96)` — `LumaNotificationIconGenerator.kt:165` | **Daily notification**: `LumaNotificationManager.kt:229` → `setSmallIcon(IconCompat.createWithBitmap(...))` at `:305` | **in scope** |
| `generateIcon(context, dayText, sizePx = 120)` — coloured glassmorphism, same file | **nothing in `app/src/main`** — referenced only by `LumaNotificationTest.testDynamicIconGeneration` | out of scope |
| `R.drawable.ic_notification_luma` | **Event Reminder**: `EventNotifications.kt:367` | excluded by FR-011 |

Day text feeding the daily icon (`LumaNotificationManager.kt:228`):
`if (calendarType == CalendarType.JALALI) jDayPersian else j.day.toString()` — the *day* is
always the Jalali day; only the glyph style follows the active calendar type. **Untouched.**

What `generateSmallIcon` draws today, in order:

1. Rounded-rect calendar outline (stroke, `size*0.055f`) spanning x 0.16–0.84, y 0.20–0.86
2. Header separator line at 26% down the calendar body
3. Two binder tabs above the outline
4. Day number: `textSize = 0.34×size` (2+ chars) or `0.42×size` (1 char), anchored at
   `cx = size*0.5`, `cy = headerY + (bottom-headerY)*0.5 - bounds.exactCenterY()`

Two independent defects: **(a)** the frame is present at all, and **(b)** the number is
centered on the *calendar's lower body*, not on the icon — so even without the frame it would
sit low and small.

---

## Decision 1 — Measure-then-fit sizing, not fixed fractions

**Decision**: Start the text at a generous size, measure its ink bounds, and shrink (a few
times, or by direct ratio) until the bounding box's half-diagonal fits the safe radius. Use the
resulting size for the draw.

**Rationale**: SC-003 (zero clipping) and SC-004 (≥60% fill) must hold for **both** glyph
styles, and Persian (`۱۰`) and Latin (`10`) digits differ in width at the same nominal size —
a fixed per-digit-count fraction cannot simultaneously guarantee "never clipped" and "as large
as possible" across both. Measuring the actual ink makes the fit exact for whatever is being
drawn, which is also what the edge-case list demands.

**Alternatives considered**:
- **Bump the existing fixed fractions** (e.g. 0.42 → 0.55 / 0.34 → 0.46) — rejected: one
  fraction either clips the wider glyph style or wastes margin on the narrower one; it cannot
  satisfy SC-003 and SC-004 for every input 1–31 in both glyph styles.
- **Per-digit-count fixed sizes tuned until tests pass** — rejected: tuned constants are the
  same trap with extra steps, and they silently break if the face or glyph style changes.

## Decision 2 — Fit to a *circle*, not a square

**Decision**: The constraint is `half-diagonal(bounding box) ≤ safeRadius`, i.e. every corner
of the ink box lies inside a circle centered on the icon.

**Rationale**: SC-003 explicitly requires the number to survive **the system's mask**, not just
the image edge. A box that fits a square can still poke outside an inscribed circle — exactly
the clipping the existing code's own comment warns about ("leave breathing room so the system
tint circle doesn't clip it"). Fitting the diagonal is the edge-case-correct rule: it is
conservative on the corner pixels where a bounding box is mostly empty, and it costs only a
little size.

**Alternatives considered**:
- **Fit to an inscribed square** (half-side = safeRadius) — rejected: allows larger text but
  corner pixels can fall outside a circular mask, violating SC-003.
- **Fit each axis independently to the icon edge** — rejected: maximizes one axis while the
  other still risks the mask; no guarantee at all for the corners.

## Decision 3 — Safe radius and centering anchor

**Decision**: `safeRadius = 0.46 × size` (4% clearance from the icon edge on the cardinal
axes), and center the **ink bounding box** on the icon's own center:
`cx = size/2 - bounds.exactCenterX()`, `cy = size/2 - bounds.exactCenterY()`, with the paint's
horizontal alignment set to start so the offsets are applied exactly.

**Rationale**: 0.46 mirrors the breathing room the current code already leaves (its outline ran
0.16→0.84, i.e. 0.34 half-extent, and the tabs pushed further out); tightening it slightly
buys margin for the mask. Centering on `size/2` directly satisfies FR-004, and using the ink
bounds rather than the text-advance center satisfies SC-002 (≤1% error) for glyph shapes with
side bearings — the advance-based center can drift on Persian digits.

Worked check at the default 96px canvas, safe radius 44.2px:

| Input | fitted ink box | share of icon |
|-------|----------------|---------------|
| single digit (≈0.6:1 aspect) | ≈45 × 76 px | height ≈79%, half-diagonal 44.2 ✓ |
| double digit (≈2:1 aspect) | ≈79 × 40 px | width ≈82%, half-diagonal 44.2 ✓ |

Both clear SC-004's ≥60% on their constraining dimension and sit inside the safe circle.

**Alternatives considered**:
- **Keep the header-relative anchor and only enlarge** — rejected: FR-004 requires centering
  on the icon, and the header disappears with the frame anyway.
- **Center the text *advance* (baseline math only)** — rejected: fails SC-002 for glyphs whose
  advance and ink differ.

## Decision 4 — Typeface: keep the bundled bold face

**Decision**: Continue using `vazirmatn_bold`, falling back to the platform's default bold if
it fails to load — exactly as the current code does.

**Rationale**: The spec's Assumptions fix "bold, clean" to the face already bundled and already
used; it carries both Persian and Latin digits; constitution's preference for existing assets
and the "no new font" constraint. A fallback keeps the icon rendering even if the resource is
missing.

**Alternatives considered**:
- **Add a numeral-specific display face** — rejected: a new asset for a one-glyph icon, and it
  would need its own Persian-digit coverage.

## Decision 5 — Verify geometry on the real rendered bitmap

**Decision**: Assert against the pixels `generateSmallIcon` actually produces. `LumaNotificationTest`
already runs with native graphics (`@GraphicsMode(GraphicsMode.Mode.NATIVE)`), so `drawText`
rasterizes and the glyph bounding box can be measured by scanning for opaque pixels.

**Rationale**: This tests the shipped artifact rather than a helper, needs no new infrastructure,
and covers every TR at once — TR-001/TR-002 (content), TR-003 (centering from the measured
box), TR-004 (box inside the safe circle and inside the image), TR-005 (opacity + fill ratio),
TR-006 (no frame: no opaque pixel outside the measured glyph box).

**Alternatives considered**:
- **Extract a pure `fitText(...)` helper and unit-test the arithmetic** — rejected: it tests a
  helper the app does not ship, and pixel assertions on the real bitmap already prove the same
  properties more directly. Adds a file and an abstraction for no coverage gain.
- **Golden/screenshot comparisons** — rejected: none exist in this repo (`captureGolden` appears
  nowhere in `app/src/test`), so it would be new infrastructure for a single icon, and exact
  pixel goldens are brittle across font rasterizers.

## Decision 6 — Leave `generateIcon` (coloured) and the caller alone

**Decision**: Touch only `generateSmallIcon`'s body. Do not modify `generateIcon`,
`LumaNotificationManager`, the day-text rule, or any regeneration trigger.

**Rationale**: `generateIcon` is wired to no notification (test-only), so changing it would not
alter anything a user sees while breaking an existing test — outside FR-010's "Daily/Persistent
only" scope. The caller already passes everything needed; FR-007..FR-013 require the text rule,
channel, ID, and schedule to stay identical, and FR-014 requires modifying the existing routine
rather than adding a parallel one.

**Alternatives considered**:
- **Also redesign `generateIcon` for consistency** — rejected: scope creep into a routine no
  notification uses; noted for a possible dead-code cleanup instead.
- **Add a new `generateDayNumberIcon(...)` alongside the old one** — rejected: forbidden
  outright by FR-014.

## Decision 7 — No change to how or when the icon is refreshed

**Decision**: Regeneration triggers (midnight update, timezone/date change, reboot reschedule,
app restart) and the default 96px canvas stay exactly as they are; this feature only changes
what is drawn inside the canvas.

**Rationale**: FR-008/FR-009 require today's behavior to keep working, and FR-013 forbids
touching scheduling. The default resolution already matches the highest density at the size
notifications render, so raising it would change nothing visible while widening the diff.

**Alternatives considered**:
- **Render at a higher resolution for crispness** — rejected: no user-visible gain at the size
  the system uses, and it changes an input the existing tests assert on.
