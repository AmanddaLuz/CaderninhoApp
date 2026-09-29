# Product specification

## Goal

Let small businesses (mercadinhos, salões, manicures, vendedores autônomos)
control clients, sales/services and pending payments ("fiado"), and send
payment reminders through WhatsApp.

## Functional scope

1. Cliente registration with name, phone (WhatsApp) and optional note.
2. Client list showing an "em dia" or pending-balance badge.
3. Register a sale/service with description, value and payment method.
4. Mark a sale as paid or pending (fiado) at any time.
5. Send a WhatsApp charge with the client's pending balance.
6. Monthly summary: total received, total pending and totals by payment
   method.

## Critical rules

- A client requires at least a name to be saved.
- A sale/service requires a positive value and a non-blank description.
- Pending balance is the sum of a client's sales with `PENDENTE` status.
- Marking a sale as paid stamps `pagoEm` with the current time; marking it as
  pending clears `pagoEm`.
- The WhatsApp charge only appears when the client has a pending balance.
- The WhatsApp charge normalizes the phone to `55<ddd><numero>` and never logs
  the message content.
- The monthly summary only aggregates sales created within the current
  calendar month.

## Payment states

```text
PENDENTE -> PAGO
PAGO -> PENDENTE
```

Both transitions are user-initiated; there is no automatic status change.
