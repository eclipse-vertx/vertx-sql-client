/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.impl.MySQLCollation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.vertx.mysqlclient.MySQLConnectOptions.*;

/**
 * Authentication options for MySQL authentication which can be used for CHANGE_USER command.
 */
@DataObject(generateConverter = true)
public class MySQLAuthOptions {
  private String user;
  private String password;
  private String database;
  private String collation;
  private String charset;
  private String serverRsaPublicKeyPath;
  private Buffer serverRsaPublicKeyValue;
  private Map<String, String> properties;

  public MySQLAuthOptions() {
    init();
  }

  public MySQLAuthOptions(JsonObject json) {
    init();
    MySQLAuthOptionsConverter.fromJson(json, this);
  }

  public MySQLAuthOptions(MySQLAuthOptions other) {
    init();
    this.user = other.user;
    this.password = other.password;
    this.database = other.database;
    this.collation = other.collation;
    this.charset = other.charset;
    this.serverRsaPublicKeyPath = other.serverRsaPublicKeyPath;
    this.serverRsaPublicKeyValue = other.serverRsaPublicKeyValue != null ? other.serverRsaPublicKeyValue.copy() : null;
    this.properties = new HashMap<>(other.properties);
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
  public MySQLAuthOptions setUser(String user) {
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
  public MySQLAuthOptions setPassword(String password) {
    Objects.requireNonNull(password, "Password can not be null");
    this.password = password;
    return this;
  }

  /**
   * Get the database name for the re-authentication.
   *
   * @return the database name
   */
  public String getDatabase() {
    return database;
  }

  /**
   * Specify the default database for the re-authentication.
   *
   * @param database the database name to specify
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLAuthOptions setDatabase(String database) {
    Objects.requireNonNull(database, "Database name can not be null");
    this.database = database;
    return this;
  }

  /**
   * @return the value of current connection attributes
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Set connection attributes which will be sent to server at the re-authentication.
   *
   * @param properties the value of properties to specify
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLAuthOptions setProperties(Map<String, String> properties) {
    Objects.requireNonNull(properties, "Properties can not be null");
    this.properties = properties;
    return this;
  }

  /**
   * Add a property for this client, which will be sent to server at the re-authentication.
   *
   * @param key   the value of property key
   * @param value the value of property value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  public MySQLAuthOptions addProperty(String key, String value) {
    Objects.requireNonNull(key, "Property key can not be null");
    Objects.requireNonNull(value, "Property value can not be null");
    this.properties.put(key, value);
    return this;
  }

  /**
   * Get the collation for the connection.
   *
   * @return the MySQL collation
   */
  public String getCollation() {
    return collation;
  }

  /**
   * Set the collation for the connection.
   *
   * @param collation the collation to set
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLAuthOptions setCollation(String collation) {
    if (collation != null && !MySQLCollation.SUPPORTED_COLLATION_NAMES.contains(collation)) {
      throw new IllegalArgumentException("Unsupported collation: " + collation);
    }
    this.collation = collation;
    return this;
  }

  /**
   * Get the charset for the connection.
   *
   * @return the MySQL collation
   */
  public String getCharset() {
    return charset;
  }

  /**
   * Set the charset for the connection.
   *
   * @param charset the charset to set
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLAuthOptions setCharset(String charset) {
    if (charset != null && !MySQLCollation.SUPPORTED_CHARSET_NAMES.contains(charset)) {
      throw new IllegalArgumentException("Unsupported charset: " + charset);
    }
    this.charset = charset;
    return this;
  }

  /**
   * Get the path of the server RSA public key.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public String getServerRsaPublicKeyPath() {
    return serverRsaPublicKeyPath;
  }

  /**
   * Set the path of server RSA public key which is mostly used for encrypting password under insecure connections when performing authentication.
   *
   * @param serverRsaPublicKeyPath the path of the server RSA public key
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLAuthOptions setServerRsaPublicKeyPath(String serverRsaPublicKeyPath) {
    this.serverRsaPublicKeyPath = serverRsaPublicKeyPath;
    return this;
  }

  /**
   * Get the value of the server RSA public key.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public Buffer getServerRsaPublicKeyValue() {
    return serverRsaPublicKeyValue;
  }

  /**
   * Set the value of server RSA public key which is mostly used for encrypting password under insecure connections when performing authentication.
   *
   * @param serverRsaPublicKeyValue the path of the server RSA public key
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLAuthOptions setServerRsaPublicKeyValue(Buffer serverRsaPublicKeyValue) {
    this.serverRsaPublicKeyValue = serverRsaPublicKeyValue;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    MySQLAuthOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.user = DEFAULT_USER;
    this.password = DEFAULT_PASSWORD;
    this.database = DEFAULT_SCHEMA;
    this.charset = DEFAULT_CHARSET;
    this.properties = new HashMap<>(DEFAULT_CONNECTION_ATTRIBUTES);
  }
}
