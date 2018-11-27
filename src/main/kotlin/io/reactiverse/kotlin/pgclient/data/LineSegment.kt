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

