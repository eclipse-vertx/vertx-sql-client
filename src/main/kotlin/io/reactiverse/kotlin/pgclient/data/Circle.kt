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

