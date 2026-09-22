# Feature Specification: Fix Appearance & Theme Controls

**Feature Branch**: `010-fix-appearance-theme`

**Created**: 2026-09-22

**Status**: Draft

**Input**: User description: "Fix the Appearance & Theme controls shown in Settings. The Appearance & Theme section currently renders Liquid Glass, OLED Deep, and Accent Color options, but these controls do not actually produce the expected visual/theme changes. Tapping a theme must actually apply that appearance across the app with a clear selected state, immediately, without restart, and persist after force-stop. Every accent circle must be a functional selection that immediately updates the app's actual accent color wherever the centralized accent token is used, with a clear selected state, persisting after restart. Inspect the existing theme architecture first and connect the UI to the real source of truth. Changes must propagate through app state immediately, existing screens must update without navigating away, and calendar/sheets/buttons/cards/selected states/FAB/tab bar must use the selected accent/theme. Use the existing preferences architecture, no new database or migration, stable keys, preserve existing settings. Do not change RTL/LTR or localization. Scope is only the Appearance & Theme functionality and closely related wiring. Add tests proving theme switching, accent switching, restart persistence, immediate UI reaction, and that existing settings remain unaffected."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Theme Mode Actually Applies (Priority: P1)

As a user, when I tap **Liquid Glass** or **OLED Deep** in Settings → Appearance & Theme, the app's appearance visibly changes to that theme right away, the tapped option shows a clear selected indicator, and my choice is still in effect the next time I open the app.

**Why this priority**: This is the most direct defect — a prominent Settings control that appears to do almost nothing. A theme picker that does not change the theme is broken functionality, not a cosmetic gap.

**Independent Test**: Open Settings → Appearance & Theme, tap each of the two theme options in turn and confirm the rendered appearance differs between them and matches the option tapped, with one option clearly marked as selected; force-stop and relaunch the app and confirm the same theme is applied.

**Acceptance Scenarios**:

1. **Given** the app is showing the Liquid Glass appearance, **When** the user taps "OLED Deep", **Then** the app immediately renders the OLED Deep appearance (noticeably flatter/darker backdrop with reduced ambient glow) without restarting or navigating away.
2. **Given** the app is showing the OLED Deep appearance, **When** the user taps "Liquid Glass", **Then** the app immediately renders the Liquid Glass appearance (layered gradient backdrop with pronounced ambient glow).
3. **Given** either theme is applied, **When** the user looks at the Appearance & Theme section, **Then** exactly one of the two options displays a clear selected indicator and it is the theme currently in effect.
4. **Given** a theme has been selected, **When** the user force-stops and relaunches the app, **Then** the same theme is applied at startup with no extra step.
5. **Given** a first run with no stored appearance choice, **When** the app starts, **Then** the current default theme is applied with no crash and no blank state.

---

### User Story 2 — Accent Color Actually Applies (Priority: P1)

As a user, when I tap one of the accent color circles in Settings, that color becomes the app's accent color everywhere accent color is displayed, the tapped circle shows a clear selected indicator, and the choice survives an app restart.

**Why this priority**: The accent picker is the second control in the same section and is equally non-functional today. It shares the same defect class and the same user-visible symptom.

**Independent Test**: Tap each accent circle in turn and confirm accent-bearing elements across the app change to the selected color; force-stop and relaunch and confirm the last-selected accent is still applied.

**Acceptance Scenarios**:

1. **Given** the app is using accent A, **When** the user taps accent circle B, **Then** every component that renders accent color (action buttons, floating action button, tab bar highlight, cards, sheet headers, selected/highlighted states, calendar day selection) is immediately rendered with accent B.
2. **Given** an accent has been selected, **When** the user views the Accent Color row, **Then** exactly one circle displays a clear selected indicator and it corresponds to the accent currently in effect.
3. **Given** all displayed accent circles, **When** the user taps each one in turn, **Then** every circle performs a selection (none are inert).
4. **Given** an accent has been selected, **When** the user force-stops and relaunches the app, **Then** the same accent is applied at startup.
5. **Given** a first run with no stored accent choice, **When** the app starts, **Then** the default accent is applied with no crash and no blank state.

---

### User Story 3 — Immediate App-Wide Propagation (Priority: P2)

