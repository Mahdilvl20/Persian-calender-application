# Data Model: Widget Visual Refinement

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Entities

### WidgetLayout (Virtual)

The RemoteViews composition defining the widget's visual structure.

| Element | View ID | Content Source | Text Size | Font Weight | Color |
|---------|---------|---------------|-----------|-------------|-------|
| Month name | widget_month_text | Jalali month name via CalendarConverter | 12sp | Medium | #FFFFFFFF |
| Day number | widget_day_number | Jalali day via CalendarConverter | 40sp | Bold | #FFFFFFFF (normal) / #FF453A (holiday) |
| Secondary left | widget_secondary_left | Gregorian or Hijri day number | 14sp | Regular | #B3FFFFFF |
| Secondary right | widget_secondary_right | Hijri or Gregorian day number | 14sp | Regular | #B3FFFFFF |

**Note**: The current widget has `widget_weekday_text` and
`widget_event_text` which will be replaced by the secondary calendar
values. View IDs may be renamed or re-purposed.

### WidgetDateValues (Derived from Device Date)

| Value | Source | Conversion |
|-------|--------|------------|
| Device date | DateUtils.getRealDeviceLocalDate() | None (raw LocalDate) |
| Jalali year | CalendarConverter.gregorianToJalali() | JDN intermediate |
| Jalali month | CalendarConverter.getMonthName(jalaliMonth) | Localized name |
| Jalali day | CalendarConverter.gregorianToJalali() | JDN intermediate |
| Gregorian day | now.dayOfMonth | Direct from LocalDate |
| Hijri day | CalendarConverter.gregorianToHijri() | JDN intermediate |
| Is holiday | HolidayService.getHoliday() | Gregorian date lookup |

### WidgetSpacing (New Design Values)

| Between | Current | New | Rationale |
|---------|---------|-----|-----------|
| Month → Day number | ~6dp (header bottom + content padding) | ~10dp | Clear separation of levels |
| Day number → Secondary | 2dp (marginTop) | ~10dp | Clear separation of levels |
| Widget edges | 8dp (content padding) | 8dp | Maintain current |
| Secondary left ↔ right | N/A (not present) | Balanced around center | Independent values |

## Validation Rules

| Rule | Element | Validation |
|------|---------|------------|
| VR-001 | Month | Must be Jalali month name, not Gregorian |
| VR-002 | Day number | Must be Jalali day from device date |
| VR-003 | Secondary values | Must represent same real-world day |
| VR-004 | Day number | Must use Persian digits (۰-۹) |
| VR-005 | All values | Must come from device local date, not hardcoded |
