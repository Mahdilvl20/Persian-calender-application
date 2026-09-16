# Data Model: Time Picker Ordering Fix

**Date**: 2026-09-17
**Spec**: [spec.md](spec.md)

## Entities

### TimeControlRow (Layout Isolation)

The Row containing Hour, Separator, Minute, and AM/PM. MUST be
wrapped in `CompositionLocalProvider(LocalLayoutDirection provides
LayoutDirection.Ltr)` to force stable LTR ordering.

| Property | Value | Rule |
|----------|-------|------|
| LayoutDirection | Always Ltr | FR-002 |
| Source order | [Hour] [Colon] [Minute] [AM/PM] | — |
| Visual order | [Hour] [Colon] [Minute] [AM/PM] | FR-001 |
| Parent dialog direction | Driven by calendarType | FR-003 |

### DialogContent (Preserved RTL)

Everything outside the TimeControlRow (header, labels, buttons,
quick picks, duration shortcuts) retains the calendar-type-driven
LayoutDirection.

| Section | RTL in Jalali | LTR in Gregorian |
|---------|---------------|------------------|
| Header (title, icon, toggle) | RTL | LTR |
| Cancel/Confirm buttons | RTL (Confirm left) | LTR (Confirm right) |
| Quick minute picks | RTL | LTR |
| TimeControlRow | LTR (forced) | LTR (natural) |

### LayoutDirection Matrix (After Fix)

| CalendarType | Dialog | TimeControlRow | Hour Position | Minute Position |
|--------------|--------|----------------|---------------|-----------------|
| JALALI | Rtl | Ltr (forced) | Left | Right |
| GREGORIAN | Ltr | Ltr (natural) | Left | Right |
| HIJRI | Rtl | Ltr (forced) | Left | Right |

**Key invariant**: Hour → Separator → Minute is ALWAYS left-to-right
regardless of calendar type.

## Validation Rules

| Rule | Validation |
|------|------------|
| VR-001 | TimeControlRow LayoutDirection MUST be Ltr in all cases |
| VR-002 | Dialog content LayoutDirection MUST follow calendarType |
| VR-003 | Hour controls MUST modify hour (never minute) |
| VR-004 | Minute controls MUST modify minute (never hour) |
