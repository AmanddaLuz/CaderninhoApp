# Caderninho agent instructions

- Read `AGENTS.md` and the related spec or ADR before changing code.
- Use Kotlin, Jetpack Compose, Material 3 and unidirectional state flow.
- Keep `minSdk 24`, MVVM with Hilt-injected ViewModels.
- Keep Room as the local source of truth; Firebase sync is additive (ADR 0002).
- Keep Composable screens passive: render state and dispatch events only.
- Prefer reusable, generic Composables (`ui/components`) when a real second
  use exists.
- Never log or persist WhatsApp message content beyond sending it.
- Follow `.github/pull_request_template.md`.
