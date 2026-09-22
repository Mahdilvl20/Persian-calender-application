# Implementation Plan: Notification Icon Is Day Number Only

**Branch**: `011-notification-icon-number-only` | **Date**: 2026-09-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/011-notification-icon-number-only/spec.md`

## Summary

The daily notification's small icon is a white alpha mask drawn by
`LumaNotificationIconGenerator.generateSmallIcon`: a rounded calendar outline, a header
separator, two binder tabs, and the day number centered in the calendar's *lower body* at a
fixed fraction of the canvas (0.34× double digit, 0.42× single digit). The frame crowds the
number and the centering is relative to the header, not the icon.

Delete every frame primitive and replace the fixed-fraction sizing with a measure-then-fit
that grows the number until its bounding box's half-diagonal reaches a safe radius, keeping
it centered on the icon's own axes. One source file changes; the caller, the digit-glyph rule,
the regeneration triggers, and every other icon are untouched.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Jetpack Compose app (bitmap drawn with platform 2D canvas)

**Primary Dependencies**: platform 2D drawing already used by the generator; bundled
`vazirmatn_bold` face already used by the current icon — **no new dependency, no new font**

**Storage**: none — the icon is produced on the fly; no preference, asset, or migration

**Testing**: JUnit 4 + Robolectric with **`@GraphicsMode(GraphicsMode.Mode.NATIVE)`**, which
is already set on `LumaNotificationTest` — Canvas calls really rasterize, so pixel-level
geometry assertions (bounding box, centering, opacity, absence of frame) work today with no
new infrastructure

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose, single `LumaViewModel`)

**Performance Goals**: icon generated once per daily-notification refresh; the fit loop is a
handful of measure/shrink iterations on a ≤96px canvas — no measurable change

**Constraints**: Behavior unchanged (FR-007..FR-013): real local Jalali day, existing
digit-glyph rule, existing regeneration triggers; Event Reminder icon, launcher icon,
notification text/layout/channel/ID/scheduling all untouched; existing icon routine modified
in place, no parallel icon system (FR-014)

**Scale/Scope**: **1 source file, 1 test file, 0 callers changed** —
`notification/LumaNotificationIconGenerator.kt` (`generateSmallIcon` only) and
`notification/LumaNotificationTest.kt`. `generateIcon` (coloured, test-only) and the Event
Reminder's static drawable are explicitly out of scope.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | Day derivation untouched — `smallIconDayText` still comes from the device-local date (FR-007, FR-009) |
| II. Calendar Conversion Correctness | PASS | Jalali day still produced by `CalendarConverter`; no conversion change (FR-007) |
| III. Liquid Glass Design Language | PASS | Notification small icons are system-tinted alpha masks, so glass surface/translucency tokens do not apply; no new translucency value is introduced and the result stays a flat opaque-white mask |
| IV. RTL-Aware Localization | PASS | Digit-glyph rule (Persian while Jalali calendar type is active, otherwise Latin) is preserved verbatim (FR-007, FR-013) |
| V. Offline-First Reliability | PASS | Purely local bitmap generation; no network, no storage |
| VI. Single-ViewModel Simplicity | PASS | No state added; the generator stays a stateless routine called from the existing refresh path |
| VII. Testable Date Logic | PASS | No date logic touched; new geometry assertions run against the real rendered bitmap |

No violations.

### Post-Design Re-Check (after Phase 1)

Re-ran every gate against the finished design artifacts
([research.md](research.md), [data-model.md](data-model.md),
[contracts/day-icon-contract.md](contracts/day-icon-contract.md),
[quickstart.md](quickstart.md)):

| Principle | Status | Post-design note |
|-----------|--------|------------------|
| I. Device-Time Authority | PASS | The day still comes from the device-local date; the generator receives it and never derives it (contract §1 "reads no clock"; quickstart V5–V7) |
| II. Calendar Conversion Correctness | PASS | No conversion change — the Jalali day and its digit rendering arrive unchanged from the caller (contract §2) |
| III. Liquid Glass Design Language | PASS | Output stays a flat opaque-white alpha mask that the system tints; no colour, gradient, or new translucency value is introduced, so no glass token is needed or invented |
| IV. RTL-Aware Localization | PASS | The Persian-vs-Latin glyph rule is preserved verbatim and called out as a non-goal (contract §2, §3); Persian-digit width differences are handled by measuring actual ink (research Decision 1) |
| V. Offline-First Reliability | PASS | Purely local drawing; no network, no storage, no new asset (research Decision 7) |
| VI. Single-ViewModel Simplicity | PASS | No state added — still a stateless routine invoked from the existing refresh path (data-model: no state transitions) |
| VII. Testable Date Logic | PASS | No date logic touched; geometry is asserted on the real rendered image with the graphics mode already enabled (research Decision 5), covering TR-001..TR-007 |

Still no violations → Complexity Tracking remains empty.

## Project Structure

### Documentation (this feature)

```text
specs/011-notification-icon-number-only/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── day-icon-contract.md
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to modify)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/
├── LumaNotificationIconGenerator.kt   # ONLY generateSmallIcon(): remove the calendar
│                                      #   outline, header bar, binder tabs; replace
│                                      #   fixed-fraction text sizing with measure-then-fit;
│                                      #   center on the icon's own axes.
│                                      #   generateIcon() (coloured, test-only): UNTOUCHED
└── LumaNotificationManager.kt         # UNTOUCHED — caller, day-text rule, setSmallIcon

app/src/test/java/com/aistudio/lumacalendar/vtxk/notification/
└── LumaNotificationTest.kt            # existing tests kept; add TR-001..TR-006 geometry
                                       #   coverage (frame-free, centering, no clipping,
                                       #   fill ratio, opacity at real notification size)
```

**Structure Decision**: Single-module app, single routine. The work is confined to the body of
`generateSmallIcon` plus tests — no new file, no new abstraction, no caller change, no second
icon path. Geometry is verified on the real rendered bitmap (the test class already runs with
native graphics), so no test-only sizing helper needs to exist.

Rejected alternatives are recorded in [research.md](research.md).

## Complexity Tracking

No constitution violations requiring justification.
