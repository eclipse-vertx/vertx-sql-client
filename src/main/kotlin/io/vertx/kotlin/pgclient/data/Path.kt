package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.Path
import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Path] objects.
 *
 * Path data type in Postgres represented by lists of connected points.
 * Paths can be open, where the first and last points in the list are considered not connected,
 * or closed, where the first and last points are considered connected.
 *
 * @param open 
 * @param points 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Path original] using Vert.x codegen.
 */
fun Path(
  open: Boolean? = null,
  points: Iterable<io.vertx.pgclient.data.Point>? = null): Path = io.vertx.pgclient.data.Path().apply {

  if (open != null) {
    this.setOpen(open)
  }
  if (points != null) {
    this.setPoints(points.toList())
  }
}

