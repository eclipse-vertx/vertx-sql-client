/*
 * Copyright 2019 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
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
fun lineOf(
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
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("lineOf(a, b, c)")
)
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

