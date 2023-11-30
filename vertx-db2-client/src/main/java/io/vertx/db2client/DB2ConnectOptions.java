/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.db2client.impl.DB2ConnectionUriParser;
import io.vertx.db2client.impl.drda.SQLState;
import io.vertx.db2client.impl.drda.SqlCode;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * Connect options for configuring {@link DB2Connection} or {@link DB2Builder}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class DB2ConnectOptions extends SqlConnectOptions {

  /**
   * @return the {@code options} as DB2 specific connect options
   */
  public static DB2ConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof DB2ConnectOptions) {
      return (DB2ConnectOptions) options;
    } else {
      return new DB2ConnectOptions(options);
    }
  }

  /**
   * Provide a {@link DB2ConnectOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link DB2ConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an
   *                                  invalid format
   */
  public static DB2ConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = DB2ConnectionUriParser.parse(connectionUri);
    return new DB2ConnectOptions(parsedConfiguration);
  }

  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 50000;
  public static final String DEFAULT_CHARSET = "utf8";
  public static final boolean DEFAULT_USE_AFFECTED_ROWS = false;
  public static final int DEFAULT_PIPELINING_LIMIT = 1; // 256; // TODO default to 256 once implemented properly
  public static final Map<String, String> DEFAULT_CONNECTION_ATTRIBUTES;
  public static final boolean DEFAULT_SSL = false;

  static {
    Map<String, String> defaultAttributes = new HashMap<>();
    defaultAttributes.put("_client_name", "vertx-db2-client");
    DEFAULT_CONNECTION_ATTRIBUTES = Collections.unmodifiableMap(defaultAttributes);
  }

  private boolean ssl = DEFAULT_SSL;
  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

  public DB2ConnectOptions() {
    super();
  }

  public DB2ConnectOptions(JsonObject json) {
    super(json);
    DB2ConnectOptionsConverter.fromJson(json, this);
  }

  public DB2ConnectOptions(SqlConnectOptions other) {
    super(other);
    if (other instanceof DB2ConnectOptions) {
      DB2ConnectOptions opts = (DB2ConnectOptions) other;
      this.pipeliningLimit = opts.pipeliningLimit;
      this.ssl = opts.ssl;
    }
  }

  public DB2ConnectOptions(DB2ConnectOptions other) {
    super(other);
    this.pipeliningLimit = other.pipeliningLimit;
    this.ssl = other.ssl;
  }

  @Override
  public DB2ConnectOptions setHost(String host) {
    return (DB2ConnectOptions) super.setHost(host);
  }

  @Override
  public DB2ConnectOptions setPort(int port) {
    return (DB2ConnectOptions) super.setPort(port);
  }

  @Override
  public DB2ConnectOptions setUser(String user) {
    if (user == null || user.length() < 1) {
      throw new DB2Exception("The user cannot be blank or null", SqlCode.MISSING_CREDENTIALS,
          SQLState.CONNECT_USERID_ISNULL);
    } else {
      return (DB2ConnectOptions) super.setUser(user);
    }
  }

  @Override
  public DB2ConnectOptions setPassword(String password) {
    if (password == null || password.length() < 1) {
      throw new DB2Exception("The password cannot be blank or null", SqlCode.MISSING_CREDENTIALS,
          SQLState.CONNECT_PASSWORD_ISNULL);
    } else {
      return (DB2ConnectOptions) super.setPassword(password);
    }
  }

  @Override
  public DB2ConnectOptions setDatabase(String database) {
    if (database == null || database.length() < 1) {
      throw new DB2Exception("The database name cannot be blank or null", SqlCode.DATABASE_NOT_FOUND,
          SQLState.DATABASE_NOT_FOUND);
    } else {
      return (DB2ConnectOptions) super.setDatabase(database);
    }
  }

  @Override
  public DB2ConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (DB2ConnectOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public DB2ConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    return (DB2ConnectOptions) super.setPreparedStatementCacheMaxSize(preparedStatementCacheMaxSize);
  }

  @GenIgnore
  @Override
  public DB2ConnectOptions setPreparedStatementCacheSqlFilter(Predicate<String> predicate) {
    return (DB2ConnectOptions) super.setPreparedStatementCacheSqlFilter(predicate);
  }

  @Override
  public DB2ConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    return (DB2ConnectOptions) super.setPreparedStatementCacheSqlLimit(preparedStatementCacheSqlLimit);
  }

  /**
   *
   * @return is SSL/TLS enabled?
   */
  public boolean isSsl() {
    return ssl;
  }

  /**
   * Set whether SSL/TLS is enabled
   *
   * @param ssl  true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public DB2ConnectOptions setSsl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  /**
   * @deprecated UNSTABLE FEATURE: Current default value is 1, anything higher
   *             than 1 will result in errors currently.
   * @param pipeliningLimit the number of commands that can simultaneously use the
   *                        same physical socket connection.
   * @return A reference to this, so the API can be used fluently
   */
  @GenIgnore
  @Deprecated // TODO: Get pipelining working properly, or remove this as API
  public DB2ConnectOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException();
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }

  @Override
  public DB2ConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    return (DB2ConnectOptions) super.setTracingPolicy(tracingPolicy);
  }

  @Override
  public DB2ConnectOptions setProperties(Map<String, String> properties) {
    return (DB2ConnectOptions) super.setProperties(properties);
  }

  @GenIgnore
  @Override
  public DB2ConnectOptions addProperty(String key, String value) {
    return (DB2ConnectOptions) super.addProperty(key, value);
  }

  @Override
  public DB2ConnectOptions setSslOptions(ClientSSLOptions sslOptions) {
    return (DB2ConnectOptions) super.setSslOptions(sslOptions);
  }

  /**
   * Initialize with the default options.
   */
  protected void init() {
    this.setHost(DEFAULT_HOST);
    this.setPort(DEFAULT_PORT);
    this.setProperties(new HashMap<>(DEFAULT_CONNECTION_ATTRIBUTES));
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof DB2ConnectOptions))
      return false;
    if (!super.equals(o))
      return false;

    DB2ConnectOptions that = (DB2ConnectOptions) o;

    if (pipeliningLimit != that.pipeliningLimit)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipeliningLimit);
  }

  @Override
  public DB2ConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new DB2ConnectOptions(json);
  }
}
