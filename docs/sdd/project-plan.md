# SDD project plan

**Status:** In progress
**Repository:** `AmanddaLuz/CaderninhoApp` (public, GitHub Actions CI/CD live)
**UI:** Jetpack Compose

## Delivery phases

<!-- markdownlint-disable MD013 -->

| Phase | Outcome |
| --- | --- |
| 0. Foundation | Completed: Compose host, Room + Hilt wiring, quality gates, documentation, GitFlow, branch protection on `main`/`develop`, and CI/CD (lint, Detekt, tests, Kover 80%, build, SonarCloud) |
| 1. Clientes | Completed: cliente registration, list with pending-balance highlight |
| 2. Vendas e fiado | Completed: sale/service registration, payment status, paid/pending toggle |
| 3. Cobrança WhatsApp | Completed: `wa.me` deep link charge with pending balance message |
| 4. Resumo mensal | Completed: monthly received/pending totals by payment method |
| 5. Sincronização Firebase | Planned: optional cloud backup/sync on top of the local Room source of truth |
| 6. Recibo e exportação | Planned: shareable receipt per sale/cliente |
| 7. Hardening | Planned: requirement audit, final validation and documentation |

<!-- markdownlint-enable MD013 -->

Each phase uses a short-lived branch from `develop`. A phase is complete only
when its behavior, tests and canonical documentation agree. Every phase branch
opens a PR into `develop`; only `develop` may open a PR into `main`, both
gated by the required CI checks (`Branch policy`, `Lint, Detekt and unit
tests`, `Coverage 80%`, `Debug build`).

## Acceptance

- Jetpack Compose and Material 3 are the only presentation stack.
- Stateless, passive Composables driven by immutable UI state.
- MVVM with Room as the local source of truth; Firebase sync is additive, not
  a replacement.
- Reusable, generic Composable components (dialogs, status badges) before
  feature-specific duplication.
- Lint, Detekt, tests, coverage and build pass.
