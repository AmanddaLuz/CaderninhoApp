# Domain specification

## Entities

- `ClientEntity`: `id`, `name`, `phone`, optional normalized `cpf`, `notes`,
  `createdAt`.
- `SaleEntity`: `id`, `clientId`, `paymentMethod`, `status`, optional
  `dueEpochDay`, `createdAt`, `paidAt`.
- `SaleItemEntity`: `id`, `saleId`, `description`, `quantity`,
  `unitValueCents`, `position`.
- `SaleWithItems`: sale relation whose total is calculated in cents from its
  item lines.
- `PaymentMethod`: `CASH`, `PIX`, `CREDIT_CARD`, `DEBIT_CARD`, `OTHER`.
- `PaymentStatus`: `PAID`, `PENDING`.

## Rules enforced today

- `ClientesViewModel.addClient` rejects a blank name.
- `ClientesViewModel.addClient` accepts an empty CPF, validates
  Brazilian check digits when present, stores digits only and rejects a
  duplicate CPF.
- `ClientesViewModel` filters clients by normalized name or phone digits.
- `ClientDetailViewModel.registerSale` rejects a blank description or a
  non-positive quantity/unit value and rejects pending sales without a due
  date.
- `ClientUiModel.pendingBalanceCents` sums only `SaleWithItems` rows with
  `PaymentStatus.PENDING` for that client.
- Starting a charge preselects overdue pending sales and those due today.
  Selection can be changed or expanded to every pending sale, and
  only selected sales compose the WhatsApp total.
- Client history sorts sales by `createdAt` descending and filters them with
  `ALL`, `PENDING` or `PAID`. Status changes update the active filtered
  list reactively.
- `SummaryViewModel` computes the current calendar month window
  (`YearMonth.now()`) once per ViewModel instance and aggregates:
  - `totalReceived`: sum of `PAID` sales in the window.
  - `totalPending`: sum of `PENDING` sales in the window.
  - `byPaymentMethod`: sum of `PAID` sales grouped by `PaymentMethod`.

## Known follow-ups
