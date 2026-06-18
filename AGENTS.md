# Agent Guidelines - vertx-sql-client

Instructions for AI coding agents working in this repository.
Checkout relevant `.agents/skills/` to accomplish specific tasks.

## Build & Verify Commands

```bash
mvn test-compile         # compile code and tests
mvn test                 # run all tests (requires Docker for database interactions)
mvn spotless:check       # verify formatting
mvn spotless:apply       # auto-fix formatting
```

## Project Structure

This is a multi-module Maven project with the following key modules:

- **vertx-sql-client**: Core SQL client API and base implementations
- **vertx-sql-client-codec**: Shared codec utilities for data type encoding/decoding
- **vertx-sql-client-templates**: SQL template support for type-safe queries
- **vertx-pg-client**: PostgreSQL-specific client implementation
- **vertx-mysql-client**: MySQL/MariaDB-specific client implementation
- **vertx-mssql-client**: Microsoft SQL Server client implementation
- **vertx-db2-client**: IBM DB2 client implementation
- **vertx-oracle-client**: Oracle Database client implementation

### Module Layout Pattern

Each database-specific client follows this structure:
- `src/main/java/`: Public API interfaces and implementation classes
  - Top package (e.g., `io.vertx.pgclient`): Public API interfaces
  - `impl/` subpackage: Implementation classes (not exported)
- `src/main/asciidoc/`: Module-specific documentation
- `src/test/java/`: Unit and integration tests
- `module-info.java`: Defines module exports and dependencies

## General Coding Rules

These rules apply when **writing or modifying code**. Code review is the checkpoint where compliance is verified.

### Logging

In production code, use the Vert.x internal logger, never SLF4J, Log4j, or `java.util.logging` directly.

```java
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
```

Vert.x internal logging API doesn't support parameters placeholders.
Check the active level before debug or trace logging.

```java
if (logger.isDebugEnabled()) {
  logger.debug("Prepared parameters: " + paramDesc);
}
```

### Async Patterns

Use Vert.x `Future<T>` and `Promise<T>` throughout. Do not use raw callbacks or `CompletableFuture` in production code.

### API Design

- Public contracts are interfaces in the top package (`io.vertx.sqlclient`, `io.vertx.pgclient`, …)
- Implementations go in `impl/` subpackages
- Annotate public API interfaces and methods with `@VertxGen` for code-generation support
- Expose construction via static factory methods, not constructors

### Module Boundaries

`module-info.java` governs exports.
Internal packages are exported only to their corresponding test modules, do not widen exports without discussion.
Test module descriptors (`src/test/java/module-info.java`) can be modified freely, e.g. to add a `requires` for a new dependency used in tests.

### Copyright Header

New Java files must include the dual-license header matching existing files:

```java
/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
```

The range is always `2011-[current year]`.

## Testing Guidelines

For comprehensive testing patterns and examples, see `.agents/skills/writing-tests/SKILL.md`.

### Test Framework

- Use **JUnit 4** (version 4.13.1) for all tests
- Async tests use **VertxUnitRunner** (JUnit 4 runner) and **TestContext**
- Integration tests require **Docker** for database containers

### Test Patterns

```java
@RunWith(VertxUnitRunner.class)
public class MyTest {

  @Test
  public void testAsyncOperation(TestContext ctx) {
    Async async = ctx.async();
    client.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        async.complete();
      }));
  }
}
```

### Test Location

- **Unit tests**: Same module as code under test, in `src/test/java/`
- **Integration tests**: May be in separate test modules or `src/test/java/`
- **Database-specific tests**: In respective client module (e.g., `vertx-pg-client/src/test/`)

### Running Tests

```bash
mvn test                           # Run all tests (requires Docker)
mvn test -Dtest=MyTest             # Run specific test class
mvn test -Dtest=MyTest#testMethod  # Run specific test method

# When your change spans modules (e.g. modifying vertx-sql-client-codec and
# testing in vertx-pg-client), use -am to rebuild dependencies:
mvn test -pl vertx-pg-client -am -Dtest=MyTest
```

### Test Requirements

- All new features must include tests
- Database tests must clean up resources (connections, containers)

## Development Workflow

### Incremental Development

When making changes:
1. Compile frequently: `mvn compile -pl <module>`
2. Run affected tests: `mvn test -pl <module>`
3. Verify formatting: `mvn spotless:check`
4. Run full build before PR: `mvn clean install`

### Build Optimization

```bash
# Skip tests during development
mvn compile -DskipTests

# Build specific module and dependencies
mvn install -pl vertx-pg-client -am
```

## Specialized Skills

When performing specific tasks, read the relevant skill file for detailed guidance:

- **Writing tests** - Read `.agents/skills/writing-tests/SKILL.md` when creating or modifying tests

## Contribution Process

- All commits must be signed off: `git commit -s` (DCO)
- Commit messages should end with: `Assisted-by: [Provider] [Model-Family] ([Version/ID])` (replace placeholders)
- Contributors must have signed the [Eclipse Contributor Agreement (ECA)](https://www.eclipse.org/legal/ECA.php)

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full contribution workflow.

## Code Review Guidelines

### Verify

- General coding rules above are followed
- Test coverage is present; async tests use `VertxUnitRunner` and `TestContext`
- No breaking changes to public interfaces without prior discussion

### Do Not Comment On

- Patterns already used consistently throughout the codebase
