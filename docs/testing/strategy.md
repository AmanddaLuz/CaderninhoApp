# Testing strategy

## Test pyramid

- Unit: ViewModels (with fake/in-memory repository), `Formatters`,
  `WhatsAppLauncher` phone normalization.
- Instrumented: Room DAOs and the `ClientWithSales` relation query.
- Manual: navigation flow, WhatsApp intent hand-off on a physical device.

## Critical scenarios

- Blank name is rejected by `ClientesViewModel.addClient`.
- CPF accepts empty input, validates Brazilian check digits, is normalized to
  digits and rejects duplicates.
- Client search ignores name case/accents and phone punctuation.
- Client phone input accepts at most 11 digits, displays the Brazilian mobile
  mask progressively and persists digits only.
- Blank description or non-positive value is rejected by
  `ClientDetailViewModel.registerSale`.
- Itemized sales reject blank descriptions and non-positive quantities/unit
  values, calculate totals in cents and migrate legacy sales to one item.
- Pending sales require a due date; paid sales do not schedule reminders.
- - `pendingBalanceCents` only sums `PENDING` sales, never `PAID` ones.
- Marking a sale as paid stamps `paidAt`; marking it pending clears it.
- `SummaryViewModel` excludes sales outside the current calendar month.
- Summary periods use `paidAt` for received sales, `createdAt` for pending
  sales and exclusive end boundaries for both daily and monthly navigation.
- `SummaryViewModel` groups received sales by `PaymentMethod` without double
  counting.
- `WhatsAppLauncher` normalizes a 10/11-digit local number to `55<numero>` and
  leaves an already-prefixed `55` number untouched.
- Room cascade delete: removing a client removes its sales.
- Room migration 1 to 2 preserves clients and adds nullable CPF.
- Reminder scheduling must create one unique job per pending sale and cancel
  client/date, reconcile it when a sale is paid or removed, and never notify
  when the database no longer has a pending sale for that group.
- Charge selection defaults to overdue and today-due sales, supports
  select-all and totals only selected sales.
- Client history defaults to All, sorts newest first and filters Pending/Paid
  reactively when a sale changes status.
- Rescheduling rejects paid sales and dates that are not in the future,
  cancels the previous reminder group and schedules the new one.
- Home state contains aggregate data only, starts financial values hidden and
  correctly counts pending, overdue and today-due sales.
- Home value visibility toggles in memory and is not persisted.

## Gates

Android Lint warnings and Detekt findings fail the build. Version-update
advisories for SDKs, plugins and dependencies remain informational and are
excluded from the Lint gate. Kover requires at least 80% eligible line
coverage; `ui/theme`, `ui/navigation`, `di`, Room DAOs, the
`LedgerDatabase` class, generated `*ScreenKt` Composable wrappers,
feature dialog and sale-card Composables, `LedgerApplication`, `MainActivity`
and Android-only
notification adapters (`Worker`, `NotificationManager`, WorkManager
scheduler) are excluded, since they hold Android wiring or declarative UI
without business rules. Reminder coordination, sale rules, ViewModels and
repositories remain covered.

SonarQube Cloud enforces at least 80% coverage on new code through its Quality
Gate. The CI scanner waits for that result and fails the `SonarCloud analysis`
job when the gate fails. Its source-level coverage exclusions mirror Kover's
eligible class set, including passive Compose screens/components,
`Converters` and `WhatsAppLauncher`; changing one exclusion list requires updating
the other in the same pull request.
