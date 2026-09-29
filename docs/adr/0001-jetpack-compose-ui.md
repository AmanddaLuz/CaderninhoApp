# ADR 0001: Jetpack Compose UI

**Status:** Accepted

## Decision

Use Jetpack Compose with Material 3 as the only presentation stack, instead of
native XML with ViewBinding.

## Rationale

Unlike CieloTickets (constrained to Cielo Smart terminals on Android 10/API
29), Caderninho targets general consumer Android phones (API 24+) sold
through the Play Store. Compose gives a faster iteration loop for a
CRUD-heavy, form-heavy app with no dependency on legacy terminal hardware.

## Consequences

- Screens are stateless Composables driven by `StateFlow`-backed immutable UI
  state from Hilt ViewModels.
- Reusable UI building blocks are plain `@Composable` functions in
  `ui/components`, not custom Views or XML `<merge>` layouts.
- Detekt's `FunctionNaming` rule ignores `@Composable` functions to allow the
  PascalCase Compose naming convention.
