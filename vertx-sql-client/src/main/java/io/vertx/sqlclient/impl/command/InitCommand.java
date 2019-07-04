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

package io.vertx.sqlclient.impl.command;

import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;

import java.util.Map;

/**
 * Initialize the connection so it can be used to interact with the database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InitCommand extends CommandBase<Connection> {

  private final SocketConnectionBase conn;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> properties;

  public InitCommand(
    SocketConnectionBase conn,
    String username,
    String password,
    String database,
    Map<String, String> properties) {
    this.conn = conn;
    this.username = username;
    this.password = password;
    this.database = database;
    this.properties = properties;
  }

  public SocketConnectionBase connection() {
    return conn;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String database() {
    return database;
  }

  public Map<String, String> properties() {
    return properties;
  }

}
