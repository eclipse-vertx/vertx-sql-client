package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgStream
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Close the stream and release the resources.
 *
 * @param completionHandler the completion handler for this operation
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgStream original] using Vert.x codegen.
 */
suspend fun <T> PgStream<T>.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}

