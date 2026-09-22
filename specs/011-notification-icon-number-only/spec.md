# Feature Specification: Notification Icon Is Day Number Only

**Feature Branch**: `011-notification-icon-number-only`

**Created**: 2026-09-22

**Status**: Draft

**Input**: User description: "Update ONLY the Daily/Persistent Notification small icon design. The current icon contains a small calendar-shaped frame with the Jalali/Persian day number inside, and the number is too small. Change it so the icon consists ONLY of the current Jalali/Persian day number: completely remove the calendar outline/frame, draw no calendar shape, border, square, page, or surrounding container; use a clean bold highly readable font; make the number as large as reasonably possible within notification small-icon constraints; center it precisely horizontally and vertically; support single- and double-digit days; automatically scale double-digit numbers so they stay fully visible without clipping; keep the number visually balanced at actual notification size. Behavior must remain unchanged: the number must be the REAL current local Jalali day, must update when the local date changes, and must keep working after reboot, timezone/date changes, and app restart. This applies ONLY to the Daily/Persistent notification — do not change the Event Reminder icon, the launcher icon, or any notification text, layout, channel, ID, or scheduling. Inspect the existing dynamic notification icon generation code and modify the existing implementation rather than creating a parallel icon system. Add or update tests for single-digit day, double-digit day, proper centering, no clipping, and readable rendering at actual notification icon size, then run the tests and build."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Icon Shows Only the Day Number (Priority: P1)

As a user glancing at my notification shade, I see the current Jalali day number and nothing else — no calendar outline, no binder tabs, no header bar, no container around it.

**Why this priority**: Removing the frame is the headline of the request and the thing the user will notice immediately; without it the rest of the redesign still reads as "a calendar icon".

**Independent Test**: Render the icon for any day and inspect it — the only visible content must be the day number's glyphs; there must be no rectangular outline, no header separator line, and no tabs anywhere in the image.

**Acceptance Scenarios**:

1. **Given** the daily notification icon for day ۲۲, **When** it is rendered, **Then** the image contains glyph pixels for "۲۲" and no calendar frame, border, square, page, or other container pixels.
2. **Given** the icon for any day in 1–31, **When** it is inspected, **Then** the only opaque content is the number — the background outside the glyphs is fully transparent.
3. **Given** the previous frame-based icon, **When** this change ships, **Then** the outline, header separator, and binder tabs are gone entirely rather than merely thinned or recolored.

---

### User Story 2 — Number Is Large, Centered, and Never Clipped (Priority: P1)

As a user, the day number is the largest thing that fits, sits dead-center in the icon, and reads cleanly at actual notification size — whether today is a single digit or a double digit.

**Why this priority**: This is the stated reason for the change ("the number is currently too small") and only works if both digit widths are handled.

**Independent Test**: Generate the icon for a single-digit day (e.g. ۹) and a double-digit day (e.g. ۳۱); verify each is centered on both axes, occupies a large share of the usable area, and has zero pixels cut off at the edges.

**Acceptance Scenarios**:

1. **Given** day ۹, **When** the icon is rendered, **Then** the glyph is centered horizontally and vertically on the icon's center and is noticeably larger than it is today.
2. **Given** day ۳۱, **When** the icon is rendered, **Then** both digits are fully visible with no clipping at any edge and the pair is still centered on both axes.
3. **Given** any day 1–31, **When** the icon is rendered, **Then** the whole number stays inside the safe area the system uses when it masks small icons, so the system never cuts it off.
4. **Given** the icon scaled down to its actual notification size, **When** it is viewed, **Then** the day number is legible and visually balanced — neither digits nor empty margin dominate.
5. **Given** a single-digit day followed by a double-digit day, **When** both are compared, **Then** neither looks undersized or drifting off-center relative to the other.

---

### User Story 3 — Nothing Else Changes (Priority: P2)

As a user, only the daily notification's picture changes. The day it shows, when it updates, its text, and every other icon in the app behave exactly as before.

**Why this priority**: Guardrail rather than a new capability — it must hold for the change to be shippable, but it delivers no user-visible value on its own.

