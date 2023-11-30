/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetClientOptionsConverter;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.spi.Driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * Connect options for configuring {@link SqlConnection} or {@link Pool}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class SqlConnectOptions extends NetClientOptions {

  /**
   * Provide a {@link SqlConnectOptions} subclass configured from {@code connectionUri}.
   *
   * <p> This tries to locate among the available databases implementations the one matching the connection URI protocol, e.g
   * this returns {@code PgConnectOptions} for an URI that starts with {@code postgresql://}.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link SqlConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   * @throws ServiceConfigurationError when a database for the {@code connectionUri} could not be found
   */
  public static SqlConnectOptions fromUri(String connectionUri) throws IllegalArgumentException, ServiceConfigurationError {
    List<SqlConnectOptions> candidates = new ArrayList<>(1);
    for (Driver d : ServiceLoader.load(Driver.class)) {
      SqlConnectOptions options = d.parseConnectionUri(connectionUri);
      if (options != null) {
        candidates.add(options);
      }
    }
    if (candidates.size() == 0) {
      throw new ServiceConfigurationError("No implementations of " + Driver.class + " found that accept connection uri " + connectionUri);
    } else if (candidates.size() > 1) {
      throw new ServiceConfigurationError("Multiple implementations of " + Driver.class + " found: " + candidates);
    } else {
      return candidates.get(0);
    }
  }

  public static final boolean DEFAULT_CACHE_PREPARED_STATEMENTS = false;
  public static final int DEFAULT_PREPARED_STATEMENT_CACHE_MAX_SIZE = 256;
  public static final int DEFAULT_PREPARED_STATEMENT_CACHE_SQL_LIMIT = 2048;
  public static final Predicate<String> DEFAULT_PREPARED_STATEMENT_CACHE_FILTER = sql -> sql.length() < DEFAULT_PREPARED_STATEMENT_CACHE_SQL_LIMIT;

  private String host;
  private int port;
  private String user;
  private String password;
  private String database;
  private boolean cachePreparedStatements = DEFAULT_CACHE_PREPARED_STATEMENTS;
  private int preparedStatementCacheMaxSize = DEFAULT_PREPARED_STATEMENT_CACHE_MAX_SIZE;
  private Predicate<String> preparedStatementCacheSqlFilter = DEFAULT_PREPARED_STATEMENT_CACHE_FILTER;
  private Map<String, String> properties = new HashMap<>(4);
  private TracingPolicy tracingPolicy;

  public SqlConnectOptions() {
    super();
    init();
  }

  public SqlConnectOptions(JsonObject json) {
    super(json);
    init();
    SqlConnectOptionsConverter.fromJson(json, this);
  }

  public SqlConnectOptions(SqlConnectOptions other) {
    super(other);
    this.host = other.host;
    this.port = other.port;
    this.user = other.user;
    this.password = other.password;
    this.database = other.database;
    this.cachePreparedStatements = other.cachePreparedStatements;
    this.preparedStatementCacheMaxSize = other.preparedStatementCacheMaxSize;
    this.preparedStatementCacheSqlFilter = other.preparedStatementCacheSqlFilter;
    if (other.properties != null) {
      this.properties = new HashMap<>(other.properties);
    }
  }

  /**
   * Get the host for connecting to the server.
   *
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * Specify the host for connecting to the server.
   *
   * @param host the host to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setHost(String host) {
    Objects.requireNonNull(host, "Host can not be null");
    this.host = host;
    return this;
  }

  /**
   * Get the port for connecting to the server.
   *
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Specify the port for connecting to the server.
   *
   * @param port the port to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setPort(int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Port should range in 0-65535");
    }
    this.port = port;
    return this;
  }

  /**
   * Get the user account to be used for the authentication.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Specify the user account to be used for the authentication.
   *
   * @param user the user to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setUser(String user) {
    Objects.requireNonNull(user, "User account can not be null");
    this.user = user;
    return this;
  }

  /**
   * Get the user password to be used for the authentication.
   *
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Specify the user password to be used for the authentication.
   *
   * @param password the password to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setPassword(String password) {
    Objects.requireNonNull(password, "Password can not be null");
    this.password = password;
    return this;
  }

  /**
   * Get the default database name for the connection.
   *
   * @return the database name
   */
  public String getDatabase() {
    return database;
  }

  /**
   * Specify the default database for the connection.
   *
   * @param database the database name to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setDatabase(String database) {
    Objects.requireNonNull(database, "Database name can not be null");
    this.database = database;
    return this;
  }

  /**
   * Get whether prepared statements cache is enabled.
   *
   * @return the value
   */
  public boolean getCachePreparedStatements() {
    return cachePreparedStatements;
  }

  /**
   * Set whether prepared statements cache should be enabled.
   *
   * @param cachePreparedStatements true if cache should be enabled
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    this.cachePreparedStatements = cachePreparedStatements;
    return this;
  }

  /**
   * Get the maximum number of prepared statements that the connection will cache.
   *
   * @return the size
   */
  public int getPreparedStatementCacheMaxSize() {
    return preparedStatementCacheMaxSize;
  }

  /**
   * Set the maximum number of prepared statements that the connection will cache.
   *
   * @param preparedStatementCacheMaxSize the size to set
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    this.preparedStatementCacheMaxSize = preparedStatementCacheMaxSize;
    return this;
  }

  /**
   * Get the predicate filtering prepared statements that the connection will cache.
   *
   * @return the current predicate
   */
  @GenIgnore
  public Predicate<String> getPreparedStatementCacheSqlFilter() {
    return preparedStatementCacheSqlFilter;
  }

  /**
   * Set a predicate filtering prepared statements that the connection will cache.
   *
   * <p> The default predicate accepts predicate having query length < {@link #DEFAULT_PREPARED_STATEMENT_CACHE_SQL_LIMIT}
   *
   * @param predicate the filter
   */
  @GenIgnore
  public SqlConnectOptions setPreparedStatementCacheSqlFilter(Predicate<String> predicate) {
    this.preparedStatementCacheSqlFilter = predicate;
    return this;
  }

  /**
   * Set the maximum length of prepared statement SQL string that the connection will cache.
   *
   * <p> This is an helper setting the {@link #setPreparedStatementCacheSqlFilter(Predicate)}.
   *
   * @param preparedStatementCacheSqlLimit the maximum length limit of SQL string to set
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    return setPreparedStatementCacheSqlFilter(sql -> sql.length() <= preparedStatementCacheSqlLimit);
  }

  /**
   * @return the value of current connection properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Set properties for this client, which will be sent to server at the connection start.
   *
   * @param properties the value of properties to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setProperties(Map<String, String> properties) {
    Objects.requireNonNull(properties, "Properties can not be null");
    this.properties = properties;
    return this;
  }

  /**
   * Add a property for this client, which will be sent to server at the connection start.
   *
   * @param key the value of property key
   * @param value the value of property value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  public SqlConnectOptions addProperty(String key, String value) {
    Objects.requireNonNull(key, "Property key can not be null");
    Objects.requireNonNull(value, "Property value can not be null");
    this.properties.put(key, value);
    return this;
  }


  @GenIgnore
  public SocketAddress getSocketAddress() {
    return SocketAddress.inetSocketAddress(getPort(), getHost());
  }

  /**
   * @return the tracing policy
   */
  public TracingPolicy getTracingPolicy() {
    return tracingPolicy;
  }

  /**
   * Set the tracing policy for the client behavior when Vert.x has tracing enabled.
   *
   * @param tracingPolicy the tracing policy
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    this.tracingPolicy = tracingPolicy;
    return this;
  }

  public boolean isUsingDomainSocket() {
    return false;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    SqlConnectOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * Initialize with the default options.
   */
  protected void init() {
  }

  /**
   * Returns new options created after this object and merged with the {@code other} json config.
   *
   * @param other the other JSON object
   * @return new options created after this object and merged with the {@code other} json config
   */
  public SqlConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new SqlConnectOptions(json);
  }
}
