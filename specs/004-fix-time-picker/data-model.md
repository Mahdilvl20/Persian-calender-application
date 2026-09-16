# Data Model: Time Picker RTL/LTR Fix & UI Refinement

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Entities

### TimePickerDialog (Composable State)

| Property | Type | Default | Source | Rule |
|----------|------|---------|--------|------|
| calendarType | CalendarType | from caller | AddEditEventSheet | FR-001 |
| layoutDirection | LayoutDirection | derived from calendarType | LocalizationManager | FR-002 |
| selectedHour | Int | parsed from initialTime | TimeValidator | FR-003 |
| selectedMinute | Int | parsed from initialTime | TimeValidator | FR-004 |
| is24HourMode | Boolean | true | Local state | — |
| activeField | String? | null ("hour"/"minute"/null) | Local state | FR-006 |
| isAm | Boolean | derived from hour | ParsedTime | — |

**State Transitions**:
```
[Dialog Open] → parse initialTime → [Fields Populated]
[Stepper Tap] → update hour/minute → [Value Updated]
[Field Tap] → set activeField → [Field Highlighted]
[Toggle 12H/24H] → toggle is24HourMode → [Mode Switched]
[AM/PM Tap] → adjust hour ±12 → [AM/PM Switched]
[Confirm] → canonicalTime → [onTimeSelected callback]
[Cancel] → [Dialog Dismissed]
```

### TimeField (Virtual)

| Property | Valid Range | Error Condition |
|----------|-------------|-----------------|
| hour | 0–23 (24h), 1–12 (12h) | Never (stepper wraps) |
| minute | 0–59 | Never (stepper wraps) |

**Note**: Since the UI uses stepper buttons only (no text input),
error states per FR-003/FR-004 are not applicable. Values wrap at
boundaries. `ParsedTime.ofSafe()` provides safety clamping.

### LayoutDirection Matrix

| CalendarType | LayoutDirection | Hour Position | Minute Position | AM/PM Position |
|--------------|-----------------|---------------|-----------------|----------------|
| JALALI | Rtl | Right | Left | Left (end of flow) |
| GREGORIAN | Ltr | Left | Right | Right (end of flow) |
| HIJRI | Rtl | Right | Left | Left (end of flow) |

**Compose Row behavior**: Source order `[hour][colon][minute][am/pm]`
with Rtl direction produces visual `[am/pm][minute][colon][hour]`.
This matches the spec requirement (FR-001: RTL = hour right, minute left).

### Touch Target Sizes (After Fix)

| Element | Current | Target | Requirement |
|---------|---------|--------|-------------|
| Stepper buttons | 36dp | 48dp | FR-007, SC-004 |
| Quick pick chips | ~36dp | 48dp | FR-007 |
| 12H/24H toggle | ~34dp | 48dp | FR-007 |
| AM/PM pills | ~42dp | 48dp | FR-007 |
| Cancel/Confirm | ~48dp | 48dp | Already compliant |

## Validation Rules

| Rule | Field | Validation | Error Handling |
|------|-------|------------|----------------|
| VR-001 | hour | 0–23 (24h mode) | Stepper wraps |
| VR-002 | minute | 0–59 | Stepper wraps |
| VR-003 | initialTime | "HH:mm" format | TimeValidator.parseTime with safe default |
| VR-004 | canonicalTime | "HH:mm" format | ParsedTime.canonicalTime |
