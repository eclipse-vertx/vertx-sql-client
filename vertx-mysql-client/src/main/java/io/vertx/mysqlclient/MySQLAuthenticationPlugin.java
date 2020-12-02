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

package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;

/**
 * MySQL authentication plugins which can be specified at the connection start, more information could be found in <a href="https://dev.mysql.com/doc/refman/8.0/en/authentication-plugins.html">MySQL Reference Manual</a>.
 */
@VertxGen
public enum MySQLAuthenticationPlugin {

  /**
   * Default authentication plugin, the client will firstly try to use the plugin name provided by the server.
   */
  DEFAULT(null),

  /**
   * Authentication plugin which enables the client to send password to the server as cleartext without encryption.
   */
  MYSQL_CLEAR_PASSWORD("mysql_clear_password"),

  /**
   * Authentication plugin which uses SHA-1 hash function to scramble the password and send it to the server.
   */
  MYSQL_NATIVE_PASSWORD("mysql_native_password"),

  /**
   * Authentication plugin which uses SHA-256 hash function to scramble the password and send it to the server.
   */
  SHA256_PASSWORD("sha256_password"),

  /**
   * Like {@code sha256_password} but enables caching on the server side for better performance and with wider applicability.
   */
  CACHING_SHA2_PASSWORD("caching_sha2_password");

  public final String value;

  MySQLAuthenticationPlugin(String value) {
    this.value = value;
  }

}
