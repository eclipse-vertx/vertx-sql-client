package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;

/**
 * Connect options for configuring {@link SqlConnection} or {@link Pool}.
 */
@DataObject(generateConverter = true)
public abstract class SqlConnectOptions extends NetClientOptions {
  public static final boolean DEFAULT_CACHE_PREPARED_STATEMENTS = false;
  public static final int DEFAULT_PREPARED_STATEMENT_CACHE_MAX_SIZE = 256;
  public static final int DEFAULT_PREPARED_STATEMENT_CACHE_SQL_LIMIT = 2048;

  private String host;
  private int port;
  private String user;
  private String password;
  private String database;
  private boolean cachePreparedStatements = DEFAULT_CACHE_PREPARED_STATEMENTS;
  private int preparedStatementCacheMaxSize = DEFAULT_PREPARED_STATEMENT_CACHE_MAX_SIZE;
  private int preparedStatementCacheSqlLimit = DEFAULT_PREPARED_STATEMENT_CACHE_SQL_LIMIT;
  private JsonObject properties;

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
    this.cachePreparedStatements = other.cachePreparedStatements;
    this.preparedStatementCacheMaxSize = other.preparedStatementCacheMaxSize;
    this.preparedStatementCacheSqlLimit = other.preparedStatementCacheSqlLimit;
    this.properties = other.properties.copy();
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

  /**
   * Get whether prepared statements cache is enabled.
   *
   * @return the value
   */
  public boolean getCachePreparedStatements() {
    return cachePreparedStatements;
  }

  /**
   * Set whether prepared statements cache should be enabled.
   *
   * @param cachePreparedStatements true if cache should be enabled
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    this.cachePreparedStatements = cachePreparedStatements;
    return this;
  }

  /**
   * Get the maximum number of prepared statements that the connection will cache.
   *
   * @return the size
   */
  public int getPreparedStatementCacheMaxSize() {
    return preparedStatementCacheMaxSize;
  }

  /**
   * Set the maximum number of prepared statements that the connection will cache.
   *
   * @param preparedStatementCacheMaxSize the size to set
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    this.preparedStatementCacheMaxSize = preparedStatementCacheMaxSize;
    return this;
  }

  /**
   * Get the maximum length of prepared statement SQL string that the connection will cache.
   *
   * @return the limit of maximum length
   */
  public int getPreparedStatementCacheSqlLimit() {
    return preparedStatementCacheSqlLimit;
  }

  /**
   * Set the maximum length of prepared statement SQL string that the connection will cache.
   *
   * @param preparedStatementCacheSqlLimit the maximum length limit of SQL string to set
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    this.preparedStatementCacheSqlLimit = preparedStatementCacheSqlLimit;
    return this;
  }

  /**
   * @return the value of current connection properties
   */
  public JsonObject getProperties() {
    return properties;
  }

  /**
   * Set properties for this client, which will be sent to server at the connection start.
   *
   * @param properties the value of properties to specify
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions setProperties(JsonObject properties) {
    this.properties = properties;
    return this;
  }

  /**
   * Add a property for this client, which will be sent to server at the connection start.
   *
   * @param key the value of property key
   * @param value the value of property value
   * @return a reference to this, so the API can be used fluently
   */
  public SqlConnectOptions addProperty(String key, String value) {
    this.properties.put(key, value);
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
