# Copilot Instructions for dp-permittering-statistikk

<!-- This file captures repository-specific context.
     Nav-wide language and framework conventions are provided by installed Copilot instructions. -->

## Repository Overview

Rapids & Rivers application producing statistics about permittering (lay-offs) in dagpenger.

- Owner: team STSB (Saksbehandling dagpenger) in teamdagpenger
- Not an economy system (`okonomisystem: "false"` in `.nais/nais.yaml`)
- Domain scope (source events, storage, consumers) is not decided yet

## Tech Stack

- Kotlin/JVM 25, Gradle with version catalog `no.nav.dagpenger:dp-version-catalog`
- Rapids & Rivers: primary topic `teamdagpenger.permittering-statistikk.v1` (output read by the statistics team), extra topic `teamdagpenger.rapid.v1` (input)
- The output topic is managed in `navikt/dagpenger-iac`, not in this repo
- konfig for configuration
- Kotest assertions and `TestRapid` for tests
- Nais on GCP, Renovate via `navikt/dp-renovate`

## Key Patterns

- Setup follows `navikt/dp-saksbehandling`: `ApplicationBuilder` wires `RapidApplication`, defaults live in `Configuration`
- Rivers use `precondition` for `@event_name` filtering and `validate` for required fields
- Never log personal data (fnr, name); log behandling/event ids instead

## Minimal Editing

When fixing a bug or implementing a feature, change only what is necessary.
Do not rename variables, restructure working code, or refactor beyond the task at hand.
Keep diffs small and focused so they are easy to review.
