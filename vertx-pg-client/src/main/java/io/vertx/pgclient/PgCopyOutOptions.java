/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 * Options for COPY TO STDOUT streams.
 */
@DataObject
@JsonGen(publicConverter = false)
public class PgCopyOutOptions {

  /**
   * The default aggregation threshold, in bytes = {@code 1}, each {@code CopyData} message is
   * emitted as its own buffer.
   */
  public static final int DEFAULT_AGGREGATION_THRESHOLD = 1;

  private int aggregationThreshold = DEFAULT_AGGREGATION_THRESHOLD;

  public PgCopyOutOptions() {
  }

  public PgCopyOutOptions(PgCopyOutOptions other) {
    this.aggregationThreshold = other.aggregationThreshold;
  }

  public PgCopyOutOptions(JsonObject json) {
    PgCopyOutOptionsConverter.fromJson(json, this);
  }

  /**
   * @return the number of accumulated COPY data bytes after which a buffer is emitted
   */
  public int getAggregationThreshold() {
    return aggregationThreshold;
  }

  /**
   * Set the number of accumulated COPY data bytes after which a buffer is emitted.
   * <p>
   * A value of {@code 1} disables additional aggregation and emits each PostgreSQL
   * {@code CopyData} message as a buffer. COPY data buffers are not row-aligned.
   *
   * @param aggregationThreshold the aggregation threshold, in bytes
   * @return this options instance
   */
  public PgCopyOutOptions setAggregationThreshold(int aggregationThreshold) {
    if (aggregationThreshold <= 0) {
      throw new IllegalArgumentException("aggregationThreshold must be > 0");
    }
    this.aggregationThreshold = aggregationThreshold;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PgCopyOutOptionsConverter.toJson(this, json);
    return json;
  }
}
