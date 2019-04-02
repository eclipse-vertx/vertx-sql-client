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

import io.reactiverse.pgclient.data.Circle
import io.reactiverse.pgclient.data.Point

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Circle] objects.
 *
 * Circle data type in Postgres represented by a center [io.reactiverse.pgclient.data.Point] and radius.
 *
 * @param centerPoint 
 * @param radius 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Circle original] using Vert.x codegen.
 */
fun circleOf(
  centerPoint: io.reactiverse.pgclient.data.Point? = null,
  radius: Double? = null): Circle = io.reactiverse.pgclient.data.Circle().apply {

  if (centerPoint != null) {
    this.setCenterPoint(centerPoint)
  }
  if (radius != null) {
    this.setRadius(radius)
  }
}

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Circle] objects.
 *
 * Circle data type in Postgres represented by a center [io.reactiverse.pgclient.data.Point] and radius.
 *
 * @param centerPoint 
 * @param radius 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Circle original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("circleOf(centerPoint, radius)")
)
fun Circle(
  centerPoint: io.reactiverse.pgclient.data.Point? = null,
  radius: Double? = null): Circle = io.reactiverse.pgclient.data.Circle().apply {

  if (centerPoint != null) {
    this.setCenterPoint(centerPoint)
  }
  if (radius != null) {
    this.setRadius(radius)
  }
}

