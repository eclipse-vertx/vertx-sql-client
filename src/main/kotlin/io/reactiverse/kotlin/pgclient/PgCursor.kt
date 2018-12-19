package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgCursor
import io.reactiverse.pgclient.PgRowSet
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Read rows from the cursor, the result is provided asynchronously to the <code>handler</code>.
 *
 * @param count the amount of rows to read
 * @param handler the handler for the result
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgCursor original] using Vert.x codegen.
 */
suspend fun PgCursor.readAwait(count : Int) : PgRowSet {
  return awaitResult{
    this.read(count, it)
  }
}

/**
 * Like [io.reactiverse.pgclient.PgCursor] but with a <code>completionHandler</code> called when the cursor has been released.
 *
 * @param completionHandler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgCursor original] using Vert.x codegen.
 */
suspend fun PgCursor.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}

