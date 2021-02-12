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

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.TimeUnit;

/**
 * The options for configuring a connection pool.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PoolOptions {

  /**
   * Default connection timeout in the pool = 0 (no timeout)
   */
  public static final int DEFAULT_IDLE_TIMEOUT = 0;

  /**
   * Default connection idle time unit in the pool = seconds
   */
  public static final TimeUnit DEFAULT_IDLE_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

  /**
   * The default maximum number of connections a client will pool = 4
   */
  public static final int DEFAULT_MAX_SIZE = 4;

  /**
   * Default max wait queue size = -1 (unbounded)
   */
  public static final int DEFAULT_MAX_WAIT_QUEUE_SIZE = -1;

  private int maxSize = DEFAULT_MAX_SIZE;
  private int maxWaitQueueSize = DEFAULT_MAX_WAIT_QUEUE_SIZE;
  private int idleTimeout = DEFAULT_IDLE_TIMEOUT;
  private TimeUnit idleTimeUnit = DEFAULT_IDLE_TIMEOUT_TIME_UNIT;

  public PoolOptions() {
  }

  public PoolOptions(JsonObject json) {
    PoolOptionsConverter.fromJson(json, this);
  }

  public PoolOptions(PoolOptions other) {
    maxSize = other.maxSize;
    maxWaitQueueSize = other.maxWaitQueueSize;
    idleTimeout = other.idleTimeout;
    idleTimeUnit = other.idleTimeUnit;
  }

  /**
   * @return  the maximum pool size
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Set the maximum pool size
   *
   * @param maxSize  the maximum pool size
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setMaxSize(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size cannot be negative");
    }
    this.maxSize = maxSize;
    return this;
  }

  /**
   * @return the maximum wait queue size
   */
  public int getMaxWaitQueueSize() {
    return maxWaitQueueSize;
  }

  /**
   * Set the maximum connection request allowed in the wait queue, any requests beyond the max size will result in
   * an failure.  If the value is set to a negative number then the queue will be unbounded.
   *
   * @param maxWaitQueueSize the maximum number of waiting requests
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setMaxWaitQueueSize(int maxWaitQueueSize) {
    this.maxWaitQueueSize = maxWaitQueueSize;
    return this;
  }

  /**
   * @return the pool connection idle time unit
   */
  public TimeUnit getIdleTimeUnit() {
    return idleTimeUnit;
  }

  /**
   * Establish a idle time unit for connections in the pool
   * @param idleTimeUnit
   */
  public PoolOptions setIdleTimeUnit(TimeUnit idleTimeUnit) {
    this.idleTimeUnit = idleTimeUnit;
    return this;
  }

  /**
   * @return the pool connection idle timeout
   */
  public int getIdleTimeout() {
    return idleTimeout;
  }

  /**
   * Establish a idle timeout for connections in the pool
   * @param idleTimeout
   */
  public PoolOptions setIdleTimeout(int idleTimeout) {
    this.idleTimeout = idleTimeout;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PoolOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PoolOptions)) return false;
    if (!super.equals(o)) return false;

    PoolOptions that = (PoolOptions) o;

    if (maxSize != that.maxSize) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + maxSize;
    return result;
  }
}
