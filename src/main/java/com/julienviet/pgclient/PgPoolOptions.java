package com.julienviet.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * The options for configuring a connection pool.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PgPoolOptions {

  public static final int DEFAULT_MAX_POOL_SIZE = 4;
  public static final PoolingMode DEFAULT_MODE = PoolingMode.CONNECTION;

  private int maxSize = DEFAULT_MAX_POOL_SIZE;
  private PoolingMode mode = DEFAULT_MODE;

  public PgPoolOptions() {
  }

  public PgPoolOptions(JsonObject json) {
    PgPoolOptionsConverter.fromJson(json, this);
  }

  public PgPoolOptions(PgPoolOptions other) {
    maxSize = other.maxSize;
    mode = other.mode;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public PgPoolOptions setMaxSize(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size cannot be negative");
    }
    this.maxSize = maxSize;
    return this;
  }

  public PoolingMode getMode() {
    return mode;
  }

  public PgPoolOptions setMode(PoolingMode mode) {
    this.mode = mode;
    return this;
  }
}
