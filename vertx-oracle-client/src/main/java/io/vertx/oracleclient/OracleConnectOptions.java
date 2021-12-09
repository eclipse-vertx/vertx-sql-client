/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.oracleclient.impl.OracleConnectionUriParser;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.Map;
import java.util.function.Predicate;

@DataObject(generateConverter = true)
public class OracleConnectOptions extends SqlConnectOptions {

  // Support TNS_ADMIN (tnsnames.ora, ojdbc.properties).
  private String tnsAdmin;

  public OracleConnectOptions() {
  }

  public OracleConnectOptions(OracleConnectOptions other) {
    super(other);
    this.tnsAdmin = other.tnsAdmin;
  }

  public OracleConnectOptions(SqlConnectOptions options) {
    super(options);
  }

  public OracleConnectOptions(JsonObject json) {
    super(json);
    OracleConnectOptionsConverter.fromJson(json, this);
  }

  /**
   * Provide a {@link OracleConnectOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link OracleConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static OracleConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = OracleConnectionUriParser.parse(connectionUri);
    return new OracleConnectOptions(parsedConfiguration);
  }

  // Oracle-specific options

  public String getTnsAdmin() {
    return tnsAdmin;
  }

  public OracleConnectOptions setTnsAdmin(String tnsAdmin) {
    this.tnsAdmin = tnsAdmin;
    return this;
  }

  // Non-specific options

  @Override
  public String getHost() {
    return super.getHost();
  }

  @Override
  public OracleConnectOptions setHost(String host) {
    return (OracleConnectOptions) super.setHost(host);
  }

  @Override
  public int getPort() {
    return super.getPort();
  }

  @Override
  public OracleConnectOptions setPort(int port) {
    return (OracleConnectOptions) super.setPort(port);
  }

  @Override
  public String getUser() {
    return super.getUser();
  }

  @Override
  public OracleConnectOptions setUser(String user) {
    return (OracleConnectOptions) super.setUser(user);
  }

  @Override
  public String getPassword() {
    return super.getPassword();
  }

  @Override
  public OracleConnectOptions setPassword(String password) {
    return (OracleConnectOptions) super.setPassword(password);
  }

  @Override
  public String getDatabase() {
    return super.getDatabase();
  }

  @Override
  public OracleConnectOptions setDatabase(String database) {
    return (OracleConnectOptions) super.setDatabase(database);
  }

  @Override
  public boolean getCachePreparedStatements() {
    return super.getCachePreparedStatements();
  }

  @Override
  public OracleConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (OracleConnectOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public int getPreparedStatementCacheMaxSize() {
    return super.getPreparedStatementCacheMaxSize();
  }

  @Override
  public OracleConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    return (OracleConnectOptions) super.setPreparedStatementCacheMaxSize(preparedStatementCacheMaxSize);
  }

  @Override
  public Predicate<String> getPreparedStatementCacheSqlFilter() {
    return super.getPreparedStatementCacheSqlFilter();
  }

  @Override
  public OracleConnectOptions setPreparedStatementCacheSqlFilter(Predicate<String> predicate) {
    return (OracleConnectOptions) super.setPreparedStatementCacheSqlFilter(predicate);
  }

  @Override
  public OracleConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    return (OracleConnectOptions) super.setPreparedStatementCacheSqlLimit(preparedStatementCacheSqlLimit);
  }

  @Override
  public Map<String, String> getProperties() {
    return super.getProperties();
  }

  @Override
  public OracleConnectOptions setProperties(Map<String, String> properties) {
    return (OracleConnectOptions) super.setProperties(properties);
  }

  @Override
  public OracleConnectOptions addProperty(String key, String value) {
    return (OracleConnectOptions) super.addProperty(key, value);
  }

  @Override
  public SocketAddress getSocketAddress() {
    return super.getSocketAddress();
  }

  @Override
  public TracingPolicy getTracingPolicy() {
    return super.getTracingPolicy();
  }

  @Override
  public OracleConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    return (OracleConnectOptions) super.setTracingPolicy(tracingPolicy);
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    OracleConnectOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public OracleConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new OracleConnectOptions(json);
  }
}
