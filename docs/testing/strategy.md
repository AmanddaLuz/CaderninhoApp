# Testing strategy

## Test pyramid

- Unit: ViewModels (with fake/in-memory repository), `Formatadores`,
  `WhatsAppUtil` phone normalization.
- Instrumented: Room DAOs and the `ClienteComVendas` relation query.
- Manual: navigation flow, WhatsApp intent hand-off on a physical device.

## Critical scenarios

- Blank name is rejected by `ClientesViewModel.adicionarCliente`.
- CPF accepts empty input, validates Brazilian check digits, is normalized to
  digits and rejects duplicates.
- Client search ignores name case/accents and phone punctuation.
- Blank description or non-positive value is rejected by
  `ClienteDetalheViewModel.registrarVenda`.
- `saldoPendente` only sums `PENDENTE` sales, never `PAGO` ones.
- Marking a sale as paid stamps `pagoEm`; marking it pending clears it.
- `ResumoViewModel` excludes sales outside the current calendar month.
- `ResumoViewModel` groups `PAGO` sales by `FormaPagamento` without double
  counting.
- `WhatsAppUtil` normalizes a 10/11-digit local number to `55<numero>` and
  leaves an already-prefixed `55` number untouched.
- Room cascade delete: removing a client removes its sales.
- Room migration 1 to 2 preserves clients and adds nullable CPF.
- Reminder scheduling must create one unique job per pending sale and cancel
  it when that sale is paid or removed.

## Gates

Android Lint and Detekt fail the build on errors. Kover requires at least 80%
eligible line coverage; `ui/theme`, `ui/navigation`, `di`, Room DAOs, the
`CaderninhoDatabase` class, generated `*ScreenKt` Composable wrappers,
`CaderninhoApp` and `MainActivity` are excluded, since they hold Android
wiring or declarative UI without business rules.

SonarQube Cloud enforces at least 80% coverage on new code through its Quality
Gate. The CI scanner waits for that result and fails the `SonarCloud analysis`
job when the gate fails. Its source-level coverage exclusions mirror Kover's
eligible class set, including passive Compose screens/components,
`Converters` and `WhatsAppUtil`; changing one exclusion list requires updating
the other in the same pull request.
