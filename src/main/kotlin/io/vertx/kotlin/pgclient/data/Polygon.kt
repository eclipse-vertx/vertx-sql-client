package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.Polygon
import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Polygon] objects.
 *
 * Polygon data type in Postgres represented by lists of points (the vertexes of the polygon).
 * Polygons are very similar to closed paths, but are stored differently and have their own set of support routines.
 *
 * @param points 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Polygon original] using Vert.x codegen.
 */
fun Polygon(
  points: Iterable<io.vertx.pgclient.data.Point>? = null): Polygon = io.vertx.pgclient.data.Polygon().apply {

  if (points != null) {
    this.setPoints(points.toList())
  }
}

