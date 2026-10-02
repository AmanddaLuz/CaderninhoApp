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
| 5. Busca e CPF | Completed: client search by name/phone and optional validated CPF |
| 6. Itens, vencimento e lembrete | Completed: itemized sales, expected payment date, grouped local notification and selective user-confirmed WhatsApp hand-off |
| 7. Histórico do cliente | Completed: chronological sales with All, Pending and Paid filters |
| 8. English code naming | Completed: English identifiers, files and packages with Portuguese UI copy and backward-compatible Room storage |
| 9. Resumo diário e mensal | Completed: navigable daily/monthly periods, received totals by payment date, pending totals by creation date and pending-sale due-date rescheduling |
| 10. Home privada e identidade visual | Completed: privacy-first start destination, hidden financial totals, notebook visual language and explicit selected states |
| 11. Configurações e privacidade | Planned: optional biometric/device-credential lock, privacy controls and secure notification deep links |
| 12. Preparação Play Store | In progress: API 36, signed AAB, store listing, privacy policy, Data Safety, closed testing and staged rollout |
| 13. Sincronização Firebase | Planned: optional account-based cloud backup/sync on top of the local Room source of truth |
| 14. Recibo e exportação | Planned: shareable receipt per sale/client |
| 15. Hardening | Planned: requirement audit, final validation and documentation |

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
