package io.reactiverse.mysqlclient;

import io.vertx.core.net.NetClientOptions;

public class MySQLConnectOptions extends NetClientOptions {
  private String host;
  private int port;
  private String database;
  private String user;
  private String password;
  private String charset;

  public MySQLConnectOptions() {
    super();
    init();
  }

  public MySQLConnectOptions(MySQLConnectOptions other) {
    super(other);
    this.host = other.host;
    this.port = other.port;
    this.database = other.database;
    this.user = other.user;
    this.password = other.password;
    this.charset = other.charset;
  }

  private void init() {
    host = "localhost";
    port = 3306;
    user = "root";
    charset = "UTF-8";
  }

  public String getHost() {
    return host;
  }

  public MySQLConnectOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public MySQLConnectOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getDatabase() {
    return database;
  }

  public MySQLConnectOptions setDatabase(String database) {
    this.database = database;
    return this;
  }

  public String getUser() {
    return user;
  }

  public MySQLConnectOptions setUser(String user) {
    this.user = user;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public MySQLConnectOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  public String getCharset() {
    return charset;
  }

  public MySQLConnectOptions setCharset(String charset) {
    this.charset = charset;
    return this;
  }
}
