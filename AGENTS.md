# Agent guide

## Source of truth

- Product behavior: `docs/specs/product-spec.md`
- Delivery phases: `docs/sdd/project-plan.md`
- Architecture: `docs/architecture/overview.md`
- Tests and quality gates: `docs/testing/strategy.md`
- Decisions: `docs/adr/`

## Non-negotiable rules

- Jetpack Compose with Material 3 as the only presentation stack.
- MVVM: stateless Composables, immutable UI state, Hilt-injected ViewModels.
- Room is the local source of truth; a future Firebase sync adapter must
  satisfy the existing `CaderninhoRepository` contract (see ADR 0002).
- Reusable, generic Composable components (`ui/components`) before
  feature-specific duplication — mirror `CampoFormularioDialog`/`SeloStatus`.
- Never log or persist WhatsApp message content beyond what is needed to send
  it (see ADR 0003).
- Keep `minSdk 24`; enable core library desugaring for `java.time` usage.
- Update the canonical document when behavior or a decision changes.

## Workflow

1. Work from an updated `develop` branch using a short-lived branch.
2. Read the applicable spec and ADR.
3. Implement the smallest complete vertical change.
4. Add deterministic unit tests for ViewModel rules and state transitions.
5. Run the smallest applicable validation (`./gradlew testDebugUnitTest`,
   `lintDebug`, `detekt`, or `koverVerifyDebug` as relevant).
6. Record relevant AI constraints or decisions in `docs/agent-harness/`.
