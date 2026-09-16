# Research: Time Picker Hour/Minute Ordering Fix

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Root Cause Analysis

### Current Implementation

File: `LiquidGlassTimePickerDialog.kt` (613 lines)

The dialog receives `isRtl: Boolean` and wraps its entire content in:
```kotlin
CompositionLocalProvider(
    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl
                                  else LayoutDirection.Ltr
)
```

The time display Row (line 247) has source order:
`[Hour Column] [Colon] [Minute Column] [AM/PM Column]`

When `LocalLayoutDirection = Rtl`, Compose's Row automatically
reverses children, producing visual order:
`[AM/PM] [Minute] [Colon] [Hour]`

This places Minute on the LEFT and Hour on the RIGHT, which is the
opposite of what the user wants.

### The Fix

**Decision**: Wrap ONLY the time-control Row (the Row containing
Hour, Colon, Minute, AM/PM) in a nested
`CompositionLocalProvider(LocalLayoutDirection provides
LayoutDirection.Ltr)`. This forces the Row to render in LTR
(source order = visual order), producing:
`[Hour] [Colon] [Minute] [AM/PM]`

**Rationale**: This is the standard Compose pattern for isolating a
section from global layout direction. The outer dialog retains RTL
for header, labels, and buttons. Only the numeric time controls
are forced to LTR.

**Alternatives considered**:
- Reverse source order for RTL: Rejected because it creates two
  code paths and breaks when calendar type changes.
- Force entire dialog to LTR: Rejected because header, labels,
  and buttons must remain RTL in Persian mode.
- Use `Modifier.layoutDirection(LayoutDirection.Ltr)` on the Row:
  Also valid, but `CompositionLocalProvider` is more explicit and
  matches the project's existing pattern.

### Impact on AM/PM

With the time Row forced to LTR, AM/PM appears at the end (right
side) of the Row. In 12H mode with RTL dialog, this means AM/PM
is visually on the right of the time fields. This is acceptable
because AM/PM is part of the time value, not a navigation element.

### Impact on Separator

The colon separator `Text(":")` will now always render in LTR
context within the forced-LTR Row. This ensures consistent colon
rendering regardless of global direction.

### Impact on Plus/Minus Controls

The plus/minus controls are inside each Column (Hour Column,
Minute Column). Since the Column's internal layout is
`Alignment.CenterHorizontally` (symmetric), RTL mirroring does
not affect the vertical arrangement of plus/value/minus. Each
field's controls remain correctly associated with their value.
