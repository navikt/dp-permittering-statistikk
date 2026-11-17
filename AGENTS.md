# AGENTS.md — dp-permittering-statistikk

Rapids & Rivers application producing statistics about permittering in dagpenger. Owned by team STSB (teamdagpenger). Not an economy system.

## Build & Test Commands

```bash
./gradlew build   # compile, ktlint, tests and shadow jar
./gradlew test
```

## Project Structure

```text
src/main/kotlin/no/nav/dagpenger/permittering/statistikk/  # App, ApplicationBuilder, Configuration, rivers
src/test/kotlin/                                           # Tests (TestRapid)
.nais/                                                     # Nais manifest and vars per environment
.github/workflows/                                         # Build and deploy to dev-gcp/prod-gcp
```

## Code Style

### Minimal Editing

When fixing a bug or implementing a feature, change only what is necessary.
Do not rename variables, restructure working code, or refactor beyond the task at hand.
Keep diffs small and focused so they are easy to review.

## Git Workflow

Trunk-based on `main`: every push to `main` builds and deploys to dev-gcp and prod-gcp. Renovate branches only run tests.

## Boundaries

### ✅ Always

- Run tests after changes
- Follow existing code patterns in the project
- Preserve existing code structure — do not reorganize or refactor beyond the task
- Validate all external input

### ⚠️ Ask First

- Changing authentication mechanisms
- Adding new dependencies
- Modifying database schema

### 🚫 Never

- Commit secrets or credentials
- Skip input validation on external boundaries
