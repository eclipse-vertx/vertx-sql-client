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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.SSLEngineOptions;
import io.vertx.core.net.TrustOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PgClientOptions extends NetClientOptions {

  public static final String DEFAULT_HOST = "localhost";
  public static int DEFAULT_PORT = 5432;
  public static final String DEFAULT_DATABASE = "db";
  public static final String DEFAULT_USERNAME = "user";
  public static final String DEFAULT_PASSWORD = "pass";
  public static final boolean DEFAULT_CACHE_PREPARED_STATEMENTS = false;
  public static final int DEFAULT_PIPELINING_LIMIT = 16;
  public static final int DEFAULT_WRITE_BATCH_SIZE = 16;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private String database = DEFAULT_DATABASE;
  private String username = DEFAULT_USERNAME;
  private String password = DEFAULT_PASSWORD;
  private boolean cachePreparedStatements = DEFAULT_CACHE_PREPARED_STATEMENTS;
  private int pipeliningLimit = DEFAULT_PIPELINING_LIMIT;
  private int writeBatchSize = DEFAULT_WRITE_BATCH_SIZE;

  public PgClientOptions() {
    super();
  }

  public PgClientOptions(JsonObject json) {
    super(json);
    PgClientOptionsConverter.fromJson(json, this);
  }

  public PgClientOptions(PgClientOptions other) {
    super(other);
    host = other.host;
    port = other.port;
    database = other.database;
    username = other.username;
    password = other.password;
    pipeliningLimit = other.pipeliningLimit;
    writeBatchSize = other.writeBatchSize;
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

  public int getWriteBatchSize() {
    return writeBatchSize;
  }

  public PgClientOptions setWriteBatchSize(int writeBatchSize) {
    if (writeBatchSize < 0) {
      throw new IllegalArgumentException();
    }
    this.writeBatchSize = writeBatchSize;
    return this;
  }

  @Override
  public PgClientOptions setSendBufferSize(int sendBufferSize) {
    return (PgClientOptions)super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public PgClientOptions setReceiveBufferSize(int receiveBufferSize) {
    return (PgClientOptions)super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public PgClientOptions setReuseAddress(boolean reuseAddress) {
    return (PgClientOptions)super.setReuseAddress(reuseAddress);
  }

  @Override
  public PgClientOptions setTrafficClass(int trafficClass) {
    return (PgClientOptions)super.setTrafficClass(trafficClass);
  }

  @Override
  public PgClientOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (PgClientOptions)super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public PgClientOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (PgClientOptions)super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public PgClientOptions setSoLinger(int soLinger) {
    return (PgClientOptions)super.setSoLinger(soLinger);
  }

  @Override
  public PgClientOptions setUsePooledBuffers(boolean usePooledBuffers) {
    return (PgClientOptions)super.setUsePooledBuffers(usePooledBuffers);
  }

  @Override
  public PgClientOptions setIdleTimeout(int idleTimeout) {
    return (PgClientOptions)super.setIdleTimeout(idleTimeout);
  }

  @Override
  public PgClientOptions setSsl(boolean ssl) {
    return (PgClientOptions)super.setSsl(ssl);
  }

  @Override
  public PgClientOptions setKeyCertOptions(KeyCertOptions options) {
    return (PgClientOptions)super.setKeyCertOptions(options);
  }

  @Override
  public PgClientOptions setKeyStoreOptions(JksOptions options) {
    return (PgClientOptions)super.setKeyStoreOptions(options);
  }

  @Override
  public PgClientOptions setPfxKeyCertOptions(PfxOptions options) {
    return (PgClientOptions)super.setPfxKeyCertOptions(options);
  }

  @Override
  public PgClientOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (PgClientOptions)super.setPemKeyCertOptions(options);
  }

  @Override
  public PgClientOptions setTrustOptions(TrustOptions options) {
    return (PgClientOptions)super.setTrustOptions(options);
  }

  @Override
  public PgClientOptions setTrustStoreOptions(JksOptions options) {
    return (PgClientOptions)super.setTrustStoreOptions(options);
  }

  @Override
  public PgClientOptions setPemTrustOptions(PemTrustOptions options) {
    return (PgClientOptions)super.setPemTrustOptions(options);
  }

  @Override
  public PgClientOptions setPfxTrustOptions(PfxOptions options) {
    return (PgClientOptions)super.setPfxTrustOptions(options);
  }

  @Override
  public PgClientOptions addEnabledCipherSuite(String suite) {
    return (PgClientOptions)super.addEnabledCipherSuite(suite);
  }

  @Override
  public PgClientOptions addEnabledSecureTransportProtocol(String protocol) {
    return (PgClientOptions)super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public PgClientOptions addCrlPath(String crlPath) throws NullPointerException {
    return (PgClientOptions)super.addCrlPath(crlPath);
  }

  @Override
  public PgClientOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (PgClientOptions)super.addCrlValue(crlValue);
  }

  @Override
  public PgClientOptions setTrustAll(boolean trustAll) {
    return (PgClientOptions)super.setTrustAll(trustAll);
  }

  @Override
  public PgClientOptions setConnectTimeout(int connectTimeout) {
    return (PgClientOptions)super.setConnectTimeout(connectTimeout);
  }

  @Override
  public PgClientOptions setMetricsName(String metricsName) {
    return (PgClientOptions)super.setMetricsName(metricsName);
  }

  @Override
  public PgClientOptions setReconnectAttempts(int attempts) {
    return (PgClientOptions)super.setReconnectAttempts(attempts);
  }

  @Override
  public PgClientOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (PgClientOptions)super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public PgClientOptions setLogActivity(boolean logEnabled) {
    return (PgClientOptions)super.setLogActivity(logEnabled);
  }

  @Override
  public PgClientOptions setReconnectInterval(long interval) {
    return (PgClientOptions)super.setReconnectInterval(interval);
  }

  @Override
  public PgClientOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (PgClientOptions)super.setProxyOptions(proxyOptions);
  }

  @Override
  public PgClientOptions setLocalAddress(String localAddress) {
    return (PgClientOptions)super.setLocalAddress(localAddress);
  }

  @Override
  public PgClientOptions setUseAlpn(boolean useAlpn) {
    return (PgClientOptions)super.setUseAlpn(useAlpn);
  }

  @Override
  public PgClientOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (PgClientOptions)super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgClientOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (PgClientOptions)super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgClientOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (PgClientOptions)super.setOpenSslEngineOptions(sslEngineOptions);
  }
}
