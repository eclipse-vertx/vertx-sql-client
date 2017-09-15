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

package com.julienviet.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PgClientOptions {

  public static final String DEFAULT_HOST = "localhost";
  public static int DEFAULT_PORT = 5432;
  public static final String DEFAULT_DATABASE = "db";
  public static final String DEFAULT_USERNAME = "user";
  public static final String DEFAULT_PASSWORD = "pass";
  public static final boolean DEFAULT_CACHE_PREPARED_STATEMENTS = false;
  public static final int DEFAULT_PIPELINING_LIMIT = 256;
  public static final boolean DEFAULT_SSL = false;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private String database = DEFAULT_DATABASE;
  private String username = DEFAULT_USERNAME;
  private String password = DEFAULT_PASSWORD;
  private boolean cachePreparedStatements = DEFAULT_CACHE_PREPARED_STATEMENTS;
  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;
  private boolean ssl = DEFAULT_SSL;

  public PgClientOptions() {
  }

  public PgClientOptions(JsonObject json) {
    PgClientOptionsConverter.fromJson(json, this);
  }

  public PgClientOptions(PgClientOptions other) {
    host = other.host;
    port = other.port;
    database = other.database;
    username = other.username;
    password = other.password;
    pipeliningLimit = other.pipeliningLimit;
    ssl = other.ssl;
  }

  public String getHost() {
    return host;
  }

  public PgClientOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public PgClientOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getDatabase() {
    return database;
  }

  public PgClientOptions setDatabase(String database) {
    this.database = database;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public PgClientOptions setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public PgClientOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  public PgClientOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException();
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }

  public boolean getCachePreparedStatements() {
    return cachePreparedStatements;
  }

  public PgClientOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    this.cachePreparedStatements = cachePreparedStatements;
    return this;
  }

  public boolean isSsl() {
    return ssl;
  }

  public PgClientOptions setSsl(boolean ssl) {
    this.ssl = ssl;
    return this;
  }
}
