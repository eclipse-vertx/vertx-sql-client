/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.mssqlclient.impl.MSSQLConnectionUriParser;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Connect options for configuring {@link MSSQLConnection}.
 */
@DataObject(generateConverter = true)
public class MSSQLConnectOptions extends SqlConnectOptions {

  /**
   * @return the {@code options} as MSSQL specific connect options
   */
  public static MSSQLConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof MSSQLConnectOptions) {
      return (MSSQLConnectOptions) options;
    } else {
      return new MSSQLConnectOptions(options);
    }
  }

  /**
   * Provide a {@link MSSQLConnectOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link MSSQLConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static MSSQLConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = MSSQLConnectionUriParser.parse(connectionUri);
    return new MSSQLConnectOptions(parsedConfiguration);
  }

  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 1433;
  public static final String DEFAULT_USER = "sa";
  public static final String DEFAULT_PASSWORD = "";
  public static final String DEFAULT_DATABASE = "";
  public static final String DEFAULT_APP_NAME = "vertx-mssql-client";
  public static final String DEFAULT_CLIENT_INTERFACE_NAME = "Vert.x";
  public static final Map<String, String> DEFAULT_PROPERTIES;
  public static final int MIN_PACKET_SIZE = 512;
  public static final int MAX_PACKET_SIZE = 32767;
  public static final int DEFAULT_PACKET_SIZE = 4096;
  public static final boolean DEFAULT_SSL = false;

  static {
    Map<String, String> defaultProperties = new HashMap<>();
    defaultProperties.put("appName", DEFAULT_APP_NAME);
    defaultProperties.put("clientInterfaceName", DEFAULT_CLIENT_INTERFACE_NAME);
    DEFAULT_PROPERTIES = defaultProperties;
  }

  private boolean ssl;
  private int packetSize;

  public MSSQLConnectOptions() {
    super();
  }

  public MSSQLConnectOptions(JsonObject json) {
    super(json);
    MSSQLConnectOptionsConverter.fromJson(json, this);
  }

  public MSSQLConnectOptions(SqlConnectOptions other) {
    super(other);
    if (other instanceof MSSQLConnectOptions) {
      MSSQLConnectOptions opts = (MSSQLConnectOptions) other;
      copyFields(opts);
    }
  }

  public MSSQLConnectOptions(MSSQLConnectOptions other) {
    super(other);
    copyFields(other);
  }

  private void copyFields(MSSQLConnectOptions other) {
    packetSize = other.packetSize;
    ssl = other.ssl;
  }

  @Override
  public MSSQLConnectOptions setHost(String host) {
    return (MSSQLConnectOptions) super.setHost(host);
  }

  @Override
  public MSSQLConnectOptions setPort(int port) {
    return (MSSQLConnectOptions) super.setPort(port);
  }

  @Override
  public MSSQLConnectOptions setUser(String user) {
    return (MSSQLConnectOptions) super.setUser(user);
  }

  @Override
  public MSSQLConnectOptions setPassword(String password) {
    return (MSSQLConnectOptions) super.setPassword(password);
  }

  @Override
  public MSSQLConnectOptions setDatabase(String database) {
    return (MSSQLConnectOptions) super.setDatabase(database);
  }

  @Override
  public MSSQLConnectOptions setProperties(Map<String, String> properties) {
    return (MSSQLConnectOptions) super.setProperties(properties);
  }

  @Override
  public MSSQLConnectOptions addProperty(String key, String value) {
    return (MSSQLConnectOptions) super.addProperty(key, value);
  }

  /**
   * Get the desired size (in bytes) for TDS packets.
   *
   * @return the desired packet size
   */
  public int getPacketSize() {
    return packetSize;
  }

  /**
   * Set the desired size (in bytes) for TDS packets.
   * <p>
   * The client will use the value as a parameter in the LOGIN7 packet.
   * The server may or may not accept it.
   *
   * @param packetSize the desired packet size (in bytes)
   * @return a reference to this, so the API can be used fluently
   * @throws IllegalArgumentException if {@code packetSize} is smaller than {@link #MIN_PACKET_SIZE} or bigger than {@link #MAX_PACKET_SIZE}
   */
  public MSSQLConnectOptions setPacketSize(int packetSize) {
    if (packetSize < MIN_PACKET_SIZE || packetSize > MAX_PACKET_SIZE) {
      throw new IllegalArgumentException("Packet size: " + packetSize);
    }
    this.packetSize = packetSize;
    return this;
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
  public MSSQLConnectOptions setSsl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }

  @Override
  public MSSQLConnectOptions setReconnectAttempts(int attempts) {
    return (MSSQLConnectOptions) super.setReconnectAttempts(attempts);
  }

  @Override
  public MSSQLConnectOptions setReconnectInterval(long interval) {
    return (MSSQLConnectOptions) super.setReconnectInterval(interval);
  }

  @Override
  public MSSQLConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (MSSQLConnectOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public MSSQLConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    return (MSSQLConnectOptions) super.setPreparedStatementCacheMaxSize(preparedStatementCacheMaxSize);
  }

  @Override
  public MSSQLConnectOptions setPreparedStatementCacheSqlFilter(Predicate<String> predicate) {
    return (MSSQLConnectOptions) super.setPreparedStatementCacheSqlFilter(predicate);
  }

  @Override
  public MSSQLConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    return (MSSQLConnectOptions) super.setPreparedStatementCacheSqlLimit(preparedStatementCacheSqlLimit);
  }

  @Override
  public MSSQLConnectOptions setSslOptions(ClientSSLOptions sslOptions) {
    return (MSSQLConnectOptions) super.setSslOptions(sslOptions);
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
    packetSize = DEFAULT_PACKET_SIZE;
    ssl = DEFAULT_SSL;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MSSQLConnectOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public MSSQLConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new MSSQLConnectOptions(json);
  }
}
