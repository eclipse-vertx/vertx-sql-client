/*
 * Copyright 2019 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgNotification

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.PgNotification] objects.
 *
 * A notification emited by Postgres.
 *
 * @param channel  Set the channel value.
 * @param payload  Set the payload value.
 * @param processId  Set the process id.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgNotification original] using Vert.x codegen.
 */
fun pgNotificationOf(
  channel: String? = null,
  payload: String? = null,
  processId: Int? = null): PgNotification = io.reactiverse.pgclient.PgNotification().apply {

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

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.PgNotification] objects.
 *
 * A notification emited by Postgres.
 *
 * @param channel  Set the channel value.
 * @param payload  Set the payload value.
 * @param processId  Set the process id.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgNotification original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("pgNotificationOf(channel, payload, processId)")
)
fun PgNotification(
  channel: String? = null,
  payload: String? = null,
  processId: Int? = null): PgNotification = io.reactiverse.pgclient.PgNotification().apply {

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

