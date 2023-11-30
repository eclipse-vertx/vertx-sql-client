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
package io.vertx.oracleclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.oracleclient.impl.OracleConnectionUriParser;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.Map;
import java.util.function.Predicate;

@DataObject
@JsonGen(publicConverter = false)
public class OracleConnectOptions extends SqlConnectOptions {

  /**
   * @return the {@code options} as Oracle specific connect options
   */
  public static OracleConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof OracleConnectOptions) {
      return (OracleConnectOptions) options;
    } else {
      return new OracleConnectOptions(options);
    }
  }

  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 1521;
  public static final String DEFAULT_USER = "";
  public static final String DEFAULT_PASSWORD = "";
  public static final String DEFAULT_DATABASE = "";

  private String serviceId;
  private String serviceName;
  private ServerMode serverMode;
  private String instanceName;
  private String tnsAlias;
  private String tnsAdmin;

  public OracleConnectOptions() {
    super();
  }

  public OracleConnectOptions(OracleConnectOptions other) {
    super(other);
    copyFields(other);
  }

  private void copyFields(OracleConnectOptions other) {
    this.serviceId = other.serviceId;
    this.serviceName = other.serviceName;
    this.serverMode = other.serverMode;
    this.instanceName = other.instanceName;
    this.tnsAlias = other.tnsAlias;
    this.tnsAdmin = other.tnsAdmin;
  }

  public OracleConnectOptions(SqlConnectOptions options) {
    super(options);
    if (options instanceof OracleConnectOptions) {
      OracleConnectOptions opts = (OracleConnectOptions) options;
      copyFields(opts);
    }
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

  /**
   * @return the Oracle service identifier (SID)
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Set the Oracle service identifier (SID).
   * If set, the client will build an Oracle connection URL using SID instead of the EZConnect format.
   *
   * @param serviceId the service identifier
   * @return a reference to this, so the API can be used fluently
   */
  public OracleConnectOptions setServiceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  /**
   * @return the Oracle service name
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Set the Oracle service name.
   * If set, the client will build an Oracle connection URL in the EZConnect format.
   *
   * @param serviceName the Oracle service name
   * @return a reference to this, so the API can be used fluently
   */
  public OracleConnectOptions setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  /**
   * @return the server connection mode
   */
  public ServerMode getServerMode() {
    return serverMode;
  }

  /**
   * Set the server connection mode.
   *
   * @param serverMode the connection mode
   * @return a reference to this, so the API can be used fluently
   */
  public OracleConnectOptions setServerMode(ServerMode serverMode) {
    this.serverMode = serverMode;
    return this;
  }

  /**
   * @return the Oracle instance name
   */
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * Set the Oracle instance name.
   *
   * @param instanceName the instance name
   * @return a reference to this, so the API can be used fluently
   */
  public OracleConnectOptions setInstanceName(String instanceName) {
    this.instanceName = instanceName;
    return this;
  }

  /**
   * @return name of the alias configured in the {@code tnsnames.ora} file
   */
  public String getTnsAlias() {
    return tnsAlias;
  }

  /**
   * Set the name of an alias configured in the {@code tnsnames.ora} file.
   *
   * @param tnsAlias the instance name
   * @return a reference to this, so the API can be used fluently
   */
  public OracleConnectOptions setTnsAlias(String tnsAlias) {
    this.tnsAlias = tnsAlias;
    return this;
  }

  /**
   * @return the path of the directory that contains the {@code tnsnames.ora} file
   */
  public String getTnsAdmin() {
    return tnsAdmin;
  }

  /**
   * Set the path of the directory that contains the {@code tnsnames.ora} file.
   *
   * @param tnsAdmin path of the directory
   * @return a reference to this, so the API can be used fluently
   */
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

  /**
   * Set the database name.
   * If set, the client will build an Oracle connection URL in the EZConnect format using the {@code database} value as service name.
   *
   * @param database the database name to specify
   * @return a reference to this, so the API can be used fluently
   */
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

  @GenIgnore
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

  @GenIgnore
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
  public OracleConnectOptions setSsl(boolean ssl) {
    return (OracleConnectOptions) super.setSsl(ssl);
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    OracleConnectOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  protected void init() {
    this.setHost(DEFAULT_HOST);
    this.setPort(DEFAULT_PORT);
    this.setUser(DEFAULT_USER);
    this.setPassword(DEFAULT_PASSWORD);
    this.setDatabase(DEFAULT_DATABASE);
  }

  @Override
  public OracleConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new OracleConnectOptions(json);
  }
}
