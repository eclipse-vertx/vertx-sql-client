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

