package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Point] objects.
 *
 * A Postgresql point.
 *
 * @param x 
 * @param y 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Point original] using Vert.x codegen.
 */
fun Point(
  x: Double? = null,
  y: Double? = null): Point = io.vertx.pgclient.data.Point().apply {

  if (x != null) {
    this.setX(x)
  }
  if (y != null) {
    this.setY(y)
  }
}

