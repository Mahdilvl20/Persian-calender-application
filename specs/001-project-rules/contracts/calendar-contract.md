# Calendar Conversion Contract

**Date**: 2026-09-16
**Spec**: [spec.md](../spec.md)

## Conversion Rules

### Input/Output Format

| Calendar | Date Format | Example |
|----------|-------------|---------|
| Gregorian | "YYYY-MM-DD" | "2026-09-16" |
| Jalali | Year, Month, Day (int) | 1405, 6, 25 |
| Hijri | Year, Month, Day (int) | 1448, 6, 24 |
| JDN | Long (Julian Day Number) | 2461300 |

### Conversion Pathway

```
Gregorian ←→ JDN ←→ Jalali
Gregorian ←→ JDN ←→ Hijri
```

All conversions MUST go through JDN as the canonical intermediate.
Direct Gregorian↔Jalali or Gregorian↔Hijri conversion is prohibited.

### Invariants

1. Round-trip identity: gregorianToJdn(y,m,d) → jdnToGregorian(jdn) = (y,m,d)
2. Cross-calendar identity: any date converted to JDN and back yields
   the same real-world date in all three systems
3. JDN is monotonically increasing with real time
4. No ad-hoc date arithmetic outside CalendarConverter

### Edge Cases

| Input | Expected Behavior |
|-------|-------------------|
| 29 Esfand in non-leap year | Convert to 1st of next month or safe fallback |
| Day 0 or negative JDN | Return safe default, log error |
| Year outside 1400-1500 (Jalali) | Still convert correctly via JDN math |
| Invalid month/day combo | Return nearest valid date, log warning |

## Device Time Rules

### "Today" Definition

```kotlin
fun getRealDeviceDate(): String
  // Returns Gregorian "YYYY-MM-DD" from device local timezone
  // NEVER uses UTC
  // NEVER uses selectedDate
```

### Timezone Handling

1. Device timezone is the single source of truth
2. All date comparisons use local timezone
3. UTC is never used for user-facing "today" determination
4. Timezone changes trigger notification update (not recreation)

### Prohibited Patterns

- `Calendar.getInstance()` without explicit timezone for user-facing logic
- `LocalDate.now()` without ZoneId parameter
- `selectedDate` used as "today"
- Hardcoded date strings in production code
- `+1 day` or `-1 day` as fix for date bugs
- UTC conversion without explicit timezone mapping
