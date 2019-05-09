package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.LineSegment
import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.LineSegment] objects.
 *
 * Finite line segment data type in Postgres represented by pairs of [io.vertx.pgclient.data.Point]s that are the endpoints of the segment.
 *
 * @param p1 
 * @param p2 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.LineSegment original] using Vert.x codegen.
 */
fun LineSegment(
  p1: io.vertx.pgclient.data.Point? = null,
  p2: io.vertx.pgclient.data.Point? = null): LineSegment = io.vertx.pgclient.data.LineSegment().apply {

  if (p1 != null) {
    this.setP1(p1)
  }
  if (p2 != null) {
    this.setP2(p2)
  }
}

