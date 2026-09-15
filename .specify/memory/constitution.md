# Luma Calendar Constitution

## Core Principles

### I. Device-Time Authority (NON-NEGOTIABLE)

All date and time operations MUST use the device's real local timezone via
`DateUtils.getRealDeviceZoneId()` and `DateUtils.getRealDeviceLocalDate()`.
Never rely on UTC to determine "today". `Calendar.getInstance()` without an
explicit timezone MUST NOT be used for user-facing date logic. Every function
that produces a "today" value MUST accept it as a parameter or derive it from
the device timezone at call time.

**Rationale**: Users expect the calendar to reflect their local date at all
times. UTC-based logic causes off-by-one day bugs around midnight in non-UTC
timezones.

### II. Calendar Conversion Correctness (NON-NEGOTIABLE)

All Gregorian / Jalali / Hijri conversions MUST go through
`CalendarConverter` using Julian Day Number (JDN) as the canonical
intermediate representation. No ad-hoc date arithmetic. Every new calendar
type added MUST implement the same `gregorianToJdn` / `jdnToXxx` contract
and be covered by round-trip unit tests.

**Rationale**: Calendar math is error-prone. A single conversion pathway
with JDN as pivot eliminates drift and divergent implementations.

### III. Liquid Glass Design Language

UI MUST maintain a consistent glassmorphism aesthetic defined in
`GlassComponents.kt` and `Color.kt`. Translucent surfaces use the
`GlassSurface*` palette; ambient glow uses `AmbientGlow*` orbs. New
components MUST use existing glass tokens rather than inventing new
translucency values. Accent colors are selected from `AccentPresets`; custom
palettes MUST go through the accent system.

**Rationale**: Visual consistency across screens builds user trust and
reinforces the Premium brand identity.

### IV. RTL-Aware Localization

Layout direction and string resources MUST be driven by `LocalizationManager`
based on the active `CalendarType`. Jalali calendar type implies RTL layout
direction; Gregorian implies LTR. New UI strings MUST be added to
`AppStrings` and supplied through `LocalAppStrings`. Hardcoded user-facing
strings are prohibited.

**Rationale**: Persian users expect native RTL flow. Embedding strings
inline breaks the localization contract and creates maintenance debt.

### V. Offline-First Reliability

Core calendar display, date navigation, and event management MUST function
without network connectivity. API calls (Persian calendar data) are
enhancements that degrade gracefully. Local Room database is the source of
truth for user events. Network failures MUST NOT prevent the app from
launching or displaying the calendar grid.

**Rationale**: A calendar is a daily-use utility. Network dependency would
make it unreliable for its primary purpose.

### VI. Single-ViewModel Simplicity

All application state flows through `LumaViewModel` using Kotlin `StateFlow`.
New features SHOULD extend the existing ViewModel rather than introducing
additional ViewModels. If a feature's state exceeds ~300 lines in the
ViewModel, extract a focused state holder class but keep the composable
wiring in `LumaApp`.

**Rationale**: The single-ViewModel pattern keeps state transitions
predictable and avoids coordination bugs between multiple state holders.

### VII. Testable Date Logic

All date conversion, formatting, and calendar grid generation functions MUST
be pure functions or accept explicit parameters (no implicit system state
dependencies). New calendar logic MUST have corresponding unit tests in
`app/src/test/`. Screenshot tests via Roborazzi SHOULD be added for any
visual component changes.

**Rationale**: Date logic is the most bug-prone area of a calendar app.
Pure functions enable reliable unit testing without Android instrumentation.

## Technical Constraints

- **Min SDK**: 24 (Android 7.0). APIs below this MUST NOT be used without
  version guards.
- **Build system**: Gradle with KSP for annotation processing (Room, Moshi).
  Configuration cache and parallel builds MUST remain enabled.
- **Dependencies**: New third-party dependencies require justification.
  Prefer AndroidX / Compose Material3 equivalents over external libraries.
- **ProGuard**: Currently disabled. Any enablement MUST include complete
  keep rules for Room entities, Moshi models, and Retrofit interfaces.
- **Firebase**: AI features use Gemini via Firebase AI. API keys are managed
  through `.env` and the Secrets Gradle plugin; they MUST NOT be hardcoded.

## Development Workflow

- **Branch model**: Feature branches off `main`. No direct commits to
  `main`.
- **Commit messages**: Conventional format (`feat:`, `fix:`, `refactor:`,
  `test:`, `docs:`).
- **Code review**: All PRs MUST verify compliance with this constitution's
  principles, especially I (device-time) and II (calendar correctness).
- **Testing gate**: `./gradlew test` MUST pass before merge. New calendar
  logic MUST include unit tests.

## Governance

This constitution is the authoritative reference for architectural and
quality decisions in Luma Calendar. When this document conflicts with other
documentation, this constitution takes precedence.

Amendments require: (1) a written proposal describing the change and its
rationale, (2) version bump following semver (MAJOR for principle
removal/redefinition, MINOR for new principles, PATCH for clarifications),
(3) update to `LAST_AMENDED_DATE`.

Compliance is verified during code review. Non-trivial violations MUST be
resolved before merge.

**Version**: 1.0.0 | **Ratified**: 2026-09-16 | **Last Amended**: 2026-09-16
