# ADR 0004: Explicit backup rules and no cleartext traffic

**Status:** Accepted

## Decision

- Keep `android:allowBackup="true"` so a shopkeeper doesn't lose their client
  list on device change, but scope it explicitly via
  `android:dataExtractionRules` (API 31+) and `android:fullBackupContent`
  (API < 31), both including only the local Room database.
- Set `android:usesCleartextTraffic="false"` on the `<application>` tag.

## Rationale

Client/sale data (names, phone numbers, amounts owed) is personal and
financial. Relying on the platform's implicit backup/cleartext defaults
(flagged by SonarCloud/Android Lint) leaves those decisions undocumented and
broader than necessary. The app never performs raw HTTP calls today (WhatsApp
charging uses an `Intent` deep link per ADR 0003, and the `INTERNET`
permission exists only for a future Firebase sync per ADR 0002), so
cleartext traffic can be disabled without breaking any feature.

## Consequences

- If a future feature needs plain HTTP (unlikely; Firebase and WhatsApp both
  use HTTPS), this ADR must be revisited alongside a network security
  config.
- Backup rule files must be updated if new local storage (e.g. shared
  preferences with tokens) is added, to keep the "only the Room database is
  backed up" invariant explicit and true.
