package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.PgNotification

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

