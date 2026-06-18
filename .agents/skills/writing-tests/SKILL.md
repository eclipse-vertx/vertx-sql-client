---
name: writing-tests
description: >
  Testing patterns for Vert.x SQL Client: test frameworks, async testing,
  test locations, and how to run tests.
---

# Writing Tests

Vert.x SQL Client uses JUnit 4 with Vert.x-specific test utilities. Tests and documentation are mandatory for contributions.

## Test Framework

- **JUnit 4** (version 4.13.1) - Standard test framework
- **VertxUnitRunner** - JUnit 4 runner for async tests
- **TestContext** - Async test utility for handling asynchronous operations
- **Docker** - Required for integration tests (database containers)

## Test Annotations and Extensions

### Standard Test Patterns

#### Pattern 1: Using Async with Simple Callback

```java
@RunWith(VertxUnitRunner.class)
public class MyTest {

  @Test
  public void testWithAsync(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> async.complete());
  }
}
```

#### Pattern 2: Using asyncAssertSuccess Directly

When using `asyncAssertSuccess`, do not call `async.complete()` as it creates an async internally:

```java
@RunWith(VertxUnitRunner.class)
public class MyTest {

  @Test
  public void testAsyncAssertSuccess(TestContext ctx) {
    client.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
      }));
  }
}
```

#### Pattern 3: Using asyncAssertSuccess from runOnContext

When calling `asyncAssertSuccess` from `runOnContext`, you need an explicit async:

```java
@RunWith(VertxUnitRunner.class)
public class MyTest {

  @Test
  public void testAsyncAssertSuccessFromRunOnContext(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      client.query("SELECT 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          async.complete();
        }));
    });
  }
}
```

## Test Location

- **Unit tests**: Same module as code under test, in `src/test/java/`
- **Integration tests**: May be in separate test modules or `src/test/java/`
- **Database-specific tests**: In respective client module
  - PostgreSQL: `vertx-pg-client/src/test/`
  - MySQL: `vertx-mysql-client/src/test/`
  - MSSQL: `vertx-mssql-client/src/test/`
  - DB2: `vertx-db2-client/src/test/`
  - Oracle: `vertx-oracle-client/src/test/`

## Test Data Setup

### Using SQL Scripts

Place initialization scripts in `src/test/resources/`:

```sql
-- init.sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

INSERT INTO users (name) VALUES ('Alice'), ('Bob');
```

### Programmatic Setup

```java
@Before
public void setUp(TestContext ctx) {
  Async async = ctx.async();
  client.query("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(100))")
    .execute()
    .compose(v -> client.query("INSERT INTO users (name) VALUES ('Alice'), ('Bob')").execute())
    .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
}

@After
public void tearDown(TestContext ctx) {
  Async async = ctx.async();
  client.query("DROP TABLE IF EXISTS users")
    .execute()
    .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
}
```

Prefer temporary tables when testing databases that support it (automatically dropped when the connection is closed).

## Test Requirements

- **All new features must include tests**
- **Integration tests must clean up resources** (connections, containers)

## Assertions

### TestContext Assertions (JUnit 4 style)

```java
ctx.assertEquals(expected, actual);
ctx.assertTrue(condition);
ctx.assertFalse(condition);
ctx.assertNull(value);
ctx.assertNotNull(value);
```

## Common Pitfalls

1. **Forgetting to complete async tests** - Always call `async.complete()`
