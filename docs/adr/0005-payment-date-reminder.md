# ADR 0005: Payment-date reminder with explicit WhatsApp hand-off

**Status:** Accepted

## Decision

- A pending sale stores an expected payment date.
- WorkManager schedules one unique, deferrable job per sale for 09:00 in the
  device's local time on that date.
- When due, background work posts a local notification. It never starts
  WhatsApp or another activity directly.
- Tapping the notification opens the app's existing `wa.me` hand-off with a
  pre-filled charge. The shopkeeper explicitly confirms sending in WhatsApp.
- Paying or removing a sale cancels its scheduled reminder. Returning a sale
  to pending requires a future expected payment date and schedules a new job.

## Rationale

Android restricts background activity launches, and a `wa.me` link cannot
send unattended messages. WorkManager is appropriate for a date-based
business reminder that does not require exact-alarm permission or exact
second delivery. This keeps the MVP local, avoids backend credentials and
remains consistent with ADR 0003.

## Consequences

- Android 13+ requires runtime notification permission. Denial must not block
  sale registration, but the UI must explain that reminders cannot appear.
- Battery optimization may delay execution beyond 09:00; the date is the
  product guarantee, not exact delivery time.
- Fully unattended sending remains out of scope. It would require a new ADR,
  WhatsApp Business Cloud API, a secure backend, approved templates, customer
  opt-in and Meta billing.
- Notification content must minimize personal and financial data. WhatsApp
  message text is generated only during the explicit hand-off and is never
  logged or persisted.
