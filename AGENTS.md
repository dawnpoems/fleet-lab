# FleetLab Agent Guide

## Project purpose

FleetLab is both a working multi-robot fleet simulation platform and a learning project. Optimize for three outcomes together:

1. Correct, executable software.
2. A simple design that can grow with real simulation requirements.
3. A development process the owner can understand and learn from.

Learning is not a reason to leave toy code, skip verification, or knowingly choose a weak design. Make the reasoning visible through small changes, focused explanations, and tests.

## Collaboration and learning

- Communicate with the owner in Korean by default. Keep code identifiers and established technical terms in English.
- For a non-trivial task, briefly align on the goal, relevant context, constraints, and definition of done before implementation.
- Ask the owner when a choice materially affects the domain model, public API, persistence model, architecture, dependencies, or development workflow. Present the viable options, their tradeoffs, and a recommendation.
- Do not block routine work on minor naming, formatting, or implementation details. Make a reasonable choice and report it.
- When introducing an important concept, explain why it is needed here, how it works in the current code path, and what tradeoff it creates. Avoid unrelated textbook exposition.
- After a meaningful change, include a short learning note: the key concept, the best files or tests to inspect, and a useful next question or exercise.
- Prefer evidence from code, tests, and current official documentation over confident guesses. Verify unstable framework or library behavior before relying on it.
- Challenge assumptions respectfully when they conflict with the product goal or create unnecessary complexity.

## Product and architecture guardrails

- Keep `Scenario` (experiment configuration) separate from `SimulationRun` (one execution of that configuration).
- Keep mutable runtime state isolated per run. Do not introduce global mutable simulation state.
- The simulation engine must depend on policy abstractions, not concrete dispatch, path-finding, or traffic-control algorithms.
- Robot counts, algorithms, workloads, speeds, and other experiment conditions must evolve as configuration, not hardcoded behavior.
- Make randomness injectable or seed-based so equivalent scenarios can be reproduced.
- Design simulation results so they can become explicit domain metrics and be compared across runs.
- Start algorithm and simulation behavior as pure Kotlin that can be tested without Spring or a database.
- Use package-by-feature. Do not create repository-wide `controller`, `service`, `repository`, or `entity` buckets.
- Add abstractions when they protect a known variation point or make testing materially easier. Do not build speculative frameworks.
- Remain a monolithic Spring Boot application until a demonstrated need justifies another deployment boundary.

Do not introduce Kubernetes, Kafka, Redis, microservices, CQRS, event sourcing, a dynamic plugin system, authentication infrastructure, or a Gradle multi-module build unless the owner explicitly changes the scope.

## Lightweight hexagonal architecture

- Keep the simulation and domain core independent of Spring, JPA, Jackson, HTTP, databases, and other external technologies.
- Direct dependencies inward: adapters may depend on application and domain code; the core must not depend on adapters.
- Put orchestration in small application use cases when coordination is needed. Keep domain rules in the domain objects and policies that own them.
- Create a Port only where the application or domain crosses a real external boundary, such as persistence, an external event source, or an external clock. Name it after the capability the core needs.
- Create an Adapter only when that boundary has an actual implementation, such as a REST API or JPA persistence. Keep framework annotations and transport or persistence models in the adapter.
- Do not create one interface per class, empty layers, pass-through services, or ceremonial DTO/entity/mapper triples merely to resemble hexagonal architecture.
- Domain strategy interfaces such as `PathPlanner`, `Dispatcher`, and `TrafficController` are policy variation points inside the core; they are not infrastructure Ports by default.
- Keep package-by-feature as the primary organization. Add `domain`, `application`, `port`, or `adapter` subpackages inside a feature only when those boundaries actually exist.
- Wire implementations at the outer Spring boundary. Prefer plain Kotlin construction in unit tests.

## Repository map

- `backend/`: Kotlin, Spring Boot, Gradle, Liquibase, and backend tests.
- `frontend/`: React, TypeScript, and Vite.
- `docs/`: architecture, domain notes, and durable decisions that are actually made.
- `compose.yaml`: local PostgreSQL/PostGIS only.
- `README.md`: prerequisites and the primary local-development entry point.

Do not pre-create empty feature packages or speculative documentation trees. Add structure with the first real use.

## Current technical conventions

### Backend

- Use JDK 21, Kotlin, Spring Boot 4.1, and the Gradle wrapper committed to the repository.
- Use the package namespace `kr.dawnpoem.fleetlab`.
- Prefer constructor injection and immutable values.
- Keep domain and simulation core code free of framework and infrastructure dependencies.
- Let Spring Boot manage dependency versions unless a specific incompatibility requires an override.
- Discuss new production dependencies with the owner before adding them; include the need, alternatives, and maintenance cost.

### Database

- Manage every schema change through Liquibase and include it from `db.changelog-master.yaml`.
- Keep `spring.jpa.hibernate.ddl-auto=validate`; do not use Hibernate schema generation as a migration mechanism.
- Use sequential, descriptive changelog names such as `001-create-map.yaml`.
- Treat the database as PostGIS-ready, not PostGIS-driven. Begin maps as graph data with simulation coordinates.
- Use Testcontainers PostgreSQL/PostGIS for database integration tests. Do not use H2 or depend on a developer's localhost database in tests.

### Frontend

