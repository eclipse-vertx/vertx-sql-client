package com.julienviet.kotlin.pgclient

import com.julienviet.pgclient.PgPoolOptions
import com.julienviet.pgclient.PoolingMode

/**
 * A function providing a DSL for building [com.julienviet.pgclient.PgPoolOptions] objects.
 *
 * The options for configuring a connection pool.
 *
 * @param maxSize 
 * @param mode 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [com.julienviet.pgclient.PgPoolOptions original] using Vert.x codegen.
 */
fun PgPoolOptions(
  maxSize: Int? = null,
  mode: PoolingMode? = null): PgPoolOptions = com.julienviet.pgclient.PgPoolOptions().apply {

  if (maxSize != null) {
    this.setMaxSize(maxSize)
  }
  if (mode != null) {
    this.setMode(mode)
  }
}

