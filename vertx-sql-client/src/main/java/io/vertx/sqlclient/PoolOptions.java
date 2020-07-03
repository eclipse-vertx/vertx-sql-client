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
import java.util.Objects;

/**
 * The options for configuring a connection pool.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PoolOptions {

  /**
   * The default maximum number of connections a client will pool = 4
   */
  public static final int DEFAULT_MAX_SIZE = 4;

  /**
   * Default max wait queue size = -1 (unbounded)
   */
  public static final int DEFAULT_MAX_WAIT_QUEUE_SIZE = -1;

  /**
   * Default time before closing an idle connection that waits in the pool = 0 (never close).
   */
  public static final int DEFAULT_CONNECTION_RELEASE_DELAY = 0;

  private int maxSize = DEFAULT_MAX_SIZE;
  private int maxWaitQueueSize = DEFAULT_MAX_WAIT_QUEUE_SIZE;
  private int connectionReleaseDelay = DEFAULT_CONNECTION_RELEASE_DELAY;

  public PoolOptions() {
  }

  public PoolOptions(JsonObject json) {
    PoolOptionsConverter.fromJson(json, this);
  }

  public PoolOptions(PoolOptions other) {
    maxSize = other.maxSize;
    maxWaitQueueSize = other.maxWaitQueueSize;
    connectionReleaseDelay = other.connectionReleaseDelay;
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
   * @return time in milliseconds before closing an idle connection that waits in the pool.
   *            If 0 the connection will never be closed.
   */
  public int getConnectionReleaseDelay() {
    return connectionReleaseDelay;
  }

  /**
   * Set the idle time in milliseconds before closing a connection waiting in the pool.
   * If 0 the connection will never be closed.
   *
   * @param connectionReleaseDelay  idle time in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setConnectionReleaseDelay(int connectionReleaseDelay) {
    this.connectionReleaseDelay = connectionReleaseDelay;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PoolOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PoolOptions)) {
      return false;
    }
    PoolOptions that = (PoolOptions) o;
    return maxSize == that.maxSize
        && maxWaitQueueSize == that.maxWaitQueueSize
        && connectionReleaseDelay == that.connectionReleaseDelay;
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxSize, maxWaitQueueSize, connectionReleaseDelay);
  }
}
