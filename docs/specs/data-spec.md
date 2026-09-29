# Data specification

## Persistence

- Room (`CaderninhoDatabase`, version 1) is the single local source of truth.
- `ClienteEntity` and `VendaEntity` are the only tables; `VendaEntity` has a
  cascading foreign key to `ClienteEntity` and an index on `clienteId`.
- `Converters` maps `FormaPagamento`/`StatusPagamento` enums to their `name`
  string for storage.
- `ClienteComVendas` is a `@Relation` projection used for list and detail
  screens; it is not a persisted table.

## Repository

- `CaderninhoRepository` is the only entry point consumed by ViewModels; DAOs
  are never injected directly into presentation code.
- All reads are exposed as `Flow`; all writes are `suspend` functions.
- Firebase Firestore/Auth dependencies are declared in the version catalog for
  a future sync adapter behind the same `CaderninhoRepository` contract; no
  network call exists yet (see ADR 0002).

## Dependency injection

- `DatabaseModule` (Hilt `SingletonComponent`) builds `CaderninhoDatabase` and
  exposes both DAOs.
- `CaderninhoRepository` is constructor-injected with `@Inject` and scoped
  `@Singleton`.
