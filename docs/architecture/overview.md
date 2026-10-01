# Architecture

The project uses MVVM in a single Android module. Package boundaries stay
explicit without introducing premature Gradle modules.

```text
Composable screen -> ViewModel (Hilt) -> CaderninhoRepository -> Room DAO
                                                                -> (future) Firebase adapter
                                      -> Reminder coordinator -> WorkManager
```

## Layers

- `domain/model`: pure enums (`FormaPagamento`, `StatusPagamento`).
- `data/local`: Room entities, `Converters`, `ClienteComVendas` relation,
  `CaderninhoDatabase`.
- `data/local/dao`: `ClienteDao`, `VendaDao`.
- `data/repository`: `CaderninhoRepository`, the only dependency ViewModels
  are allowed to hold for persistence.
- `ui/screens/*`: one package per screen area (`clientes`, `venda`,
  `resumo`), each with its Hilt `ViewModel` and Composable screen.
- `ui/components`: generic, reusable Composables with no feature dependency.
- `ui/navigation`: `Destino` route model and `CaderninhoNavHost`.
- `ui/theme`: Material 3 color scheme and typography.
- `di`: Hilt modules (composition root for Room).
- `notification`: a business-facing reminder coordinator plus Android
  adapters for WorkManager, notification channels and the due-date worker.
- `util`: `WhatsAppUtil`, `Formatadores` — small, side-effect-isolated
  helpers with no Android UI dependency beyond `Context`/`Toast`.

Composable screens are passive: they collect `StateFlow` state and dispatch
user events to the ViewModel. Validation, aggregation and persistence stay in
ViewModels and the repository.

The reminder coordinator reconciles one unique job per client/due-date after
every relevant sale transition. Workers always re-read Room before notifying,
so stale, paid or removed sales never produce a charge reminder. Android-only
notification adapters do not build or persist WhatsApp message content.

## Implemented rules

- `ClientesViewModel` derives `saldoPendente` per client from pending sales.
- `ClienteDetalheViewModel` rejects invalid sale input before persisting.
- `ResumoViewModel` computes a fixed current-month window once per instance.

See `../specs/domain-spec.md`, `../specs/data-spec.md` and
`../specs/ui-foundation-spec.md` for the canonical behavior.
