package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.Cursor
import io.vertx.sqlclient.RowSet

/**
 * Read rows from the cursor, the result is provided asynchronously to the <code>handler</code>.
 *
 * @param count the amount of rows to read
 * @param handler the handler for the result
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.Cursor original] using Vert.x codegen.
 */
suspend fun Cursor.readAwait(count : Int) : RowSet {
  return awaitResult{
    this.read(count, it)
  }
}

/**
 * Like [io.vertx.sqlclient.Cursor] but with a <code>completionHandler</code> called when the cursor has been released.
 *
 * @param completionHandler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.Cursor original] using Vert.x codegen.
 */
suspend fun Cursor.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}

