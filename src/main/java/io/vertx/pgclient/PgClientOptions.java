package io.vertx.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class PgClientOptions {

  private String host = "localhost";
  private int port = 5432;
  private String database = "db";
  private String username = "user";
  private String password = "pass";
  private int poolsize = 20;

  public PgClientOptions() {
  }

  public PgClientOptions(JsonObject json) {
    throw new UnsupportedOperationException("todo");
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

  public int getPoolsize() {
    return poolsize;
  }

  public PgClientOptions setPoolsize(int poolsize) {
    this.poolsize = poolsize;
    return this;
  }
}
