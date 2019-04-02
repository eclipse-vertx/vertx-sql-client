package io.reactiverse.kotlin.pgclient.data

import io.reactiverse.pgclient.data.Box
import io.reactiverse.pgclient.data.Point

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Box] objects.
 *
 * Rectangular box data type in Postgres represented by pairs of [io.reactiverse.pgclient.data.Point]s that are opposite corners of the box.
 *
 * @param lowerLeftCorner 
 * @param upperRightCorner 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Box original] using Vert.x codegen.
 */
fun Box(
  lowerLeftCorner: io.reactiverse.pgclient.data.Point? = null,
  upperRightCorner: io.reactiverse.pgclient.data.Point? = null): Box = io.reactiverse.pgclient.data.Box().apply {

  if (lowerLeftCorner != null) {
    this.setLowerLeftCorner(lowerLeftCorner)
  }
  if (upperRightCorner != null) {
    this.setUpperRightCorner(upperRightCorner)
  }
}

