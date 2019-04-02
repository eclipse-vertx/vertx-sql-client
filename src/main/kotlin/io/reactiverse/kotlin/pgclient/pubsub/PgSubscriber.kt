package io.reactiverse.kotlin.pgclient.pubsub

import io.reactiverse.pgclient.pubsub.PgSubscriber
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Connect the subscriber to Postgres.
 *
 * @param handler the handler notified of the connection success or failure
 * @returna reference to this, so the API can be used fluently *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.pubsub.PgSubscriber original] using Vert.x codegen.
 */
suspend fun PgSubscriber.connectAwait() : Unit {
  return awaitResult{
    this.connect({ ar -> it.handle(ar.mapEmpty()) })}
}

