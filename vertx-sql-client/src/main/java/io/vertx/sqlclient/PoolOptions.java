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
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
   * Default connection timeout in the pool = 0 (no timeout)
   */
  public static final int DEFAULT_IDLE_TIMEOUT = 0;

  /**
   * Default maximum pooled connection lifetime = 0 (no maximum)
   */
  public static final int DEFAULT_MAXIMUM_LIFETIME = 0;

  /**
   * Default connection idle time unit in the pool = seconds
   */
  public static final TimeUnit DEFAULT_IDLE_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

  /**
   * Default maximum pooled connection lifetime unit = seconds
   */
  public static final TimeUnit DEFAULT_MAXIMUM_LIFETIME_TIME_UNIT = TimeUnit.SECONDS;

  /**
   * Default pool cleaner period = 1000 ms (1 second)
   */
  public static final int DEFAULT_POOL_CLEANER_PERIOD = 1000;

  /**
   * Default connection timeout in the pool = 30 seconds
   */
  public static final int DEFAULT_CONNECTION_TIMEOUT = 30;

  /**
   * Default connection idle time unit in the pool = seconds
   */
  public static final TimeUnit DEFAULT_CONNECTION_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

  /**
   * Default shared pool config = {@code false}
   */
  public static final boolean DEFAULT_SHARED_POOL = false;

  /**
   * Actual name of anonymous shared pool = {@code __vertx.DEFAULT}
   */
  public static final String DEFAULT_NAME = "__vertx.DEFAULT";

  /**
   * Default pool event loop size = 0 (reuse current event-loop)
   */
  public static final int DEFAULT_EVENT_LOOP_SIZE = 0;

  private int maxSize = DEFAULT_MAX_SIZE;
  private int maxWaitQueueSize = DEFAULT_MAX_WAIT_QUEUE_SIZE;
  private int idleTimeout = DEFAULT_IDLE_TIMEOUT;
  private TimeUnit idleTimeoutUnit = DEFAULT_IDLE_TIMEOUT_TIME_UNIT;
  private int maxLifetime = DEFAULT_MAXIMUM_LIFETIME;
  private TimeUnit maxLifetimeUnit = DEFAULT_MAXIMUM_LIFETIME_TIME_UNIT;
  private int poolCleanerPeriod = DEFAULT_POOL_CLEANER_PERIOD;
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
  private TimeUnit connectionTimeoutUnit = DEFAULT_CONNECTION_TIMEOUT_TIME_UNIT;
  private boolean shared = DEFAULT_SHARED_POOL;
  private String name = DEFAULT_NAME;
  private int eventLoopSize = DEFAULT_EVENT_LOOP_SIZE;

  public PoolOptions() {
  }

  public PoolOptions(JsonObject json) {
    PoolOptionsConverter.fromJson(json, this);
  }

  public PoolOptions(PoolOptions other) {
    maxSize = other.maxSize;
    maxWaitQueueSize = other.maxWaitQueueSize;
    idleTimeout = other.idleTimeout;
    idleTimeoutUnit = other.idleTimeoutUnit;
    shared= other.shared;
    name = other.name;
    eventLoopSize = other.eventLoopSize;
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
   * @return the pooled connection idle timeout unit
   */
  public TimeUnit getIdleTimeoutUnit() {
    return idleTimeoutUnit;
  }

  /**
   * Establish an idle timeout unit for pooled connections.
   *
   * @param idleTimeoutUnit pooled connection idle time unit
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    this.idleTimeoutUnit = idleTimeoutUnit;
    return this;
  }

  /**
   * @return pooled connection idle timeout
   */
  public int getIdleTimeout() {
    return idleTimeout;
  }

  /**
   * Establish an idle timeout for pooled connections, a value of zero disables the idle timeout.
   *
   * @param idleTimeout the pool connection idle timeout
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setIdleTimeout(int idleTimeout) {
    Arguments.require(idleTimeout >= 0, "idleTimeout must be >= 0");
    this.idleTimeout = idleTimeout;
    return this;
  }

  /**
   * @return the pooled connection max lifetime unit
   */
  public TimeUnit getMaxLifetimeUnit() {
    return maxLifetimeUnit;
  }

  /**
   * Establish a max lifetime unit for pooled connections.
   *
   * @param maxLifetimeUnit pooled connection max lifetime unit
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setMaxLifetimeUnit(TimeUnit maxLifetimeUnit) {
    this.maxLifetimeUnit = maxLifetimeUnit;
    return this;
  }

  /**
   * @return pooled connection max lifetime
   */
  public int getMaxLifetime() {
    return maxLifetime;
  }

  /**
   * Establish a max lifetime for pooled connections, a value of zero disables the maximum lifetime.
   *
   * @param maxLifetime the pool connection max lifetime
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setMaxLifetime(int maxLifetime) {
    Arguments.require(maxLifetime >= 0, "maxLifetime must be >= 0");
    this.maxLifetime = maxLifetime;
    return this;
  }

  /**
   * @return the connection pool cleaner period in ms.
   */
  public int getPoolCleanerPeriod() {
    return poolCleanerPeriod;
  }

  /**
   * Set the connection pool cleaner period in milli seconds, a non positive value disables expiration checks and connections
   * will remain in the pool until they are closed.
   *
   * @param poolCleanerPeriod the pool cleaner period
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setPoolCleanerPeriod(int poolCleanerPeriod) {
    this.poolCleanerPeriod = poolCleanerPeriod;
    return this;
  }

  /**
   * @return the time unit of @link #setConnectionTimeout(int)}
   */
  public TimeUnit getConnectionTimeoutUnit() {
    return connectionTimeoutUnit;
  }

  /**
   * Set the time unit of {@link #setConnectionTimeout(int)}
   *
   * @param timeoutUnit the time unit
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setConnectionTimeoutUnit(TimeUnit timeoutUnit) {
    this.connectionTimeoutUnit = timeoutUnit;
    return this;
  }

  /**
   * @return the amount of time a client will wait for a connection from the pool. See {@link #setConnectionTimeout(int)}
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Set the amount of time a client will wait for a connection from the pool. If the time is exceeded
   * without a connection available, an exception is provided.
   *
   * @param timeout the pool connection idle time unit
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setConnectionTimeout(int timeout) {
    this.connectionTimeout = timeout;
    return this;
  }

  /**
   * @return whether the pool is shared
   */
  public boolean isShared() {
    return shared;
  }

  /**
   * Set to {@code true} to share the pool.
   *
   * <p> There can be multiple shared pools distinguished by {@link #getName()}, when no specific
   * name is set, the {@link #DEFAULT_NAME} is used.
   *
   * @param shared {@code true} to use a shared pool
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setShared(boolean shared) {
    this.shared = shared;
    return this;
  }

  /**
   * @return the pool name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the pool name, used when the pool shared, otherwise ignored.
   * @param name the new name
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setName(String name) {
    Objects.requireNonNull(name, "Pool name cannot be null");
    this.name = name;
    return this;
  }

  /**
   * @return the max number of event-loop a pool will use, the default value is {@code 0} which implies
   * to reuse the current event-loop
   */
  public int getEventLoopSize() {
    return eventLoopSize;
  }

  /**
   * Set the number of event-loop the pool use.
   *
   * <ul>
   *   <li>when the size is {@code 0}, the client pool will use the current event-loop</li>
   *   <li>otherwise the client will create and use its own event loop</li>
   * </ul>
   *
   * The default size is {@code 0}.
   *
   * @param eventLoopSize  the new size
   * @return a reference to this, so the API can be used fluently
   */
  public PoolOptions setEventLoopSize(int eventLoopSize) {
    Arguments.require(eventLoopSize >= 0, "poolEventLoopSize must be >= 0");
    this.eventLoopSize = eventLoopSize;
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
