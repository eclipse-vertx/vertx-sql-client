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
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;

import java.util.Objects;

/**
 * A db host to connect to.
 */
@DataObject(generateConverter = true)
public class SqlHost extends NetClientOptions {
  private String host;
  private int port;

  SqlHost() {
    init();
  }

  public SqlHost(String host, int port) {
    init();
    this.host = host;
    this.port = port;
  }

  public SqlHost(JsonObject json) {
    init();
    SqlHostConverter.fromJson(json, this);
  }

  public SqlHost(SqlHost other) {
    this.host = other.host;
    this.port = other.port;
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
  public SqlHost setHost(String host) {
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
  public SqlHost setPort(int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Port should range in 0-65535");
    }
    this.port = port;
    return this;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    SqlHostConverter.toJson(this, json);
    return json;
  }

  /**
   * Initialize with the default options.
   */
  protected void init() {
  }
}
