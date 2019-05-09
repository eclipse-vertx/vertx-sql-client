package io.vertx.kotlin.pgclient

import io.vertx.pgclient.PgNotification

/**
 * A function providing a DSL for building [io.vertx.pgclient.PgNotification] objects.
 *
 * A notification emited by Postgres.
 *
 * @param channel  Set the channel value.
 * @param payload  Set the payload value.
 * @param processId  Set the process id.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.PgNotification original] using Vert.x codegen.
 */
fun PgNotification(
  channel: String? = null,
  payload: String? = null,
  processId: Int? = null): PgNotification = io.vertx.pgclient.PgNotification().apply {

  if (channel != null) {
    this.setChannel(channel)
  }
  if (payload != null) {
    this.setPayload(payload)
  }
  if (processId != null) {
    this.setProcessId(processId)
  }
}