As a user, changing the theme or accent updates every already-visible screen at once, so I do not have to navigate away and come back (or restart) to see the change.

**Why this priority**: Without this, even a correctly wired control would look broken because the change would only appear after a navigation or restart.

**Independent Test**: From Settings, change theme or accent, then switch directly to Calendar and to Search without returning through Settings, and confirm the new appearance/accent is already in effect; also confirm an open sheet or dialog reflects the change.

**Acceptance Scenarios**:

1. **Given** the user changes the accent while on the Settings screen, **When** they switch to the Calendar tab, **Then** accent-bearing calendar elements already show the new accent without any intervening "refresh" step.
2. **Given** the user changes the accent while on the Settings screen, **When** they switch to the Search tab, **Then** accent-bearing search elements already show the new accent.
3. **Given** the user changes the theme while a sheet or confirmation dialog is open, **When** that surface is inspected, **Then** it reflects the current theme/accent.
4. **Given** the user changes either setting, **When** the screen containing the control is inspected immediately after the tap, **Then** the selected indicator and the applied appearance are already updated.

---

### User Story 4 — No Regression to Existing Behavior (Priority: P3)

As a user, fixing Appearance & Theme must not disturb anything else: my other settings stay as they were, layout direction and language stay the same, and calendar, notification, reminder, and event behavior are unchanged.

**Why this priority**: Lowest priority only because it is a guardrail rather than a user-facing capability; it must still hold for the feature to be shippable.

**Independent Test**: Record all other settings, the active calendar type, and layout direction/language before the change; apply theme and accent changes; confirm every recorded value and behavior is unchanged.

**Acceptance Scenarios**:

1. **Given** the user has configured calendar type, snooze duration, category visibility, and notification toggles, **When** they change theme and accent, **Then** all of those settings retain their stored values.
2. **Given** a Persian (Jalali/Hijri) session, **When** theme or accent is changed, **Then** the layout remains right-to-left with the same localized text; a Gregorian session remains left-to-right.
3. **Given** events with reminders and a daily notification, **When** theme or accent is changed, **Then** scheduling and delivery behavior is unaffected.
4. **Given** the user changes theme or accent, **When** calendar navigation, Today-vs-selected behavior, and event create/edit/delete are exercised, **Then** they behave exactly as before.
5. **Given** the user has selected a theme and an accent, **When** every screen and open surface is inspected, **Then** all of them reflect that single selection, with no surface showing a conflicting fixed appearance — there is one selection, not two competing ones.

---

### Edge Cases

- A stored theme value that matches neither offered option falls back to the default theme rather than rendering an unknown state.
- A stored accent selection that falls outside the available accent set is constrained to a valid accent rather than rendering nothing or crashing.
- Selecting the already-selected theme or accent leaves the app in that same consistent state (no flicker, no error, no duplicate state).
- Rapidly alternating between the two themes or across several accents ends with the last-tapped option applied and marked selected.
- Changing appearance while a sheet, dialog, or the time picker is open applies to that open surface too.
- Upgrading an existing install: previously stored appearance choices are honored, and no other stored setting is altered or lost.
- Appearance change while the calendar is scrolled to a non-today date must not alter the selected date or the Today-vs-selected distinction.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Selecting **Liquid Glass** MUST apply the Liquid Glass appearance throughout the app.
- **FR-002**: Selecting **OLED Deep** MUST apply the OLED Deep appearance throughout the app; the two themes MUST be visually distinguishable from each other.
- **FR-003**: Exactly one theme option MUST at all times display a clear selected indicator, and it MUST be the theme currently in effect.
- **FR-004**: A theme change MUST become visible immediately on the current screen, without restarting the app and without navigating away and back.
- **FR-005**: The selected theme MUST persist across force-stop and relaunch.
- **FR-006**: Every displayed accent color circle MUST be a functional selection; tapping it selects that accent.
- **FR-007**: Selecting an accent MUST immediately change the accent color rendered by every component that displays accent color, including action buttons, the floating action button, the tab bar highlight, cards, sheet headers and controls, selected/highlighted states, and calendar day selection.
- **FR-008**: Exactly one accent MUST at all times display a clear selected indicator, and it MUST be the accent currently in effect.
- **FR-009**: An accent change MUST take effect immediately on the current screen and on already-rendered other screens, without restarting the app and without navigating away and back.
- **FR-010**: The selected accent MUST persist across force-stop and relaunch.
- **FR-011**: Theme and accent MUST be driven by a single existing source of truth; no second independent theme state or accent state may be introduced alongside the existing one.
- **FR-012**: Appearance values MUST flow through the existing centralized color/design-token source, so that the selected theme and accent are what the rest of the interface renders; accent-bearing components MUST NOT continue to render a fixed accent that ignores the selection.
- **FR-013**: On first run with no stored appearance values, the current default theme and accent MUST be used, with no crash and no blank state.
- **FR-014**: A stored theme that matches neither offered option MUST fall back to the default theme, and a stored accent that corresponds to no offered accent MUST resolve to a valid accent.
- **FR-015**: Persistence MUST use the existing settings/preferences mechanism; no new database, storage mechanism, or migration may be introduced.
- **FR-016**: Existing preference keys and all previously stored user settings MUST be preserved unchanged.
- **FR-017**: RTL/LTR layout behavior and localization MUST NOT change.
- **FR-018**: Scope MUST be limited to the Appearance & Theme functionality and its closely related wiring. Calendar, notification, reminder, and event behavior MUST NOT change, and the Settings layout MUST NOT be redesigned.

