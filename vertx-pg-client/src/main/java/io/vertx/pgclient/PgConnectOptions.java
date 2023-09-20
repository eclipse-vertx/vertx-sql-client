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

package io.vertx.pgclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Unstable;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.pgclient.impl.PgConnectionUriParser;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author Billy Yuan <billy112487983@gmail.com>
 */
@DataObject(generateConverter = true)
public class PgConnectOptions extends SqlConnectOptions {

  /**
   * @return the {@code options} as PostgreSQL specific connect options
   */
  public static PgConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof PgConnectOptions) {
      return (PgConnectOptions) options;
    } else {
      return new PgConnectOptions(options);
    }
  }

  /**
   * Provide a {@link PgConnectOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link PgConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static PgConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = PgConnectionUriParser.parse(connectionUri);
    return new PgConnectOptions(parsedConfiguration);
  }

  /**
   * Provide a {@link PgConnectOptions} configured with environment variables, if the environment variable
   * is not set, then a default value will take precedence over this.
   */
  public static PgConnectOptions fromEnv() {
    PgConnectOptions pgConnectOptions = new PgConnectOptions();

    if (getenv("PGHOSTADDR") == null) {
      if (getenv("PGHOST") != null) {
        pgConnectOptions.setHost(getenv("PGHOST"));
      }
    } else {
      pgConnectOptions.setHost(getenv("PGHOSTADDR"));
    }

    if (getenv("PGPORT") != null) {
      try {
        pgConnectOptions.setPort(parseInt(getenv("PGPORT")));
      } catch (NumberFormatException e) {
        // port will be set to default
      }
    }

    if (getenv("PGDATABASE") != null) {
      pgConnectOptions.setDatabase(getenv("PGDATABASE"));
    }
    if (getenv("PGUSER") != null) {
      pgConnectOptions.setUser(getenv("PGUSER"));
    }
    if (getenv("PGPASSWORD") != null) {
      pgConnectOptions.setPassword(getenv("PGPASSWORD"));
    }
    if (getenv("PGSSLMODE") != null) {
      pgConnectOptions.setSslMode(SslMode.of(getenv("PGSSLMODE")));
    }
    return pgConnectOptions;
  }

  public static final String DEFAULT_HOST = "localhost";
  public static int DEFAULT_PORT = 5432;
  public static final String DEFAULT_DATABASE = "db";
  public static final String DEFAULT_USER = "user";
  public static final String DEFAULT_PASSWORD = "pass";
  public static final int DEFAULT_PIPELINING_LIMIT = 256;
  public static final SslMode DEFAULT_SSLMODE = SslMode.DISABLE;
  public static final boolean DEFAULT_USE_LAYER_7_PROXY = false;
  public static final Map<String, String> DEFAULT_PROPERTIES;

  static {
    Map<String, String> defaultProperties = new HashMap<>();
    defaultProperties.put("application_name", "vertx-pg-client");
    defaultProperties.put("client_encoding", "utf8");
    defaultProperties.put("DateStyle", "ISO");
    defaultProperties.put("extra_float_digits", "2");
    DEFAULT_PROPERTIES = Collections.unmodifiableMap(defaultProperties);
  }

  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;
  private SslMode sslMode = DEFAULT_SSLMODE;
  private boolean useLayer7Proxy = DEFAULT_USE_LAYER_7_PROXY;

  public PgConnectOptions() {
    super();
  }

  public PgConnectOptions(JsonObject json) {
    super(json);
    PgConnectOptionsConverter.fromJson(json, this);
  }

  public PgConnectOptions(SqlConnectOptions other) {
    super(other);
    if (other instanceof PgConnectOptions) {
      PgConnectOptions opts = (PgConnectOptions) other;
      pipeliningLimit = opts.pipeliningLimit;
      sslMode = opts.sslMode;
    }
  }

  public PgConnectOptions(PgConnectOptions other) {
    super(other);
    pipeliningLimit = other.pipeliningLimit;
    sslMode = other.sslMode;
  }

  @Override
  public PgConnectOptions setHost(String host) {
    return (PgConnectOptions) super.setHost(host);
  }

  @Override
  public PgConnectOptions setPort(int port) {
    return (PgConnectOptions) super.setPort(port);
  }

  @Override
  public PgConnectOptions setUser(String user) {
    return (PgConnectOptions) super.setUser(user);
  }

  @Override
  public PgConnectOptions setPassword(String password) {
    return (PgConnectOptions) super.setPassword(password);
  }

  @Override
  public PgConnectOptions setDatabase(String database) {
    return (PgConnectOptions) super.setDatabase(database);
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  public PgConnectOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException();
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }

  public PgConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (PgConnectOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public PgConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    return (PgConnectOptions) super.setPreparedStatementCacheMaxSize(preparedStatementCacheMaxSize);
  }

  @GenIgnore
  @Override
  public PgConnectOptions setPreparedStatementCacheSqlFilter(Predicate<String> predicate) {
    return (PgConnectOptions) super.setPreparedStatementCacheSqlFilter(predicate);
  }

  @Override
  public PgConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    return (PgConnectOptions) super.setPreparedStatementCacheSqlLimit(preparedStatementCacheSqlLimit);
  }

  @Override
  public PgConnectOptions setProperties(Map<String, String> properties) {
    return (PgConnectOptions) super.setProperties(properties);
  }

  @GenIgnore
  @Override
  public PgConnectOptions addProperty(String key, String value) {
    return (PgConnectOptions) super.addProperty(key, value);
  }

  /**
   * @return the value of current sslmode
   */
  public SslMode getSslMode() {
    return sslMode;
  }

  /**
   * Set {@link SslMode} for the client, this option can be used to provide different levels of secure protection.
   *
   * @param sslmode the value of sslmode
   * @return a reference to this, so the API can be used fluently
   */
  public PgConnectOptions setSslMode(SslMode sslmode) {
    this.sslMode = sslmode;
    return this;
  }

  /**
   * @return whether the client interacts with a layer 7 proxy instead of a server
   */
  @Unstable
  public boolean getUseLayer7Proxy() {
    return useLayer7Proxy;
  }

  /**
   * Set the client to use a layer 7 (application) proxy compatible protocol, set to {@code true} when the client
   * interacts with a layer 7 proxy like PgBouncer instead of a server. Prepared statement caching must be disabled.
   *
   * @param useLayer7Proxy whether to use a layer 7 proxy instead of a server
   * @return a reference to this, so the API can be used fluently
   */
  @Unstable
  public PgConnectOptions setUseLayer7Proxy(boolean useLayer7Proxy) {
    this.useLayer7Proxy = useLayer7Proxy;
    return this;
  }

  @Override
  public PgConnectOptions setReconnectAttempts(int attempts) {
    return (PgConnectOptions)super.setReconnectAttempts(attempts);
  }

  @Override
  public PgConnectOptions setReconnectInterval(long interval) {
    return (PgConnectOptions)super.setReconnectInterval(interval);
  }

  @Override
  public PgConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    return (PgConnectOptions) super.setTracingPolicy(tracingPolicy);
  }

  @Override
  public PgConnectOptions setSslOptions(ClientSSLOptions sslOptions) {
    return (PgConnectOptions) super.setSslOptions(sslOptions);
  }

  /**
   * Initialize with the default options.
   */
  protected void init() {
    this.setHost(DEFAULT_HOST);
    this.setPort(DEFAULT_PORT);
    this.setUser(DEFAULT_USER);
    this.setPassword(DEFAULT_PASSWORD);
    this.setDatabase(DEFAULT_DATABASE);
    this.setProperties(new HashMap<>(DEFAULT_PROPERTIES));
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    PgConnectOptionsConverter.toJson(this, json);
    return json;
  }

  @GenIgnore
  public SocketAddress getSocketAddress() {
    if (!isUsingDomainSocket()) {
      return super.getSocketAddress();
    } else {
      return SocketAddress.domainSocketAddress(getHost() + "/.s.PGSQL." + getPort());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PgConnectOptions)) return false;
    if (!super.equals(o)) return false;

    PgConnectOptions that = (PgConnectOptions) o;

    if (pipeliningLimit != that.pipeliningLimit) return false;
    if (sslMode != that.sslMode) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + pipeliningLimit;
    result = 31 * result + sslMode.hashCode();
    return result;
  }

  public boolean isUsingDomainSocket() {
    return this.getHost().startsWith("/");
  }

  @Override
  public PgConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new PgConnectOptions(json);
  }
}
