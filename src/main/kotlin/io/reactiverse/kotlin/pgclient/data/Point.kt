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
package io.reactiverse.kotlin.pgclient.data

import io.reactiverse.pgclient.data.Point

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Point] objects.
 *
 * A Postgresql point.
 *
 * @param x 
 * @param y 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Point original] using Vert.x codegen.
 */
fun pointOf(
  x: Double? = null,
  y: Double? = null): Point = io.reactiverse.pgclient.data.Point().apply {

  if (x != null) {
    this.setX(x)
  }
  if (y != null) {
    this.setY(y)
  }
}

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Point] objects.
 *
 * A Postgresql point.
 *
 * @param x 
 * @param y 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Point original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("pointOf(x, y)")
)
fun Point(
  x: Double? = null,
  y: Double? = null): Point = io.reactiverse.pgclient.data.Point().apply {

  if (x != null) {
    this.setX(x)
  }
  if (y != null) {
    this.setY(y)
  }
}

