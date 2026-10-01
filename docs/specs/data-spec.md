# Data specification

## Persistence

- Room (`CaderninhoDatabase`, version 3) is the single local source of truth.
- `ClienteEntity`, `VendaEntity` and `VendaItemEntity` are the persisted
  tables. Sales cascade from clients, and items cascade from sales.
- `ClienteEntity.cpf` is nullable and has a unique index. Migration 1 to 2
  adds it without changing existing rows.
- Migration 2 to 3 rebuilds the sale table with an optional expected-payment
  epoch day and converts every legacy description/value into one quantity-1
  item with its value rounded to integer cents.
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
