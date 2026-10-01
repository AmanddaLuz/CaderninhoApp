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
3. Register a sale/service with description, value, payment method and an
   expected payment date when pending.
4. Mark a sale as paid or pending (fiado) at any time.
5. Schedule a local reminder for the expected payment date. The shopkeeper
   taps the notification to open a pre-filled WhatsApp charge and manually
   confirms sending it.
6. Monthly summary: total received, total pending and totals by payment
   method.

## Critical rules

- A client requires at least a name to be saved.
- CPF is optional, but must pass Brazilian check-digit validation and be
  unique when provided.
- Client search is case- and accent-insensitive for names and ignores phone
  formatting.
- A sale/service requires a positive value and a non-blank description.
- A pending sale requires an expected payment date.
- Pending balance is the sum of a client's sales with `PENDENTE` status.
- Marking a sale as paid stamps `pagoEm` with the current time; marking it as
  pending clears `pagoEm`.
- The WhatsApp charge only appears when the client has a pending balance.
- The WhatsApp charge normalizes the phone to `55<ddd><numero>` and never logs
  the message content.
- WhatsApp is never launched directly from background work. The scheduled
  worker posts a notification and requires an explicit user tap.
- The monthly summary only aggregates sales created within the current
  calendar month.

## Payment states

```text
PENDENTE -> PAGO
PAGO -> PENDENTE
```

Both transitions are user-initiated; there is no automatic status change.
