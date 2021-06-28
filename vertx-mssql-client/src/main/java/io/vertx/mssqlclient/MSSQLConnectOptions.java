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

package io.vertx.mssqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.SSLEngineOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.mssqlclient.impl.MSSQLConnectionUriParser;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Connect options for configuring {@link MSSQLConnection}.
 */
@DataObject(generateConverter = true)
public class MSSQLConnectOptions extends SqlConnectOptions {

  /**
   * @return the {@code options} as MSSQL specific connect options
   */
  public static MSSQLConnectOptions wrap(SqlConnectOptions options) {
    if (options instanceof MSSQLConnectOptions) {
      return (MSSQLConnectOptions) options;
    } else {
      return new MSSQLConnectOptions(options);
    }
  }

  /**
   * Provide a {@link MSSQLConnectOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link MSSQLConnectOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static MSSQLConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
    JsonObject parsedConfiguration = MSSQLConnectionUriParser.parse(connectionUri);
    return new MSSQLConnectOptions(parsedConfiguration);
  }

  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 1433;
  public static final String DEFAULT_USER = "sa";
  public static final String DEFAULT_PASSWORD = "";
  public static final String DEFAULT_DATABASE = "";
  public static final String DEFAULT_APP_NAME = "vertx-mssql-client";
  public static final String DEFAULT_CLIENT_INTERFACE_NAME = "Vert.x";
  public static final Map<String, String> DEFAULT_PROPERTIES;

  static {
    Map<String, String> defaultProperties = new HashMap<>();
    defaultProperties.put("appName", DEFAULT_APP_NAME);
    defaultProperties.put("clientInterfaceName", DEFAULT_CLIENT_INTERFACE_NAME);
    DEFAULT_PROPERTIES = defaultProperties;
  }

  public MSSQLConnectOptions() {
    super();
  }

  public MSSQLConnectOptions(JsonObject json) {
    super(json);
    MSSQLConnectOptionsConverter.fromJson(json, this);
  }

  public MSSQLConnectOptions(SqlConnectOptions other) {
    super(other);
  }

  public MSSQLConnectOptions(MSSQLConnectOptions other) {
    super(other);
  }

  @Override
  public MSSQLConnectOptions setHost(String host) {
    return (MSSQLConnectOptions) super.setHost(host);
  }

  @Override
  public MSSQLConnectOptions setPort(int port) {
    return (MSSQLConnectOptions) super.setPort(port);
  }

  @Override
  public MSSQLConnectOptions setUser(String user) {
    return (MSSQLConnectOptions) super.setUser(user);
  }

  @Override
  public MSSQLConnectOptions setPassword(String password) {
    return (MSSQLConnectOptions) super.setPassword(password);
  }

  @Override
  public MSSQLConnectOptions setDatabase(String database) {
    return (MSSQLConnectOptions) super.setDatabase(database);
  }

  @Override
  public MSSQLConnectOptions setProperties(Map<String, String> properties) {
    return (MSSQLConnectOptions) super.setProperties(properties);
  }

  @Override
  public MSSQLConnectOptions addProperty(String key, String value) {
    return (MSSQLConnectOptions) super.addProperty(key, value);
  }

  @Override
  public MSSQLConnectOptions setSendBufferSize(int sendBufferSize) {
    return (MSSQLConnectOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public MSSQLConnectOptions setReceiveBufferSize(int receiveBufferSize) {
    return (MSSQLConnectOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public MSSQLConnectOptions setReuseAddress(boolean reuseAddress) {
    return (MSSQLConnectOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public MSSQLConnectOptions setReusePort(boolean reusePort) {
    return (MSSQLConnectOptions) super.setReusePort(reusePort);
  }

  @Override
  public MSSQLConnectOptions setTrafficClass(int trafficClass) {
    return (MSSQLConnectOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public MSSQLConnectOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (MSSQLConnectOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public MSSQLConnectOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (MSSQLConnectOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public MSSQLConnectOptions setSoLinger(int soLinger) {
    return (MSSQLConnectOptions) super.setSoLinger(soLinger);
  }

  @Override
  public MSSQLConnectOptions setIdleTimeout(int idleTimeout) {
    return (MSSQLConnectOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public MSSQLConnectOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    return (MSSQLConnectOptions) super.setIdleTimeoutUnit(idleTimeoutUnit);
  }

  @Override
  public MSSQLConnectOptions setKeyCertOptions(KeyCertOptions options) {
    return (MSSQLConnectOptions) super.setKeyCertOptions(options);
  }

  @Override
  public MSSQLConnectOptions setKeyStoreOptions(JksOptions options) {
    return (MSSQLConnectOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public MSSQLConnectOptions setPfxKeyCertOptions(PfxOptions options) {
    return (MSSQLConnectOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public MSSQLConnectOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (MSSQLConnectOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public MSSQLConnectOptions setTrustOptions(TrustOptions options) {
    return (MSSQLConnectOptions) super.setTrustOptions(options);
  }

  @Override
  public MSSQLConnectOptions setTrustStoreOptions(JksOptions options) {
    return (MSSQLConnectOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public MSSQLConnectOptions setPemTrustOptions(PemTrustOptions options) {
    return (MSSQLConnectOptions) super.setPemTrustOptions(options);
  }

  @Override
  public MSSQLConnectOptions setPfxTrustOptions(PfxOptions options) {
    return (MSSQLConnectOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public MSSQLConnectOptions addEnabledCipherSuite(String suite) {
    return (MSSQLConnectOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public MSSQLConnectOptions addEnabledSecureTransportProtocol(String protocol) {
    return (MSSQLConnectOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public MSSQLConnectOptions removeEnabledSecureTransportProtocol(String protocol) {
    return (MSSQLConnectOptions) super.removeEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public MSSQLConnectOptions setUseAlpn(boolean useAlpn) {
    return (MSSQLConnectOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public MSSQLConnectOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (MSSQLConnectOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public MSSQLConnectOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (MSSQLConnectOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public MSSQLConnectOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (MSSQLConnectOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public MSSQLConnectOptions setTcpCork(boolean tcpCork) {
    return (MSSQLConnectOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public MSSQLConnectOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (MSSQLConnectOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public MSSQLConnectOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (MSSQLConnectOptions) super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public MSSQLConnectOptions addCrlPath(String crlPath) throws NullPointerException {
    return (MSSQLConnectOptions) super.addCrlPath(crlPath);
  }

  @Override
  public MSSQLConnectOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (MSSQLConnectOptions) super.addCrlValue(crlValue);
  }

  @Override
  public MSSQLConnectOptions setTrustAll(boolean trustAll) {
    return (MSSQLConnectOptions) super.setTrustAll(trustAll);
  }

  @Override
  public MSSQLConnectOptions setConnectTimeout(int connectTimeout) {
    return (MSSQLConnectOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public MSSQLConnectOptions setMetricsName(String metricsName) {
    return (MSSQLConnectOptions) super.setMetricsName(metricsName);
  }

  @Override
  public MSSQLConnectOptions setReconnectAttempts(int attempts) {
    return (MSSQLConnectOptions) super.setReconnectAttempts(attempts);
  }

  @Override
  public MSSQLConnectOptions setReconnectInterval(long interval) {
    return (MSSQLConnectOptions) super.setReconnectInterval(interval);
  }

  @Override
  public MSSQLConnectOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (MSSQLConnectOptions) super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public MSSQLConnectOptions setLogActivity(boolean logEnabled) {
    return (MSSQLConnectOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public MSSQLConnectOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (MSSQLConnectOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public MSSQLConnectOptions setLocalAddress(String localAddress) {
    return (MSSQLConnectOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public MSSQLConnectOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (MSSQLConnectOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  @Override
  public MSSQLConnectOptions setSslHandshakeTimeout(long sslHandshakeTimeout) {
    return (MSSQLConnectOptions) super.setSslHandshakeTimeout(sslHandshakeTimeout);
  }

  @Override
  public MSSQLConnectOptions setSslHandshakeTimeoutUnit(TimeUnit sslHandshakeTimeoutUnit) {
    return (MSSQLConnectOptions) super.setSslHandshakeTimeoutUnit(sslHandshakeTimeoutUnit);
  }

  @Override
  public MSSQLConnectOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    return (MSSQLConnectOptions) super.setTracingPolicy(tracingPolicy);
  }

  /**
   * Initialize with the default options.
   */
  protected void init() {
    this.setHost(DEFAULT_HOST);
    this.setPort(DEFAULT_PORT);
    this.setUser(DEFAULT_USER);
    this.setPassword(DEFAULT_PASSWORD);
    this.setDatabase(DEFAULT_DATABASE);
    this.setProperties(new HashMap<>(DEFAULT_PROPERTIES));
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MSSQLConnectOptionsConverter.toJson(this, json);
    return json;
  }
}