**Independent Test**: Compare the icon source, the event-reminder icon, the launcher icon, and the daily notification's text/channel/ID/scheduling before and after; every one must be identical apart from the daily icon's artwork.

**Acceptance Scenarios**:

1. **Given** the local date crosses midnight, **When** the daily notification is refreshed, **Then** it shows the new real local Jalali day, exactly as it does today.
2. **Given** a reboot, a timezone change, or a date change, **When** the device settles, **Then** the daily notification shows the correct real local Jalali day with no further user action.
3. **Given** this change, **When** an Event Reminder fires, **Then** its icon is unchanged.
4. **Given** this change, **When** the app icon is viewed on the launcher, **Then** it is unchanged, including any day-based variants.
5. **Given** this change, **When** the daily notification is inspected, **Then** its text, layout, channel, identifier, priority, and schedule are unchanged.
6. **Given** this change, **When** the icon code is reviewed, **Then** it is a modification of the existing generation routine, not a second icon system alongside it.

---

### Edge Cases

- Day 1 (single digit) and day 31 (maximum double digit) must both render fully, centered, and large.
- Persian digit glyphs and Latin digit glyphs both occur, because the active calendar type decides which glyph style is drawn; both must be centered and unclipped.
- Persian digits can be visually wider or narrower than their Latin counterparts at the same nominal size, so the fit rule must not assume a fixed width.
- The system masks small icons to a rounded/circular region; a number that merely fits the square icon image can still be cut off by that mask.
- Two digits must not be scaled down so far that they lose the "larger than today" improvement the change is meant to deliver.
- The day is always the Jalali day even when the user is browsing another calendar type; that must not change.
- A day number must never be rendered blank, placeholder, or as a substitute value if the source date is momentarily unavailable.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Daily/Persistent notification small icon MUST consist only of the current day number. No calendar outline, border, square, page, header bar, binder tab, rounded container, background tile, or any other surrounding shape may be drawn.
- **FR-002**: The day number MUST be drawn in a clean, bold, highly readable typeface consistent with the bold face the app already uses.
- **FR-003**: The day number MUST be rendered as large as reasonably possible while remaining entirely within the safe area the system uses when it masks small icons — no part of any glyph may fall outside it.
- **FR-004**: The number MUST be centered precisely on both axes: its visual center coincides with the icon's horizontal and vertical center.
- **FR-005**: Single-digit days MUST render centered and large, with visual weight comparable to double-digit days — no vertical or horizontal drift caused by the missing second digit.
- **FR-006**: Double-digit days MUST be scaled automatically so both digits are fully visible with no clipping, and MUST remain centered.
- **FR-007**: The number MUST represent the real current local Jalali day, drawn exactly as it is drawn today (glyph style follows the existing rule tied to the active calendar type).
- **FR-008**: The icon MUST update when the local date changes.
- **FR-009**: The icon MUST show the correct day after a reboot, after a timezone or date change, and after an app restart, with no user action.
- **FR-010**: This change MUST apply only to the Daily/Persistent notification.
- **FR-011**: The Event Reminder notification icon MUST NOT change.
- **FR-012**: The launcher icon, including any day-based launcher variants, MUST NOT change.
- **FR-013**: Notification text, layout, channel, identifier, priority, schedule, and all other notification behavior MUST NOT change.
- **FR-014**: The existing icon-generation implementation MUST be modified in place; a second or parallel icon system MUST NOT be introduced.
- **FR-015**: At the icon's actual notification size, the number MUST be legible — rendered as a solid, fully-opaque glyph with no gaps, notches, or clipped edges.
- **FR-016**: Automated coverage MUST exist for single-digit days, double-digit days, horizontal and vertical centering, absence of clipping, and legibility at actual notification size, and the pre-existing tests MUST continue to pass.

### Key Entities

