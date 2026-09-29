# ADR 0002: Room as source of truth, Firebase sync deferred

**Status:** Accepted

## Decision

Persist clients and sales locally with Room. Add Firebase Firestore/Auth as
version-catalog dependencies now, but implement the actual sync adapter in a
later phase, behind the existing `CaderninhoRepository` contract.

## Rationale

Small merchants need the app to work fully offline on day one. Building the
sync adapter later, without changing the repository contract consumed by
ViewModels, avoids a rewrite of the presentation layer when sync ships.

## Consequences

- `CaderninhoRepository` remains the single dependency for ViewModels; a
  future `FirebaseCaderninhoRepositoryImpl` or a decorator over the Room
  implementation must satisfy the same contract.
- No `google-services.json` is committed yet; the `google-services` Gradle
  plugin is declared but not applied to the `app` module until sync is
  implemented, to avoid a broken build for contributors without the file.
- See `docs/sdd/project-plan.md` phase 5 for the sync rollout plan.
