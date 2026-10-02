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
broader than necessary. The app never performs raw HTTP calls today. WhatsApp charging uses an
external `Intent` deep link per ADR 0003, so neither the `INTERNET` permission
nor cleartext traffic is needed. A future Firebase phase must add network
permission and revisit the privacy disclosures alongside ADR 0002.

## Consequences

- If a future feature needs plain HTTP (unlikely; Firebase and WhatsApp both
  use HTTPS), this ADR must be revisited alongside a network security
  config.
- Backup rule files must be updated if new local storage (e.g. shared
  preferences with tokens) is added, to keep the "only the Room database is
  backed up" invariant explicit and true.
