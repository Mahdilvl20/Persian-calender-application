# Implementation Plan: Remove Redundant Notification Header

**Branch**: `007-notification-remove-header` | **Date**: 2026-09-17 | **Spec**: [spec.md](spec.md)

## Summary

Remove the redundant internal "Luma Calendar" header (ImageView +
TextView) from both notification XML layouts. The Android system
notification header already provides "Luma Calendar · now". The
notification body should start directly with the primary date.

## Technical Context

**Language/Version**: Kotlin 2.2.10, XML RemoteViews

**Primary Dependencies**: Android NotificationCompat, RemoteViews

**Storage**: N/A

**Testing**: Manual visual verification

**Target Platform**: Android 7.0+ (API 24)

**Project Type**: Mobile app

**Scale/Scope**: 2 XML files, ~10 lines removed per file

## Constitution Check

| Principle | Status | Notes |
|-----------|--------|-------|
| All | PASS | No principle violations — minimal XML change |

## Project Structure

### Source Code (files to modify)

```text
app/src/main/res/layout/
├── notification_luma_calendar.xml          # Remove internal header
└── notification_luma_calendar_expanded.xml # Remove internal header
```

**Structure Decision**: Remove the header LinearLayout (ImageView +
TextView) from both layouts. All other content remains.
