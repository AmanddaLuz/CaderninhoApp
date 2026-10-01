# ADR 0003: WhatsApp charge via wa.me deep link

**Status:** Accepted

## Decision

Send payment reminders by opening WhatsApp through the public `wa.me`
universal link (`Intent.ACTION_VIEW`), instead of the WhatsApp Business
Cloud API.

## Rationale

The Cloud API requires a paid, verified WhatsApp Business account and a
backend to hold credentials. A `wa.me` deep link needs no credentials, no
backend and no per-message cost, at the price of the user manually pressing
send inside WhatsApp.

## Consequences

- `WhatsAppLauncher.sendCharge` builds the link from digits-only phone number
  normalized to `55<ddd><numero>` and a URL-encoded message; it never stores
  or logs the message content.
- If WhatsApp is not installed, the intent launch fails silently into a
  toast; there is no fallback channel today.
- Revisit this ADR if/when a paid, server-side send confirmation becomes a
  product requirement.
