package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.PgNotification

/**
 * A function providing a DSL for building [com.julienviet.pgclient.PgNotification] objects.
 *
 * A notification emited by Postgres.
 *
 * @param channel  Set the channel value.
 * @param payload  Set the payload value.
 * @param processId  Set the process id.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [com.julienviet.pgclient.PgNotification original] using Vert.x codegen.
 */
fun PgNotification(
  channel: String? = null,
  payload: String? = null,
  processId: Int? = null): PgNotification = com.julienviet.pgclient.PgNotification().apply {

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

