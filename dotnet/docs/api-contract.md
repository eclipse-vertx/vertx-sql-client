# Apex SQL client API contract

## Ownership and lifetime

- `ISqlClient`, `ISqlConnection`, `ISqlPool`, `ISqlPreparedStatement`, `ISqlCursor`, and `ISqlTransaction` are async-disposable.
- A pooled connection lease must remain alive while its transaction, prepared statement, cursor, or stream is in use. The pool pins the physical connection until lease-derived resources complete.
- `SqlRowSet` and its rows own managed storage and remain valid after the originating connection is released.
- Streaming rows are consumed in order. Implementations may reuse internal transport buffers, but a yielded `SqlRow` remains a safe managed value.

## Concurrency

- Pools are safe for concurrent callers.
- A connection preserves command submission order. The configured driver pipelining limit controls the number of in-flight commands; it does not reorder results.
- Transactions, prepared statements, and cursors are bound to their originating connection and must not be used concurrently unless their API explicitly states otherwise.

## Cancellation

- Every one-shot asynchronous operation accepts a final `CancellationToken`.
- Cancellation before protocol submission prevents the command from being sent.
- Cancellation after PostgreSQL submission sends a PostgreSQL cancellation request and drains the response so the connection can be reused only after returning to idle state.
- Commit and rollback use cancellation only before submission. Once sent, they complete deterministically to avoid reporting cancellation after a transaction may already have committed.

## Query results

- `QueryAsync` buffers a `SqlRowSet`; `StreamAsync` is the backpressured alternative.
- `SqlParameters` stores ordered `SqlValue` instances. Common scalar `SqlValue` conversions avoid boxing at parameter construction.
- Column lookup is ordinal and case-sensitive, matching PostgreSQL field names.
- Mapping and collection helpers execute user delegates synchronously for each buffered or streamed row.

## Errors and diagnostics

- Database errors derive from `SqlClientException`; PostgreSQL errors expose SQLSTATE and structured server fields through `PgException`.
- Activities and metrics never include passwords or parameter values.
- A physical connection is never returned to a pool while PostgreSQL reports an active or failed transaction.
