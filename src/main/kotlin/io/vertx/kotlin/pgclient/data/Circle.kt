package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.Circle
import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Circle] objects.
 *
 * Circle data type in Postgres represented by a center [io.vertx.pgclient.data.Point] and radius.
 *
 * @param centerPoint 
 * @param radius 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Circle original] using Vert.x codegen.
 */
fun Circle(
  centerPoint: io.vertx.pgclient.data.Point? = null,
  radius: Double? = null): Circle = io.vertx.pgclient.data.Circle().apply {

  if (centerPoint != null) {
    this.setCenterPoint(centerPoint)
  }
  if (radius != null) {
    this.setRadius(radius)
  }
}

