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

import io.reactiverse.pgclient.data.LineSegment
import io.reactiverse.pgclient.data.Point

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.LineSegment] objects.
 *
 * Finite line segment data type in Postgres represented by pairs of [io.reactiverse.pgclient.data.Point]s that are the endpoints of the segment.
 *
 * @param p1 
 * @param p2 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.LineSegment original] using Vert.x codegen.
 */
fun lineSegmentOf(
  p1: io.reactiverse.pgclient.data.Point? = null,
  p2: io.reactiverse.pgclient.data.Point? = null): LineSegment = io.reactiverse.pgclient.data.LineSegment().apply {

  if (p1 != null) {
    this.setP1(p1)
  }
  if (p2 != null) {
    this.setP2(p2)
  }
}

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.LineSegment] objects.
 *
 * Finite line segment data type in Postgres represented by pairs of [io.reactiverse.pgclient.data.Point]s that are the endpoints of the segment.
 *
 * @param p1 
 * @param p2 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.LineSegment original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("lineSegmentOf(p1, p2)")
)
fun LineSegment(
  p1: io.reactiverse.pgclient.data.Point? = null,
  p2: io.reactiverse.pgclient.data.Point? = null): LineSegment = io.reactiverse.pgclient.data.LineSegment().apply {

  if (p1 != null) {
    this.setP1(p1)
  }
  if (p2 != null) {
    this.setP2(p2)
  }
}

