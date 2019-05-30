package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;

/**
 * Connect options for configuring {@link SqlConnection} or {@link Pool}.
 */
@DataObject(generateConverter = true)
public abstract class SqlConnectOptions extends NetClientOptions {
  private String host;
  private int port;
  private String user;
  private String password;
  private String database;

  public SqlConnectOptions() {
    super();
    init();
  }

  public SqlConnectOptions(JsonObject json) {
    super(json);
    init();
    SqlConnectOptionsConverter.fromJson(json, this);
  }

  public SqlConnectOptions(SqlConnectOptions other) {
    super(other);
    this.host = other.host;
    this.port = other.port;
    this.user = other.user;
    this.password = other.password;
    this.database = other.database;
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
  public SqlConnectOptions setHost(String host) {
    checkParameterNonNull(host, "Host can not be null");
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
  public SqlConnectOptions setPort(int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Port should range in 0-65535");
    }
    this.port = port;
    return this;
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
  public SqlConnectOptions setUser(String user) {
    checkParameterNonNull(host, "User account can not be null");
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
  public SqlConnectOptions setPassword(String password) {
    checkParameterNonNull(host, "Password can not be null");
    this.password = password;
    return this;
  }

  /**
   * Get the default database name for the connection.
   *
   * @return the database name
   */
  public String getDatabase() {
    return database;
  }

  /**
   * Specify the default database for the connection.
   *
   * @param database the database name to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setDatabase(String database) {
    checkParameterNonNull(host, "Database name can not be null");
    this.database = database;
    return this;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    SqlConnectOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * Initialize with the default options.
   */
  abstract protected void init();

  protected void checkParameterNonNull(Object parameter, String message) {
    if (parameter == null) {
      throw new IllegalArgumentException(message);
    }
  }
}
