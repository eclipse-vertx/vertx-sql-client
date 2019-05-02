package io.reactiverse.kotlin.pgclient.data

import io.reactiverse.pgclient.data.Line

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Line] objects.
 *
 * Line data type in Postgres represented by the linear equation Ax + By + C = 0, where A and B are not both zero.
 *
 * @param a 
 * @param b 
 * @param c 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Line original] using Vert.x codegen.
 */
fun Line(
  a: Double? = null,
  b: Double? = null,
  c: Double? = null): Line = io.reactiverse.pgclient.data.Line().apply {

  if (a != null) {
    this.setA(a)
  }
  if (b != null) {
    this.setB(b)
  }
  if (c != null) {
    this.setC(c)
  }
}

