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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.MySQLConnectionUriParser;
import io.vertx.sqlclient.SqlConnectOptions;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Connect options for configuring {@link MySQLConnection} or {@link MySQLBuilder}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class MySQLConnectOptions extends SqlConnectOptions {

  /**
   * @return the {@code options} as MySQL specific connect options
   */
  public static MySQLConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof MySQLConnectOptions) {
      return (MySQLConnectOptions) options;
    } else {
      return new MySQLConnectOptions(options);
    }
  }

  /**
   * Provide a {@link MySQLConnectOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link MySQLConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static MySQLConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = MySQLConnectionUriParser.parse(connectionUri);
    return new MySQLConnectOptions(parsedConfiguration);
  }

  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 3306;
  public static final String DEFAULT_USER = "root";
  public static final String DEFAULT_PASSWORD = "";
  public static final String DEFAULT_SCHEMA = "";
  public static final String DEFAULT_CHARSET = "utf8mb4";
  public static final boolean DEFAULT_USE_AFFECTED_ROWS = false;
  public static final Map<String, String> DEFAULT_CONNECTION_ATTRIBUTES;
  public static final SslMode DEFAULT_SSL_MODE = SslMode.DISABLED;
  public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
  public static final int DEFAULT_PIPELINING_LIMIT = 1;

  static {
    Map<String, String> defaultAttributes = new HashMap<>();
    defaultAttributes.put("_client_name", "vertx-mysql-client");
    DEFAULT_CONNECTION_ATTRIBUTES = Collections.unmodifiableMap(defaultAttributes);
  }

  private String collation;
  private String charset = DEFAULT_CHARSET;
  private Boolean useAffectedRows = DEFAULT_USE_AFFECTED_ROWS;
  private SslMode sslMode = DEFAULT_SSL_MODE;
  private String serverRsaPublicKeyPath;
  private Buffer serverRsaPublicKeyValue;
  private String characterEncoding = DEFAULT_CHARACTER_ENCODING;
  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;
  private MySQLAuthenticationPlugin authenticationPlugin = MySQLAuthenticationPlugin.DEFAULT;

  public MySQLConnectOptions() {
    super();
  }

  public MySQLConnectOptions(JsonObject json) {
    super(json);
    MySQLConnectOptionsConverter.fromJson(json, this);
  }

  public MySQLConnectOptions(SqlConnectOptions other) {
    super(other);
    if (other instanceof MySQLConnectOptions) {
      MySQLConnectOptions opts = (MySQLConnectOptions) other;
      this.collation = opts.collation;
      this.charset = opts.charset;
      this.useAffectedRows = opts.useAffectedRows;
      this.sslMode = opts.sslMode;
      this.serverRsaPublicKeyPath = opts.serverRsaPublicKeyPath;
      this.serverRsaPublicKeyValue = opts.serverRsaPublicKeyValue != null ? opts.serverRsaPublicKeyValue.copy() : null;
      this.characterEncoding = opts.characterEncoding;
      this.pipeliningLimit = opts.pipeliningLimit;
      this.authenticationPlugin = opts.authenticationPlugin;
    }
  }

  public MySQLConnectOptions(MySQLConnectOptions other) {
    super(other);
    this.collation = other.collation;
    this.charset = other.charset;
    this.useAffectedRows = other.useAffectedRows;
    this.sslMode = other.sslMode;
    this.serverRsaPublicKeyPath = other.serverRsaPublicKeyPath;
    this.serverRsaPublicKeyValue = other.serverRsaPublicKeyValue != null ? other.serverRsaPublicKeyValue.copy() : null;
    this.characterEncoding = other.characterEncoding;
    this.pipeliningLimit = other.pipeliningLimit;
    this.authenticationPlugin = other.authenticationPlugin;
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
  public MySQLConnectOptions setCollation(String collation) {
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
  public MySQLConnectOptions setCharset(String charset) {
    if (charset != null && !MySQLCollation.SUPPORTED_CHARSET_NAMES.contains(charset)) {
      throw new IllegalArgumentException("Unsupported charset: " + charset);
    }
    this.charset = charset;
    return this;
  }

  /**
   * Get the Java charset for encoding string values.
   *
   * @return the charset name
   */
  public String getCharacterEncoding() {
    return characterEncoding;
  }

  /**
   * Set the Java charset for encoding string values, this value is UTF-8 by default.
   *
   * @param characterEncoding the Java charset to configure
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setCharacterEncoding(String characterEncoding) {
    if (characterEncoding != null && !Charset.isSupported(characterEncoding)) {
      throw new IllegalArgumentException("Unsupported Java charset: " + characterEncoding);
    } else {
      this.characterEncoding = characterEncoding;
    }
    return this;
  }

  /**
   * Get how affected rows are calculated on update/delete/insert.
   *
   * @return how affected rows are calculated on update/delete/insert.
   */
  public boolean isUseAffectedRows() {
    return useAffectedRows;
  }

  /**
   * Sets how affected rows are calculated on update/delete/insert, if set to <code>true</code> an update that effectively
   * does not change any data returns zero affected rows.
   *
   * See <a href="https://dev.mysql.com/doc/refman/8.0/en/mysql-affected-rows.html">mysql-affected-rows</a> for details.
   *
   * @param useAffectedRows whether only affected rows are count
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setUseAffectedRows(boolean useAffectedRows) {
    this.useAffectedRows = useAffectedRows;
    return this;
  }

  /**
   * Get the value of the configured SSL mode.
   *
   * @return the sslmode
   */
  public SslMode getSslMode() {
    return sslMode;
  }

  /**
   * Set the {@link SslMode} for the client, this option can be used to specify the desired security state of the connection to the server.
   *
   * @param sslMode the ssl-mode to specify
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setSslMode(SslMode sslMode) {
    this.sslMode = sslMode;
    return this;
  }

  /**
   * Get the default authentication plugin for connecting the server.
   *
   * @return the authentication plugin
   */
  public MySQLAuthenticationPlugin getAuthenticationPlugin() {
    return authenticationPlugin;
  }

  /**
   * Set the default {@link MySQLAuthenticationPlugin authentication plguin} for the client, the option will take effect at the connection start.
   *
   * @param authenticationPlugin the auth plugin to use
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setAuthenticationPlugin(MySQLAuthenticationPlugin authenticationPlugin) {
    Objects.requireNonNull(authenticationPlugin, "Authentication plugin can not be null");
    this.authenticationPlugin = authenticationPlugin;
    return this;
  }

  @Override
  public MySQLConnectOptions setSsl(boolean ssl) {
    if (ssl) {
      setSslMode(SslMode.VERIFY_CA);
    } else {
      setSslMode(SslMode.DISABLED);
    }
    return this;
  }

  /**
   * Set the path of server RSA public key which is mostly used for encrypting password under insecure connections when performing authentication.
   *
   * @param serverRsaPublicKeyPath the path of the server RSA public key
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setServerRsaPublicKeyPath(String serverRsaPublicKeyPath) {
    this.serverRsaPublicKeyPath = serverRsaPublicKeyPath;
    return this;
  }

  /**
   * Get the path of the server RSA public key.
   *
   * @return the public key path
   */
  public String getServerRsaPublicKeyPath() {
    return serverRsaPublicKeyPath;
  }

  /**
   * Set the value of server RSA public key which is mostly used for encrypting password under insecure connections when performing authentication.
   *
   * @param serverRsaPublicKeyValue the value of the server RSA public key
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setServerRsaPublicKeyValue(Buffer serverRsaPublicKeyValue) {
    this.serverRsaPublicKeyValue = serverRsaPublicKeyValue;
    return this;
  }

  /**
   * Get the value of the server RSA public key.
   *
   * @return the public key value
   */
  public Buffer getServerRsaPublicKeyValue() {
    return serverRsaPublicKeyValue;
  }

  /**
   * Get the pipelining limit count.
   *
   * @return the pipelining count
   */
  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  /**
   * Set the pipelining limit count.
   *
   * @param pipeliningLimit the count to configure
   * @return a reference to this, so the API can be used fluently
   */
  public MySQLConnectOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException("pipelining limit can not be less than 1");
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }

  @Override
  public MySQLConnectOptions setHost(String host) {
    return (MySQLConnectOptions) super.setHost(host);
  }

  @Override
  public MySQLConnectOptions setPort(int port) {
    return (MySQLConnectOptions) super.setPort(port);
  }

  @Override
  public MySQLConnectOptions setUser(String user) {
    return (MySQLConnectOptions) super.setUser(user);
  }

  @Override
  public MySQLConnectOptions setPassword(String password) {
    return (MySQLConnectOptions) super.setPassword(password);
  }

  @Override
  public MySQLConnectOptions setDatabase(String database) {
    return (MySQLConnectOptions) super.setDatabase(database);
  }

  @Override
  public MySQLConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (MySQLConnectOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public MySQLConnectOptions setPreparedStatementCacheMaxSize(int preparedStatementCacheMaxSize) {
    return (MySQLConnectOptions) super.setPreparedStatementCacheMaxSize(preparedStatementCacheMaxSize);
  }

  @GenIgnore
  @Override
  public MySQLConnectOptions setPreparedStatementCacheSqlFilter(Predicate<String> predicate) {
    return (MySQLConnectOptions) super.setPreparedStatementCacheSqlFilter(predicate);
  }

  @Override
  public MySQLConnectOptions setPreparedStatementCacheSqlLimit(int preparedStatementCacheSqlLimit) {
    return (MySQLConnectOptions) super.setPreparedStatementCacheSqlLimit(preparedStatementCacheSqlLimit);
  }

  @Override
  public MySQLConnectOptions setProperties(Map<String, String> properties) {
    return (MySQLConnectOptions) super.setProperties(properties);
  }

  @GenIgnore
  @Override
  public MySQLConnectOptions addProperty(String key, String value) {
    return (MySQLConnectOptions) super.addProperty(key, value);
  }

  @Override
  public MySQLConnectOptions setSendBufferSize(int sendBufferSize) {
    return (MySQLConnectOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public MySQLConnectOptions setReceiveBufferSize(int receiveBufferSize) {
    return (MySQLConnectOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public MySQLConnectOptions setReuseAddress(boolean reuseAddress) {
    return (MySQLConnectOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public MySQLConnectOptions setReusePort(boolean reusePort) {
    return (MySQLConnectOptions) super.setReusePort(reusePort);
  }

  @Override
  public MySQLConnectOptions setTrafficClass(int trafficClass) {
    return (MySQLConnectOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public MySQLConnectOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (MySQLConnectOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public MySQLConnectOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (MySQLConnectOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public MySQLConnectOptions setSoLinger(int soLinger) {
    return (MySQLConnectOptions) super.setSoLinger(soLinger);
  }

  @Override
  public MySQLConnectOptions setIdleTimeout(int idleTimeout) {
    return (MySQLConnectOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public MySQLConnectOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    return (MySQLConnectOptions) super.setIdleTimeoutUnit(idleTimeoutUnit);
  }

  @Override
  public MySQLConnectOptions setKeyCertOptions(KeyCertOptions options) {
    return (MySQLConnectOptions) super.setKeyCertOptions(options);
  }

  @Override
  public MySQLConnectOptions setKeyStoreOptions(JksOptions options) {
    return (MySQLConnectOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public MySQLConnectOptions setPfxKeyCertOptions(PfxOptions options) {
    return (MySQLConnectOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public MySQLConnectOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (MySQLConnectOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public MySQLConnectOptions setTrustOptions(TrustOptions options) {
    return (MySQLConnectOptions) super.setTrustOptions(options);
  }

  @Override
  public MySQLConnectOptions setTrustStoreOptions(JksOptions options) {
    return (MySQLConnectOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public MySQLConnectOptions setPemTrustOptions(PemTrustOptions options) {
    return (MySQLConnectOptions) super.setPemTrustOptions(options);
  }

  @Override
  public MySQLConnectOptions setPfxTrustOptions(PfxOptions options) {
    return (MySQLConnectOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public MySQLConnectOptions addEnabledCipherSuite(String suite) {
    return (MySQLConnectOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public MySQLConnectOptions addEnabledSecureTransportProtocol(String protocol) {
    return (MySQLConnectOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public MySQLConnectOptions removeEnabledSecureTransportProtocol(String protocol) {
    return (MySQLConnectOptions) super.removeEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public MySQLConnectOptions setUseAlpn(boolean useAlpn) {
    return (MySQLConnectOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public MySQLConnectOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (MySQLConnectOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public MySQLConnectOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (MySQLConnectOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public MySQLConnectOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (MySQLConnectOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public MySQLConnectOptions setTcpCork(boolean tcpCork) {
    return (MySQLConnectOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public MySQLConnectOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (MySQLConnectOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public ClientOptionsBase setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public MySQLConnectOptions addCrlPath(String crlPath) throws NullPointerException {
    return (MySQLConnectOptions) super.addCrlPath(crlPath);
  }

  @Override
  public MySQLConnectOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (MySQLConnectOptions) super.addCrlValue(crlValue);
  }

  @Override
  public MySQLConnectOptions setTrustAll(boolean trustAll) {
    return (MySQLConnectOptions) super.setTrustAll(trustAll);
  }

  @Override
  public MySQLConnectOptions setConnectTimeout(int connectTimeout) {
    return (MySQLConnectOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public MySQLConnectOptions setMetricsName(String metricsName) {
    return (MySQLConnectOptions) super.setMetricsName(metricsName);
  }

  @Override
  public MySQLConnectOptions setReconnectAttempts(int attempts) {
    return (MySQLConnectOptions) super.setReconnectAttempts(attempts);
  }

  @Override
  public MySQLConnectOptions setReconnectInterval(long interval) {
    return (MySQLConnectOptions) super.setReconnectInterval(interval);
  }

  @Override
  public MySQLConnectOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (MySQLConnectOptions) super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public MySQLConnectOptions setLogActivity(boolean logEnabled) {
    return (MySQLConnectOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public MySQLConnectOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (MySQLConnectOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public MySQLConnectOptions setLocalAddress(String localAddress) {
    return (MySQLConnectOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public MySQLConnectOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (MySQLConnectOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  @Override
  public MySQLConnectOptions setSslHandshakeTimeout(long sslHandshakeTimeout) {
    return (MySQLConnectOptions) super.setSslHandshakeTimeout(sslHandshakeTimeout);
  }

  @Override
  public MySQLConnectOptions setSslHandshakeTimeoutUnit(TimeUnit sslHandshakeTimeoutUnit) {
    return (MySQLConnectOptions) super.setSslHandshakeTimeoutUnit(sslHandshakeTimeoutUnit);
  }

  @Override
  public MySQLConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    return (MySQLConnectOptions) super.setTracingPolicy(tracingPolicy);
  }

  /**
   * Initialize with the default options.
   */
  @Override
  protected void init() {
    super.init();
    this.setHost(DEFAULT_HOST);
    this.setPort(DEFAULT_PORT);
    this.setUser(DEFAULT_USER);
    this.setPassword(DEFAULT_PASSWORD);
    this.setDatabase(DEFAULT_SCHEMA);
    this.setProperties(new HashMap<>(DEFAULT_CONNECTION_ATTRIBUTES));
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MySQLConnectOptionsConverter.toJson(this, json);
    return json;
  }

  @GenIgnore
  @Override
  public SocketAddress getSocketAddress() {
    return isUsingDomainSocket() ? SocketAddress.domainSocketAddress(getHost()) : super.getSocketAddress();
  }

  @GenIgnore
  public boolean isUsingDomainSocket() {
    return this.getHost().startsWith("/");
  }

  @Override
  public MySQLConnectOptions merge(JsonObject other) {
    JsonObject json = toJson();
    json.mergeIn(other);
    return new MySQLConnectOptions(json);
  }
}