### Key Entities

- **Theme option**: One of the two offered appearances (Liquid Glass, OLED Deep). Attributes: stable identifier, display label, selected state.
- **Accent preset**: One of the offered accent colors. Attributes: stable position/identifier, the color values the interface renders with it, selected state.
- **Appearance preferences**: The stored, user-chosen theme and accent, plus their defaults when nothing has been chosen yet.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In 100% of trials, tapping a theme option produces a visually distinguishable appearance change on that same screen, with no restart and no navigation.
- **SC-002**: In 100% of trials, tapping an accent circle changes the accent rendered by accent-bearing components on that same screen; every offered accent circle (all 5) performs a selection — zero inert controls.
- **SC-003**: Theme and accent survive 100% of force-stop/relaunch cycles, matching the last selection each time.
- **SC-004**: 100% of screens and surfaces that display accent color reflect the current selection immediately, with zero requiring a navigation round-trip or restart.
- **SC-005**: Exactly one theme and exactly one accent are shown as selected at all times, and each matches the appearance actually in effect — zero mismatched indicators.
- **SC-006**: After any appearance change, 100% of other stored settings (calendar type, snooze duration, category visibility, notification toggles) retain their prior values.
- **SC-007**: RTL/LTR direction and localized strings are byte-identical before and after appearance changes for both Persian and English sessions — zero changes.
- **SC-008**: Automated verification covering theme switching, accent switching, restart persistence, immediate propagation, and preservation of existing settings passes with zero failures.
- **SC-009**: Calendar navigation, Today-vs-selected distinction, notifications, reminders, and event create/edit/delete behave identically before and after the change — zero regressions.

## Assumptions

- **Known current behavior (basis for this spec)**: Persisting the theme and accent already works — the chosen theme and accent are stored on selection and restored on startup. The disconnect is downstream: what the app renders does not vary with the selected theme, the selected accent does not reach the components that display accent color, and the selected theme currently affects only a subtle ambient backdrop difference. This spec requires the existing choice to be connected to what is actually rendered, not a second one to be created.
- **Definition of the two themes** follows the direction already implied by the app: Liquid Glass = layered gradient backdrop with pronounced ambient glow; OLED Deep = flat/near-black backdrop with reduced ambient glow. They need only be clearly distinguishable, not redefined from scratch.
- **Accent scope**: "Accent color" means the brand/interactive highlight color (buttons, FAB, tab bar highlight, selection and highlight states, sheet accents). Semantic category colors (Work/Personal/Health/Social/Finance/Holidays) are a separate, deliberate palette and are NOT expected to follow the accent selection.
- **Both color values of an accent preset** (its primary and its secondary) belong to the selected preset and should be applied together where the design uses a paired accent.
- **Settings screen layout and labels are unchanged**; only the behavior behind the existing controls changes.
- **Existing preference keys are reused**; no renaming, no additional storage, no data migration.
- **Defaults on first run** remain the current ones: the current default theme and the first offered accent.
- The two theme chips and the accent circles keep their existing labels, order, and positions; only their functional effect and selected-state clarity are in scope.
