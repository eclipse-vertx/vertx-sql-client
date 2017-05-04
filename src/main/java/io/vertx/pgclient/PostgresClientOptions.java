package io.vertx.pgclient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgresClientOptions {

  private String host = "localhost";
  private int port = 5432;
  private String database = "db";
  private String username = "user";
  private String password = "pass";
  private int pipeliningLimit = 256;

  public String getHost() {
    return host;
  }

  public PostgresClientOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public PostgresClientOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getDatabase() {
    return database;
  }

  public PostgresClientOptions setDatabase(String database) {
    this.database = database;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public PostgresClientOptions setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public PostgresClientOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  public PostgresClientOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException();
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }
}
