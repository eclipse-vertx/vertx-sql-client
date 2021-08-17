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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.SqlConnectOptions;

@DataObject(generateConverter = true)
public class OracleConnectOptions extends SqlConnectOptions {

  private String schema;

  // Support TNS_ADMIN (tnsnames.ora, ojdbc.properties).
  private String tnsAdmin;

  private int connectTimeout;
  private int idleTimeout;


  private TracingPolicy tracingPolicy;

  public OracleConnectOptions(JsonObject toJson) {
    super(toJson);
    // TODO Copy
  }

  public OracleConnectOptions() {

  }

  public OracleConnectOptions(SqlConnectOptions options) {
    super(options);
    // TODO Copy
  }

  // TODO...

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
  public OracleConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    this.tracingPolicy = tracingPolicy;
    return this;
  }

  // Oracle specifics

  public String getTnsAdmin() {
    return tnsAdmin;
  }

  public OracleConnectOptions setTnsAdmin(String tnsAdmin) {
    this.tnsAdmin = tnsAdmin;
    return this;
  }

  @Override
  public OracleConnectOptions setPort(int port) {
    super.setPort(port);
    return this;
  }

  @Override
  public OracleConnectOptions setHost(String host) {
    super.setHost(host);
    return this;
  }

  @Override
  public OracleConnectOptions setDatabase(String db) {
    super.setDatabase(db);
    return this;
  }

  @Override
  public OracleConnectOptions setUser(String user) {
    super.setUser(user);
    return this;
  }

  @Override
  public OracleConnectOptions setPassword(String pwd) {
    super.setPassword(pwd);
    return this;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public OracleConnectOptions setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public int getIdleTimeout() {
    return idleTimeout;
  }

  public OracleConnectOptions setIdleTimeout(int idleTimeout) {
    this.idleTimeout = idleTimeout;
    return this;
  }

  public String getSchema() {
    return schema;
  }

  public OracleConnectOptions setSchema(String schema) {
    this.schema = schema;
    return this;
  }
}