- Keep TypeScript strict and use function components.
- Prefer the platform, React state, and small local abstractions before adding state-management, data-fetching, UI, or rendering libraries.
- Begin map visualization with SVG or Canvas. Do not add Three.js until a concrete requirement justifies it.
- Keep feature code near the feature that owns it; create shared abstractions only after genuine reuse appears.
- After frontend work, start the application and verify the changed behavior in an actual browser whenever the environment permits.
- Give the owner a local URL, exact test steps, and the expected visible result so they can exercise the screen themselves. If the dev server cannot remain running, provide the exact startup command instead.

## Development workflow

1. Read the nearest `AGENTS.md`, the relevant code, and existing tests before editing.
2. Preserve unrelated user changes and avoid broad mechanical rewrites.
3. For complex or ambiguous work, propose a short plan and resolve material questions with the owner.
4. Define one commit-sized unit of work. If the request contains multiple units, show the sequence and work through one unit at a time.
5. Follow the backend TDD commit workflow below whenever backend production code will change.
6. Run the relevant checks and inspect the final diff for regressions, accidental files, and unnecessary complexity.
7. Report changed files, key decisions, commands and results, remaining risks, the next recommended learning step, and a proposed commit message.

Do not push, delete data, stop unrelated services, or make destructive Git changes unless the owner explicitly asks.

When the owner explicitly says `커밋해` or otherwise asks for a commit, inspect the status and diff, stage only the files belonging to the current commit-sized unit, and create the commit using the agreed Conventional Commit message. Do not push unless separately requested. When the owner does not ask for a commit, leave the changes uncommitted and provide only the proposed message.

## Commit and TDD discipline

- Each work unit must map cleanly to one commit. Do not mix unrelated features, fixes, refactors, dependency changes, or documentation cleanup.
- When a request requires several commits, stop at each commit boundary and provide its commit message before starting the next unit.
- Do not create the commit yourself unless the owner explicitly asks.

For every backend production-code change, use separate Red and Green commits:

1. **Red commit:** add or change the test first without changing production code. Run the focused test and confirm that it fails for the intended behavioral reason, not because of a syntax, setup, or environment error. Stop at this boundary and propose a `test` commit message.
2. **Green commit:** after the Red commit boundary is accepted, write the minimum production code needed to pass. Run the focused test and the relevant backend suite. Stop only when they pass, then propose a `feat` or `fix` commit message.
3. **Refactor commit:** perform optional structural cleanup separately while keeping all tests green. Do not hide refactoring inside the Green commit unless it is strictly required for the minimal implementation.

Do not write production code in the Red unit. Do not weaken, delete, or rewrite a valid failing test merely to make the Green unit pass. For documentation, build configuration, or other changes where a behavioral test is genuinely not applicable, explain the verification approach before editing instead of fabricating a test.

At the end of every work unit, provide a Korean Conventional Commit message in this exact shape:

```text
type: 간결한 한국어 제목

- 한국어 설명
- 한국어 설명
- 한국어 설명
```

- Use an appropriate Conventional Commit type such as `test`, `feat`, `fix`, `refactor`, `docs`, `chore`, `build`, or `ci`.
- Add a scope only when it materially clarifies the affected area. Do not add a redundant scope such as `agents` for a repository-level `AGENTS.md` change.
- Keep the title concise and specific.
- Write the body in Korean with 3 to 6 description lines.
- Describe only the completed commit-sized unit and include verification when it is relevant.

## Commands and verification

Local database from the repository root:

```bash
docker compose up -d --wait db
docker compose ps
```

Backend:

```bash
cd backend
./gradlew test
./gradlew bootRun
```

When the backend is running, verify `http://localhost:8080/actuator/health` reports `UP`.

Frontend:

```bash
cd frontend
npm install
npm run lint
npm run build
npm run dev
```

Apply verification in proportion to the change:

- Pure domain or algorithm change: fast unit tests without Spring.
- Persistence or API change: focused integration tests with Spring and Testcontainers.
- Frontend change: lint and production build; start the dev server, inspect the actual screen in a browser, and provide the owner with URL, test steps, and expected results.
- Configuration or bootstrap change: validate the affected command and, when practical, perform a real startup/health check.

If a required check cannot run, state the exact reason and what remains unverified. Do not silently substitute a weaker check.

## Documentation

- Update `README.md` when setup commands, prerequisites, or the supported development workflow change.
- Record architecture decisions only when a meaningful decision and tradeoff exist.
- Record accepted domain and architecture decisions in sequentially numbered files under `docs/decisions/`, and update its decision list.
- Keep decision records concise. Include the context, decision, benefits, tradeoffs, alternatives, deferred scope, and revisit conditions; prefer tables for comparisons.
- Explain why a decision was made, the alternatives considered, and when it should be revisited.
- Keep documentation synchronized with executable behavior; delete or update stale claims.

## Code Review Rules

Flag changes that introduce any of the following:

- A simulation engine that directly constructs or selects a concrete algorithm.
- Runtime state shared across independent simulation runs.
- Random behavior with no controllable seed or injectable source.
- Experiment conditions or robot scale hardcoded into domain behavior.
- Database schema changes outside Liquibase.
- Integration tests that use H2 or a developer's local database.
- Heavy infrastructure, frameworks, or dependencies without a current requirement.
- Framework or adapter dependencies leaking into the domain or simulation core.
- Ports or adapters created without a real external boundary.
- Pass-through layers or one-to-one interfaces added only for architectural symmetry.
- Domain logic that can only be tested through a full Spring context without a clear reason.
- Missing tests for changed domain invariants or externally visible behavior.
- Backend production code added before its intended failing test commit.
- Generated artifacts, secrets, local environment files, or unrelated edits entering the diff.

When flagging an issue, explain the concrete risk and suggest the smallest safe correction.
