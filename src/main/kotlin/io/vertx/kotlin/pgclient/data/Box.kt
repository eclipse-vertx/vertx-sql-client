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
package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.Box
import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Box] objects.
 *
 * Rectangular box data type in Postgres represented by pairs of [io.vertx.pgclient.data.Point]s that are opposite corners of the box.
 *
 * @param lowerLeftCorner 
 * @param upperRightCorner 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Box original] using Vert.x codegen.
 */
fun boxOf(
  lowerLeftCorner: io.vertx.pgclient.data.Point? = null,
  upperRightCorner: io.vertx.pgclient.data.Point? = null): Box = io.vertx.pgclient.data.Box().apply {

  if (lowerLeftCorner != null) {
    this.setLowerLeftCorner(lowerLeftCorner)
  }
  if (upperRightCorner != null) {
    this.setUpperRightCorner(upperRightCorner)
  }
}

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Box] objects.
 *
 * Rectangular box data type in Postgres represented by pairs of [io.vertx.pgclient.data.Point]s that are opposite corners of the box.
 *
 * @param lowerLeftCorner 
 * @param upperRightCorner 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Box original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("boxOf(lowerLeftCorner, upperRightCorner)")
)
fun Box(
  lowerLeftCorner: io.vertx.pgclient.data.Point? = null,
  upperRightCorner: io.vertx.pgclient.data.Point? = null): Box = io.vertx.pgclient.data.Box().apply {

  if (lowerLeftCorner != null) {
    this.setLowerLeftCorner(lowerLeftCorner)
  }
  if (upperRightCorner != null) {
    this.setUpperRightCorner(upperRightCorner)
  }
}

