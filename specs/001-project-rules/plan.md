# Implementation Plan: Luma Calendar Project Rules

**Branch**: `001-project-rules` | **Date**: 2026-09-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-project-rules/spec.md`

## Summary

This is a governance and enforcement plan for 38 project rules covering
calendar accuracy, notification integrity, Liquid Glass UI consistency,
error resilience, and development practices. The spec codifies existing
behavior as testable requirements. No new features are introduced; the
work is verification, gap-filling, and hardening of the current codebase
against the documented rules.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Java 11 (source/target)

**Primary Dependencies**: Jetpack Compose (Material3), Room 2.7.0 (KSP),
Retrofit 2.12 + Moshi, Firebase AI / App Check, Robolectric 4.16 /
Roborazzi 1.59

**Storage**: Room database (`luma_calendar_db`, single entity:
CalendarEvent), disk cache in `context.cacheDir` for Persian calendar JSON

**Testing**: JUnit 4 + Robolectric (unit), Roborazzi (screenshot golden),
no instrumented tests

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose)

**Performance Goals**: Daily notification within 2s of enable; event
reminder trigger within 1s of scheduled time; offline-first calendar
grid rendering

**Constraints**: No foreground service for daily notification; no Android
Live Update; no ActivityAlias; single ViewModel pattern; Vazirmatn
typography weights enforced

**Scale/Scope**: Single-user local app; ~40 Kotlin source files; 3
calendar systems; 2 notification channels

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | FR-001, FR-009 enforce via DateUtils |
| II. Calendar Conversion Correctness | PASS | FR-002 enforces JDN pathway |
| III. Liquid Glass Design Language | PASS | FR-007 enforces token usage |
| IV. RTL-Aware Localization | PASS | FR-005 enforces via LocalizationManager |
| V. Offline-First Reliability | PASS | SC-007, FR-011 enforce |
| VI. Single-ViewModel Simplicity | PASS | No new ViewModels introduced |
| VII. Testable Date Logic | PASS | SC-009 enforces test gate |

No constitution violations. All requirements align with existing principles.

## Project Structure

### Documentation (this feature)

```text
specs/001-project-rules/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/
├── MainActivity.kt                    # Entry point, notification setup
├── data/
│   ├── CalendarEvent.kt               # Room entity
│   ├── EventDao.kt                    # Database queries
│   ├── EventRepository.kt             # Data access abstraction
│   └── LumaDatabase.kt               # Room database singleton
├── data/api/                          # Persian calendar Retrofit API
├── data/holiday/                      # Holiday providers
├── data/model/                        # PersianCalendarDay
├── data/repository/                   # PersianCalendarRepository
├── notification/
│   ├── LumaNotificationManager.kt    # Daily notification (ID 1001)
│   ├── EventNotificationScheduler.kt # Event reminders
│   ├── MidnightUpdateReceiver.kt     # Date change receiver
│   └── ReminderRescheduleReceiver.kt # Boot/time change receiver
├── ui/
│   ├── components/GlassComponents.kt # Liquid Glass design system
│   ├── screens/                       # Calendar, Search, Settings
│   ├── theme/Color.kt                # Color tokens
│   └── viewmodel/LumaViewModel.kt    # Single ViewModel
├── util/
│   ├── CalendarConverter.kt          # JDN conversions
│   ├── DateUtils.kt                  # Device time utilities
│   └── LocalizationManager.kt        # RTL/LTR + strings
└── widget/                           # Home screen widget

app/src/test/java/com/aistudio/lumacalendar/vtxk/
├── CalendarAndHolidayTest.kt
├── TimeValidatorTest.kt
├── DateValidatorTest.kt
└── notification/
    ├── EventNotificationSchedulerTest.kt
    └── LumaNotificationTest.kt
```

**Structure Decision**: Existing Android single-module structure. No
structural changes required. Rules are enforced via verification of
existing code paths and selective hardening.

## Complexity Tracking

No constitution violations requiring justification.
