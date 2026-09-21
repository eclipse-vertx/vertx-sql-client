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
 * Options for {@link PgConnection#copyIn(String, PgCopyInOptions)}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class PgCopyInOptions {

  /**
   * The default target payload size, in bytes = {@code 256 KiB}.
   */
  public static final int DEFAULT_CHUNK_SIZE = 256 * 1024;

  private int chunkSize = DEFAULT_CHUNK_SIZE;

  public PgCopyInOptions() {
  }

  public PgCopyInOptions(PgCopyInOptions other) {
    this.chunkSize = other.chunkSize;
  }

  public PgCopyInOptions(JsonObject json) {
    PgCopyInOptionsConverter.fromJson(json, this);
  }

  /**
   * @return the target payload size, in bytes
   */
  public int getChunkSize() {
    return chunkSize;
  }

  /**
   * Set the target payload size.
   * <p/>
   * Writes are combined until they reach this size before being sent, so that many small writes do
   * not become many small messages. A write larger than this size is sent on its own.
   *
   * @param chunkSize the target payload size, in bytes
   * @return a reference to this, so the API can be used fluently
   */
  public PgCopyInOptions setChunkSize(int chunkSize) {
    if (chunkSize <= 0) {
      throw new IllegalArgumentException("chunkSize must be > 0");
    }
    this.chunkSize = chunkSize;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PgCopyInOptionsConverter.toJson(this, json);
    return json;
  }
}
