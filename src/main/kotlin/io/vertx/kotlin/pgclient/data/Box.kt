package io.vertx.kotlin.pgclient.data

import io.vertx.pgclient.data.Box
import io.vertx.pgclient.data.Point

/**
 * A function providing a DSL for building [io.vertx.pgclient.data.Box] objects.
 *
 * Rectangular box data type in Postgres represented by pairs of [io.vertx.pgclient.data.Point]s that are opposite corners of the box.
 *
 * @param lowerLeftCorner 
 * @param upperRightCorner 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.pgclient.data.Box original] using Vert.x codegen.
 */
fun Box(
  lowerLeftCorner: io.vertx.pgclient.data.Point? = null,
  upperRightCorner: io.vertx.pgclient.data.Point? = null): Box = io.vertx.pgclient.data.Box().apply {

  if (lowerLeftCorner != null) {
    this.setLowerLeftCorner(lowerLeftCorner)
  }
  if (upperRightCorner != null) {
    this.setUpperRightCorner(upperRightCorner)
  }
}

