# Research: Time Picker RTL/LTR Fix & UI Refinement

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Current Implementation Analysis

### File: LiquidGlassTimePickerDialog.kt (613 lines)

The Time Picker is a Compose `Dialog` that receives `isRtl: Boolean`
as a static parameter from the caller (AddEditEventSheet).

**Time display structure**: A `Row` with source order:
`[Hour Column] [Colon] [Minute Column] [AM/PM Column]`

When `LocalLayoutDirection = Rtl` (set via CompositionLocalProvider),
Compose's Row automatically reverses children, producing visual order:
`[AM/PM] [Minute] [Colon] [Hour]`

### The RTL Bug

**Root cause**: The `isRtl` parameter is a static Boolean captured
at dialog open time. Two issues:

1. **AM/PM positioning**: Row auto-reversal moves AM/PM to the far
   visual left in RTL. For Persian users, the expected reading order
   is `[Hour] [:] [Minute] [AM/PM]` flowing right-to-left, with AM/PM
   on the left side (end of the RTL reading flow). But Row reversal
   puts AM/PM at index 0 (visual far-left), which is actually the
   correct end position for RTL. However, the visual separation
   between AM/PM and the time fields becomes inconsistent.

2. **Static direction**: If the user changes calendar type while the
   dialog is open, the layout direction does NOT update because
   `isRtl` is a parameter, not a derived state.

**Decision**: Replace the static `isRtl: Boolean` parameter with
a dynamic `calendarType: CalendarType` parameter, and derive the
layout direction inside the composable using
`LocalizationManager.getLayoutDirection(calendarType)`.

**Rationale**: This matches the pattern used in MainActivity and
ensures the dialog updates if calendar type changes.

### Touch Target Issues

Stepper IconButtons are explicitly sized at 36dp, below the 48dp
Android accessibility minimum. Quick picks and toggle are ~32-40dp.

**Decision**: Increase stepper buttons to 48dp. Increase quick picks
and toggle to meet 48dp minimum. Use padding rather than fixed size
to allow natural growth.

### Focus States

Currently NO focus states exist on time fields. The spec requires
visible focus feedback.

**Decision**: Add a subtle border highlight or background change when
a time field area is being interacted with. Since the stepper UI has
no text input, "focus" means which field (hour or minute) was last
tapped or stepped. Add a `remember` state for the active field and
apply conditional border styling.

### Error States

The stepper-only UI wraps values at boundaries (hour 0-23, minute
0-59 in 5-min increments), so invalid values cannot occur via the
UI. The spec's error state requirements (FR-003, FR-004, FR-015)
are designed for text-input time pickers.

**Decision**: Since this is a stepper-only picker, error states are
not applicable. The `ParsedTime.ofSafe()` safety clamp in
TimeValidator provides the defensive backstop. If a text-input mode
is ever added, error states would need to be implemented then.

### Hardcoded Values

Found hardcoded:
- Color `#0D1426` (line 161) — should use a design token
- Strings: "Quick Minutes"/"دقایق متداول", "Duration from
  Start"/"مدت زمان از زمان شروع", and contentDescription strings
  for stepper buttons — should use AppStrings

**Decision**: Replace hardcoded color with nearest token. Move
strings to AppStrings via LocalizationManager.

## UI Improvements

### Improvement 1: Active Field Highlight

**What changes**: The time field being interacted with gets a subtle
border color change (brighter accent) while the other field dims slightly.

**Why it improves usability**: Users always know which field they're
adjusting, preventing accidental changes to the wrong field.

**How it fits the design system**: Uses existing AccentElectricBlue
and AccentRoyalViolet border colors at different opacity levels,
consistent with the glass component interaction pattern.

### Improvement 2: Larger Touch Targets

**What changes**: Stepper buttons grow from 36dp to 48dp. Quick pick
chips and toggle pill grow to meet 48dp minimum.

**Why it improves usability**: Easier to tap accurately, especially
on the stepper buttons which are used repeatedly.

**How it fits the design system**: Uses padding-based sizing within
existing glass surfaces, maintaining visual proportions.

### Improvement 3: Smooth Value Transitions

**What changes**: When a stepper button is tapped, the value change
is accompanied by a subtle scale or fade animation on the number.

**Why it improves usability**: Provides visual confirmation that the
value changed, especially useful for rapid tapping.

**How it fits the design system**: Uses existing Compose animation
tween patterns consistent with the app's interaction animations.

### Improvement 4: Consistent Separator Positioning

**What changes**: The colon separator is given explicit
`LayoutDirection.Ltr` text direction to ensure it renders consistently
regardless of the dialog's layout direction.

**Why it improves usability**: Prevents potential rendering quirks
where the colon might be positioned incorrectly in RTL mode.

**How it fits the design system**: Pure functional fix, no visual
change needed.

### Improvement 5: Localized Content Descriptions

**What changes**: Stepper button content descriptions ("Increase
Hour", "Decrease Hour", etc.) are moved to AppStrings for
localization.

**Why it improves usability**: Screen reader users get properly
localized descriptions in their language.

**How it fits the design system**: Follows the existing localization
architecture via AppStrings/LocalizationManager.
