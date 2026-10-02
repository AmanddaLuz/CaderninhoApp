# Architecture

The project uses MVVM in a single Android module. Package boundaries stay
explicit without introducing premature Gradle modules.

```text
Composable screen -> ViewModel (Hilt) -> LedgerRepository -> Room DAO
                                                                -> (future) Firebase adapter
                                      -> Reminder coordinator -> WorkManager
```

## Layers

- `domain/model`: pure enums (`PaymentMethod`, `PaymentStatus`).
- `data/local`: Room entities, `Converters`, `ClientWithSales` relation and
  `LedgerDatabase`.
- `data/local/dao`: `ClientDao`, `SaleDao`.
- `data/repository`: `LedgerRepository`, the only dependency ViewModels
  are allowed to hold for persistence.
- `ui/screens/*`: one package per screen area (`home`, `clients`, `sale`,
  `summary`), each with its Hilt `ViewModel` and Composable screen.
- `ui/components`: generic, reusable Composables with no feature dependency.
- `ui/navigation`: `Destination` route model and `LedgerNavHost`.
- `ui/theme`: Material 3 color scheme and typography.
- `di`: Hilt modules (composition root for Room).
- `notification`: a business-facing reminder coordinator plus Android
  adapters for WorkManager, notification channels and the due-date worker.
- `util`: `WhatsAppLauncher`, `Formatters` — small, side-effect-isolated
  helpers with no Android UI dependency beyond `Context`/`Toast`.

Code identifiers, file names and package names are written in English.
Portuguese is retained for user-facing copy and compatibility-sensitive
persisted values, including legacy Room names and existing WorkManager keys.
Renamed entity properties use `@ColumnInfo`, and enum converters map English
constants to the existing Portuguese persisted values, so database version 3
remains backward-compatible without a migration.

Composable screens are passive: they collect `StateFlow` state and dispatch
user events to the ViewModel. Validation, aggregation and persistence stay in
ViewModels and the repository.

The reminder coordinator reconciles one unique job per client/due-date after
every relevant sale transition. Workers always re-read Room before notifying,
so stale, paid or removed sales never produce a charge reminder. Android-only
notification adapters do not build or persist WhatsApp message content.

## Implemented rules

- `ClientsViewModel` derives `pendingBalanceCents` per client from pending sales.
- `ClientDetailViewModel` rejects invalid sale input before persisting.
- `SummaryViewModel` computes a fixed current-month window once per instance.
- `HomeViewModel` derives privacy-safe aggregate counts and totals from the
  Room stream. Value visibility is ephemeral and defaults to hidden.

See `../specs/domain-spec.md`, `../specs/data-spec.md` and
`../specs/ui-foundation-spec.md` for the canonical behavior.
