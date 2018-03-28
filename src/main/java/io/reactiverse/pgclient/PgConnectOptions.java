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

package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;

import java.util.Set;

import static java.lang.Integer.*;
import static java.lang.System.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PgConnectOptions extends NetClientOptions {

  public static final String DEFAULT_HOST = "localhost";
  public static int DEFAULT_PORT = 5432;
  public static final String DEFAULT_DATABASE = "db";
  public static final String DEFAULT_USERNAME = "user";
  public static final String DEFAULT_PASSWORD = "pass";
  public static final boolean DEFAULT_CACHE_PREPARED_STATEMENTS = false;
  public static final int DEFAULT_PIPELINING_LIMIT = 256;

  private String host;
  private int port;
  private String database;
  private String username;
  private String password;
  private boolean cachePreparedStatements;
  private int pipeliningLimit;

  public PgConnectOptions() {
    super();
    init();
  }

  public PgConnectOptions(JsonObject json) {
    super(json);
    init();
    PgConnectOptionsConverter.fromJson(json, this);
  }

  public PgConnectOptions(PgConnectOptions other) {
    super(other);
    host = other.host;
    port = other.port;
    database = other.database;
    username = other.username;
    password = other.password;
    pipeliningLimit = other.pipeliningLimit;
  }

  public String getHost() {
    return host;
  }

  public PgConnectOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public PgConnectOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getDatabase() {
    return database;
  }

  public PgConnectOptions setDatabase(String database) {
    this.database = database;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public PgConnectOptions setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public PgConnectOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  public int getPipeliningLimit() {
    return pipeliningLimit;
  }

  public PgConnectOptions setPipeliningLimit(int pipeliningLimit) {
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException();
    }
    this.pipeliningLimit = pipeliningLimit;
    return this;
  }

  public boolean getCachePreparedStatements() {
    return cachePreparedStatements;
  }

  public PgConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    this.cachePreparedStatements = cachePreparedStatements;
    return this;
  }

  @Override
  public PgConnectOptions setSendBufferSize(int sendBufferSize) {
    return (PgConnectOptions)super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public PgConnectOptions setReceiveBufferSize(int receiveBufferSize) {
    return (PgConnectOptions)super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public PgConnectOptions setReuseAddress(boolean reuseAddress) {
    return (PgConnectOptions)super.setReuseAddress(reuseAddress);
  }

  @Override
  public PgConnectOptions setTrafficClass(int trafficClass) {
    return (PgConnectOptions)super.setTrafficClass(trafficClass);
  }

  @Override
  public PgConnectOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (PgConnectOptions)super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public PgConnectOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (PgConnectOptions)super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public PgConnectOptions setSoLinger(int soLinger) {
    return (PgConnectOptions)super.setSoLinger(soLinger);
  }

  @Override
  public PgConnectOptions setUsePooledBuffers(boolean usePooledBuffers) {
    return (PgConnectOptions)super.setUsePooledBuffers(usePooledBuffers);
  }

  @Override
  public PgConnectOptions setIdleTimeout(int idleTimeout) {
    return (PgConnectOptions)super.setIdleTimeout(idleTimeout);
  }

  @Override
  public PgConnectOptions setSsl(boolean ssl) {
    return (PgConnectOptions)super.setSsl(ssl);
  }

  @Override
  public PgConnectOptions setKeyCertOptions(KeyCertOptions options) {
    return (PgConnectOptions)super.setKeyCertOptions(options);
  }

  @Override
  public PgConnectOptions setKeyStoreOptions(JksOptions options) {
    return (PgConnectOptions)super.setKeyStoreOptions(options);
  }

  @Override
  public PgConnectOptions setPfxKeyCertOptions(PfxOptions options) {
    return (PgConnectOptions)super.setPfxKeyCertOptions(options);
  }

  @Override
  public PgConnectOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (PgConnectOptions)super.setPemKeyCertOptions(options);
  }

  @Override
  public PgConnectOptions setTrustOptions(TrustOptions options) {
    return (PgConnectOptions)super.setTrustOptions(options);
  }

  @Override
  public PgConnectOptions setTrustStoreOptions(JksOptions options) {
    return (PgConnectOptions)super.setTrustStoreOptions(options);
  }

  @Override
  public PgConnectOptions setPemTrustOptions(PemTrustOptions options) {
    return (PgConnectOptions)super.setPemTrustOptions(options);
  }

  @Override
  public PgConnectOptions setPfxTrustOptions(PfxOptions options) {
    return (PgConnectOptions)super.setPfxTrustOptions(options);
  }

  @Override
  public PgConnectOptions addEnabledCipherSuite(String suite) {
    return (PgConnectOptions)super.addEnabledCipherSuite(suite);
  }

  @Override
  public PgConnectOptions addEnabledSecureTransportProtocol(String protocol) {
    return (PgConnectOptions)super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public PgConnectOptions addCrlPath(String crlPath) throws NullPointerException {
    return (PgConnectOptions)super.addCrlPath(crlPath);
  }

  @Override
  public PgConnectOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (PgConnectOptions)super.addCrlValue(crlValue);
  }

  @Override
  public PgConnectOptions setTrustAll(boolean trustAll) {
    return (PgConnectOptions)super.setTrustAll(trustAll);
  }

  @Override
  public PgConnectOptions setConnectTimeout(int connectTimeout) {
    return (PgConnectOptions)super.setConnectTimeout(connectTimeout);
  }

  @Override
  public PgConnectOptions setMetricsName(String metricsName) {
    return (PgConnectOptions)super.setMetricsName(metricsName);
  }

  @Override
  public PgConnectOptions setReconnectAttempts(int attempts) {
    return (PgConnectOptions)super.setReconnectAttempts(attempts);
  }

  @Override
  public PgConnectOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (PgConnectOptions)super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public PgConnectOptions setLogActivity(boolean logEnabled) {
    return (PgConnectOptions)super.setLogActivity(logEnabled);
  }

  @Override
  public PgConnectOptions setReconnectInterval(long interval) {
    return (PgConnectOptions)super.setReconnectInterval(interval);
  }

  @Override
  public PgConnectOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (PgConnectOptions)super.setProxyOptions(proxyOptions);
  }

  @Override
  public PgConnectOptions setLocalAddress(String localAddress) {
    return (PgConnectOptions)super.setLocalAddress(localAddress);
  }

  @Override
  public PgConnectOptions setUseAlpn(boolean useAlpn) {
    return (PgConnectOptions)super.setUseAlpn(useAlpn);
  }

  @Override
  public PgConnectOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (PgConnectOptions)super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgConnectOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (PgConnectOptions)super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgConnectOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (PgConnectOptions)super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgConnectOptions setReusePort(boolean reusePort) {
    return (PgConnectOptions) super.setReusePort(reusePort);
  }

  @Override
  public PgConnectOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (PgConnectOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public PgConnectOptions setTcpCork(boolean tcpCork) {
    return (PgConnectOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public PgConnectOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (PgConnectOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public PgConnectOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (PgConnectOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  /**
   * Initialize with the default options, if env variables are specified they take precedence over these options.
   */
  private void init() {
    host = DEFAULT_HOST;
    port = DEFAULT_PORT;
    database = DEFAULT_DATABASE;
    username = DEFAULT_USERNAME;
    password = DEFAULT_PASSWORD;
    cachePreparedStatements = DEFAULT_CACHE_PREPARED_STATEMENTS;
    pipeliningLimit = DEFAULT_PIPELINING_LIMIT;

    if (getenv("PGHOSTADDR") == null) {
      if (getenv("PGHOST") != null) {
        host = getenv("PGHOST");
      }
    } else {
      host = getenv("PGHOSTADDR");
    }
    if (getenv("PGPORT") != null) {
      try {
        port = parseInt(getenv("PGPORT"));
      } catch (NumberFormatException e) {
        // port will be set to default
      }
    }
    if (getenv("PGDATABASE") != null) {
      database = getenv("PGDATABASE");
    }
    if (getenv("PGUSER") != null) {
      username = getenv("PGUSER");
    }
    if (getenv("PGPASSWORD") != null) {
      password = getenv("PGPASSWORD");
    }
  }
}
