# Data Model: Notification Icon Is Day Number Only

**Date**: 2026-09-22
**Spec**: [spec.md](spec.md)

Nothing here is stored — the icon is produced on the fly and discarded with the notification.
This document describes the values the generator consumes, the geometry it must produce, and
the rules that geometry must satisfy.

## Inputs

| Input | Type | Range / default | Rule |
|-------|------|-----------------|------|
| Day text | text | 1–2 glyphs, the Jalali day | FR-007 — caller supplies it unchanged; the generator never derives, substitutes, or blanks it |
| Canvas size | whole number | default 96, square | Research Decision 7 — unchanged; output is always square |

The day text arrives as Persian glyphs while the Jalali calendar type is active and as Latin
glyphs otherwise. That rule belongs to the caller and is **not** part of this feature
(FR-007, FR-013).

## Output

**Day notification icon** — a square image whose only content is the day number.

| Attribute | Rule | Source |
|-----------|------|--------|
| Content | day-number glyphs only; fully transparent everywhere else | FR-001, TR-006 |
| Color | opaque white — the system tints it | FR-015 (existing behavior) |
| Typeface | bundled bold face, platform bold as fallback | FR-002, Research Decision 4 |
| Size | as large as fits the safe area | FR-003, SC-004 |
| Center | ink bounding box centered on the icon's own center, both axes | FR-004, SC-002 |
| Fit | ink bounding box inside the safe circle | FR-003, FR-006, SC-003 |

## Geometry

- **Safe area**: a circle centered on the icon with `radius = 0.46 × canvas size`. Chosen to
  clear the system's small-icon mask (Research Decision 2 and 3).
- **Ink bounding box**: the smallest rectangle containing every non-transparent pixel —
  measured from the rendered image in tests, not assumed from font metrics.
- **Fit rule**: `half-diagonal(ink box) ≤ safeRadius`. Satisfying this puts every corner of
  the box inside the circle, so no glyph pixel can be cut by the mask.
- **Center rule**: `|inkCenterX − canvasCenterX| ≤ 0.01 × size` and
  `|inkCenterY − canvasCenterY| ≤ 0.01 × size` (SC-002).

### Reference numbers at the default 96px canvas (safe radius 44.2px)

| Input class | Aspect | Expected ink box | Constraining dimension |
|-------------|--------|------------------|------------------------|
| Single digit (e.g. `۹`, `1`) | ≈0.6:1 | ≈45 × 76 px → height ≈79% of canvas | height |
| Double digit (e.g. `۳۱`, `10`) | ≈2:1 | ≈79 × 40 px → width ≈82% of canvas | width |

Both satisfy SC-004 (≥60% of the safe area along the constraining dimension) with the
half-diagonal pinned at the safe radius.

## Entities (unchanged)

- **`CalendarEvent`** (Room): untouched — no schema change, no migration.
- **Notification schedule / channel / ID / text**: untouched (FR-013).
- **Event Reminder icon** and **launcher icon and its day variants**: untouched (FR-011,
  FR-012).
- **`generateIcon`** (coloured day icon, wired to no notification): untouched (Research
  Decision 6).

## Validation Rules

| Rule | Field | Validation | Source |
|------|-------|-----------|--------|
| VI-001 | icon content | no opaque pixel outside the measured ink box → proves the frame, header bar and tabs are gone | FR-001, TR-006, SC-001 |
| VI-002 | centering | ink box center within 1% of canvas center on both axes | FR-004, TR-003, SC-002 |
| VI-003 | clipping | ink box entirely inside the safe circle **and** inside the image bounds | FR-003/FR-006, TR-004, SC-003 |
| VI-004 | size | ink box spans ≥60% of the safe area along its constraining dimension | FR-003, TR-005, SC-004 |
| VI-005 | legibility | ≥1 fully-opaque white pixel forming an unbroken glyph; holds at the real notification size | FR-015, TR-005, SC-006 |
| VI-006 | input faithfulness | rendered glyphs match the supplied day text exactly — never blank, never substituted | FR-007, edge case |
| VI-007 | scope | Event Reminder icon, launcher icon, and notification text/channel/ID/schedule unchanged | FR-011..FR-013, SC-008 |

## State Transitions

None. The icon is derived fresh from the day on each daily-notification refresh; it has no
persisted state and no lifecycle beyond the notification that carries it.