- **Daily notification day icon**: A square image whose only content is the day number. Attributes: the day value it encloses, its glyph style, the bounding box of the rendered number, and its center relative to the icon's center.
- **Day value**: The real current local Jalali day (1–31) the icon must display; it may appear as Persian or Latin glyphs depending on the active calendar type, but the underlying day is always the Jalali day.
- **Safe area**: The central region of the icon that survives the system's small-icon mask; the number's bounding box must fit entirely inside it.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For 100% of days 1–31, the rendered icon contains day-number glyphs and **zero** frame pixels — no outline, border, header bar, tab, or container edge anywhere in the image.
- **SC-002**: For 100% of days 1–31, the number's bounding-box center is within 1% of the icon's center on **both** the horizontal and vertical axes.
- **SC-003**: For 100% of single-digit and double-digit days, **zero** glyph pixels fall outside the safe area — no clipping by the icon edge and none by the system's mask.
- **SC-004**: For 100% of days 1–31, the number's bounding box fills at least 60% of the safe area along its constraining dimension, so the icon is dominated by the number rather than by empty margin.
- **SC-005**: Single-digit and double-digit days differ in size by no more than the difference needed to keep both fully inside the safe area — no day renders conspicuously smaller than its neighbors.
- **SC-006**: At actual notification size, 100% of sampled icons present a fully-opaque, gap-free glyph that a reader can identify as the correct day on first look.
- **SC-007**: 100% of date changes (midnight rollover, timezone change, reboot, app restart) result in the daily notification showing the correct real local Jalali day with no user action.
- **SC-008**: The Event Reminder icon, the launcher icon and its day variants, and the daily notification's text, channel, identifier, priority, and schedule are **unchanged** before and after — zero differences in appearance or behavior.
- **SC-009**: Automated coverage for single-digit, double-digit, centering, no-clipping, and legibility-at-size passes with zero failures, together with every pre-existing test.

## Testing Requirements

- **TR-001**: Single-digit Jalali day (e.g. ۹) — renders only the glyph, centered, unclipped, and large.
- **TR-002**: Double-digit Jalali day (e.g. ۳۱) — both digits fully visible, unclipped, centered, and scaled to fit.
- **TR-003**: Proper centering — glyph bounding-box center matches the icon's center on both axes, for single- and double-digit inputs.
- **TR-004**: No clipping — no glyph pixel lies outside the safe area or the image bounds, for the widest and tallest cases.
- **TR-005**: Readable at actual notification size — the glyph is fully opaque and covers a sufficient share of the safe area at the size the notification actually uses.
- **TR-006**: No calendar frame — asserting the absence of the previous outline, header bar, and tabs.
- **TR-007**: No regression — all pre-existing tests continue to pass.

## Assumptions

- **Which routine is in scope**: the daily notification uses one specific icon routine, the one that produces its small icon. A second, coloured glassmorphism version of the day icon also exists but is wired to no notification at all (only a test refers to it); it is **out of scope**, because this change applies only to the Daily/Persistent notification and that version is not what that notification uses.
- **"Bold, clean typeface"** means the bold face already bundled with and already used by the current icon; no new font is introduced.
- **"As large as reasonably possible"** is bounded by the system's small-icon mask: the number must fit the safe area, so size is maximized subject to zero clipping rather than to a fixed percentage of the icon.
- **Sizing rule**: single-digit and double-digit numbers are scaled independently so each fills as much of the safe area as its own width allows; a fixed per-digit-count size is acceptable only if it still meets SC-003 and SC-004.
- **Digit glyph style stays exactly as it is today** (Persian digits while the Jalali calendar type is active, otherwise Latin digits of the same Jalali day) — this is existing behaviour and FR-013 forbids changing it.
- **The day remains the Jalali day** regardless of which calendar type the user is browsing; only the glyph style varies.
- **Square output, unchanged default size**: the icon stays square and keeps its current default resolution; only its contents change.
- **No new asset, no new system**: the artwork is still produced on the fly by the existing routine (FR-014); no static image asset is added for the daily icon.
- **Existing frame-related tests are not design tests**: the current tests only assert image dimensions and that opaque glyph pixels exist, so they survive the design change; the new tests in TR-001..TR-006 add the missing geometric coverage.
