# Research: Luma Calendar Project Rules

**Date**: 2026-09-16
**Spec**: [spec.md](spec.md)

## Audit Results

### Date & Calendar System (10/10 PASS)

| Rule | Status | Evidence |
|------|--------|----------|
| Device-time for "today" | PASS | DateUtils.kt:39-61, LumaViewModel.kt:105 |
| Selected date ≠ today | PASS | DateUtils.getMonthDays takes todayDate as independent param |
| No hardcoded dates | PASS | Zero +1/-1 day hacks in calendar logic |
| JDN conversion pathway | PASS | CalendarConverter.kt encapsulates all JDN math |
| RTL for Jalali/Hijri | PASS | LocalizationManager.isRtl(), CompositionLocalProvider |
| No manual text reversal | PASS | Zero .reversed() calls on strings |
| Liquid Glass tokens | PASS | Color.kt tokens used in GlassComponents.kt |
| Vazirmatn weights | PASS | Regular/Medium/SemiBold correctly assigned |
| Dynamic icon = device date | PASS | DynamicIconManager.kt:147 uses getRealDeviceDate() |
| No ActivityAlias | PASS | Single MainActivity, ShortcutManager for icons |

### Notification System (7/9 PASS, 2 GAPs)

| Rule | Status | Evidence |
|------|--------|----------|
| ID 1001, channel, ongoing | PASS | LumaNotificationManager.kt:49-50,278-279 |
| Single instance | PASS | Mutex + fixed notification ID |
| Event channel separate | PASS | EventNotifications.kt:64 "event_reminders" |
| Cancel on edit/delete | PASS | EventNotifications.kt:102, LumaViewModel.kt:410 |
| Channel idempotent | **GAP** | Event channel lacks existence guard (EventNotifications.kt:140-154) |
| No foreground service | PASS | No service declarations in manifest |
| No notification loop | PASS | updateNotification doesn't trigger scheduleMidnightUpdate |
| Logging tag | **GAP** | Zero logging in entire notification package |
| Cancel on disable | PASS | LumaViewModel.kt:454 cancels ID 1001 |

### Development Governance (Derived from Codebase)

| Rule | Status | Evidence |
|------|--------|----------|
| No parallel implementations | PASS | Single LumaNotificationManager, single EventNotificationScheduler |
| Backward compatibility | PASS | Existing APIs preserved, no breaking changes |
| Refactoring safety | PASS | Codebase is stable, no active refactors |
| No hardcoded business data | PASS | No demo events in production code |

## Gaps Identified

### Gap 1: Event Notification Channel Idempotency

**Location**: `EventNotifications.kt:140-154`
**Severity**: Low (benign in practice due to Android platform behavior)
**Rule**: FR-016 (idempotent channel creation)

The daily notification channel at `LumaNotificationManager.kt:68` has an
explicit `getNotificationChannel() != null` guard. The event reminder
channel at `EventNotificationScheduler.createChannel()` lacks this guard.

**Decision**: Add explicit existence check for consistency with daily
channel pattern, even though Android makes the current behavior safe.

**Rationale**: Consistency with the daily channel pattern makes the
codebase self-documenting and resistant to future Android behavior changes.

**Alternatives considered**:
- Leave as-is (Android makes it safe): Rejected because it violates the
  explicit idempotency contract in FR-016 and creates inconsistency.

### Gap 2: Zero Notification Logging

**Location**: All files in `notification/` package
**Severity**: Medium (FR-020 compliance, debugging difficulty)
**Rule**: FR-020 (log notification operations with tag "LumaDailyNotification")

No `android.util.Log` import exists in any notification file. The system
has zero observability for debugging notification behavior.

**Decision**: Add structured logging with tag "LumaDailyNotification" to
key operations: update start, permission check, channel status,
notification built, notify() called, success/failure, cancel, receiver
fired.

**Rationale**: FR-020 explicitly requires this for debugging. The user
input rules (section 23) specify minimum log points.

**Alternatives considered**:
- Use Timber or custom logger: Rejected because it adds a dependency
  not justified by scope; android.util.Log is sufficient.
- Log only in debug builds: Partially adopted; production logging uses
  configurable levels per user rules section 23.
