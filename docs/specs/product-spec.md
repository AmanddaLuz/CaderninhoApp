# Product specification

## Goal

Let small businesses (mercadinhos, salões, manicures, vendedores autônomos)
control clients, sales/services and pending payments ("fiado"), and send
payment reminders through WhatsApp.

## Functional scope

1. Cliente registration with name, phone (WhatsApp), optional validated CPF
   and optional note.
2. Client list showing an "em dia" or pending-balance badge, with search by
   name or phone.
3. Register a sale/service with one or more item lines. Each line has a
   description, quantity and unit value; the sale total is calculated from
   those lines.
4. A pending sale records an expected payment date.
5. Mark a sale as paid or pending (fiado) at any time.
6. Schedule one local reminder per client and expected-payment date. The shopkeeper
   taps the notification to open a pre-filled WhatsApp charge and manually
   confirms sending it.
7. Select which pending sales to charge. Overdue sales and sales due today
   start selected, and the shopkeeper may select, clear or select all pending
   sales.
8. Monthly summary: total received, total pending and totals by payment
   method.
9. Client history in reverse chronological order with All, Pending and Paid
   status filters.

## Critical rules

- A client requires at least a name to be saved.
- CPF is optional, but must pass Brazilian check-digit validation and be
  unique when provided.
- Client search is case- and accent-insensitive for names and ignores phone
  formatting.
- A sale/service requires at least one item with a non-blank description,
  positive quantity and positive unit value.
- The sale total is the sum of each rounded item subtotal (`quantity × unit
  value`), with monetary values persisted in integer cents.
- A pending sale requires an expected payment date.
- Pending balance is the sum of a client's sales with `PENDENTE` status.
- Marking a sale as paid stamps `pagoEm` with the current time; marking it as
  pending clears `pagoEm`.
- The WhatsApp charge only appears when the client has pending sales and uses
  only the explicitly selected sales in its message and total.
- The WhatsApp charge normalizes the phone to `55<ddd><numero>` and never logs
  the message content.
- WhatsApp is never launched directly from background work. The scheduled
  worker posts a notification and requires an explicit user tap.
- The monthly summary only aggregates sales created within the current
  calendar month.
- Client history defaults to All and keeps the selected status filter while
  Room emits sale updates.

## Payment states

```text
PENDENTE -> PAGO
PAGO -> PENDENTE
```

Both transitions are user-initiated; there is no automatic status change.
