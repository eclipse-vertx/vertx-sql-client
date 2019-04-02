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

import io.reactiverse.pgclient.data.Path
import io.reactiverse.pgclient.data.Point

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Path] objects.
 *
 * Path data type in Postgres represented by lists of connected points.
 * Paths can be open, where the first and last points in the list are considered not connected,
 * or closed, where the first and last points are considered connected.
 *
 * @param open 
 * @param points 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Path original] using Vert.x codegen.
 */
fun pathOf(
  open: Boolean? = null,
  points: Iterable<io.reactiverse.pgclient.data.Point>? = null): Path = io.reactiverse.pgclient.data.Path().apply {

  if (open != null) {
    this.setOpen(open)
  }
  if (points != null) {
    this.setPoints(points.toList())
  }
}

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Path] objects.
 *
 * Path data type in Postgres represented by lists of connected points.
 * Paths can be open, where the first and last points in the list are considered not connected,
 * or closed, where the first and last points are considered connected.
 *
 * @param open 
 * @param points 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Path original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("pathOf(open, points)")
)
fun Path(
  open: Boolean? = null,
  points: Iterable<io.reactiverse.pgclient.data.Point>? = null): Path = io.reactiverse.pgclient.data.Path().apply {

  if (open != null) {
    this.setOpen(open)
  }
  if (points != null) {
    this.setPoints(points.toList())
  }
}

