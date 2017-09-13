/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

