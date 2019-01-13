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

/**
 * The options for configuring a connection pool.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class PgPoolOptions extends PgConnectOptions {

  /**
   * Provide a {@link PgPoolOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link PgPoolOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static PgPoolOptions fromUri(String connectionUri) throws IllegalArgumentException {
    return new PgPoolOptions(PgConnectOptions.fromUri(connectionUri));
  }

  /**
   * Provide a {@link PgPoolOptions} configured with environment variables, if the environment variable
   * is not set, then a default value will take precedence over this.
   */
  public static PgPoolOptions fromEnv() {
    return new PgPoolOptions(PgConnectOptions.fromEnv());
  }

  /**
   * The default maximum number of connections a client will pool = 4
   */
  public static final int DEFAULT_MAX_SIZE = 4;

  /**
   * Default max wait queue size = -1 (unbounded)
   */
  public static final int DEFAULT_MAX_WAIT_QUEUE_SIZE = -1;

  private int maxSize = DEFAULT_MAX_SIZE;
  private int maxWaitQueueSize = DEFAULT_MAX_WAIT_QUEUE_SIZE;

  public PgPoolOptions() {
  }

  public PgPoolOptions(JsonObject json) {
    super(json);
    PgPoolOptionsConverter.fromJson(json, this);
  }

  public PgPoolOptions(PgPoolOptions other) {
    super(other);
    maxSize = other.maxSize;
    maxWaitQueueSize = other.maxWaitQueueSize;
  }

  public PgPoolOptions(PgConnectOptions other) {
    super(other);
  }

  /**
   * @return  the maximum pool size
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Set the maximum pool size
   *
   * @param maxSize  the maximum pool size
   * @return a reference to this, so the API can be used fluently
   */
  public PgPoolOptions setMaxSize(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size cannot be negative");
    }
    this.maxSize = maxSize;
    return this;
  }

  /**
   * @return the maximum wait queue size
   */
  public int getMaxWaitQueueSize() {
    return maxWaitQueueSize;
  }

  /**
   * Set the maximum connection request allowed in the wait queue, any requests beyond the max size will result in
   * an failure.  If the value is set to a negative number then the queue will be unbounded.
   *
   * @param maxWaitQueueSize the maximum number of waiting requests
   * @return a reference to this, so the API can be used fluently
   */
  public PgPoolOptions setMaxWaitQueueSize(int maxWaitQueueSize) {
    this.maxWaitQueueSize = maxWaitQueueSize;
    return this;
  }

  @Override
  public PgPoolOptions setHost(String host) {
    return (PgPoolOptions) super.setHost(host);
  }

  @Override
  public PgPoolOptions setPort(int port) {
    return (PgPoolOptions) super.setPort(port);
  }

  @Override
  public PgPoolOptions setDatabase(String database) {
    return (PgPoolOptions) super.setDatabase(database);
  }

  @Override
  public PgPoolOptions setUser(String user) {
    return (PgPoolOptions) super.setUser(user);
  }

  @Override
  public PgPoolOptions setPassword(String password) {
    return (PgPoolOptions) super.setPassword(password);
  }

  @Override
  public PgPoolOptions setPipeliningLimit(int pipeliningLimit) {
    return (PgPoolOptions) super.setPipeliningLimit(pipeliningLimit);
  }

  @Override
  public PgPoolOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (PgPoolOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public PgPoolOptions setSslMode(SslMode sslmode) {
    return (PgPoolOptions) super.setSslMode(sslmode);
  }

  @Override
  public PgPoolOptions setSendBufferSize(int sendBufferSize) {
    return (PgPoolOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public PgPoolOptions setReceiveBufferSize(int receiveBufferSize) {
    return (PgPoolOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public PgPoolOptions setReuseAddress(boolean reuseAddress) {
    return (PgPoolOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public PgPoolOptions setTrafficClass(int trafficClass) {
    return (PgPoolOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public PgPoolOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (PgPoolOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public PgPoolOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (PgPoolOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public PgPoolOptions setSoLinger(int soLinger) {
    return (PgPoolOptions) super.setSoLinger(soLinger);
  }

  @Override
  public PgPoolOptions setUsePooledBuffers(boolean usePooledBuffers) {
    return (PgPoolOptions) super.setUsePooledBuffers(usePooledBuffers);
  }

  @Override
  public PgPoolOptions setIdleTimeout(int idleTimeout) {
    return (PgPoolOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public PgPoolOptions setSsl(boolean ssl) {
    return (PgPoolOptions) super.setSsl(ssl);
  }

  @Override
  public PgPoolOptions setKeyCertOptions(KeyCertOptions options) {
    return (PgPoolOptions) super.setKeyCertOptions(options);
  }

  @Override
  public PgPoolOptions setKeyStoreOptions(JksOptions options) {
    return (PgPoolOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public PgPoolOptions setPfxKeyCertOptions(PfxOptions options) {
    return (PgPoolOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public PgPoolOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (PgPoolOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public PgPoolOptions setTrustOptions(TrustOptions options) {
    return (PgPoolOptions) super.setTrustOptions(options);
  }

  @Override
  public PgPoolOptions setTrustStoreOptions(JksOptions options) {
    return (PgPoolOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public PgPoolOptions setPemTrustOptions(PemTrustOptions options) {
    return (PgPoolOptions) super.setPemTrustOptions(options);
  }

  @Override
  public PgPoolOptions setPfxTrustOptions(PfxOptions options) {
    return (PgPoolOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public PgPoolOptions addEnabledCipherSuite(String suite) {
    return (PgPoolOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public PgPoolOptions addEnabledSecureTransportProtocol(String protocol) {
    return (PgPoolOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public PgPoolOptions addCrlPath(String crlPath) throws NullPointerException {
    return (PgPoolOptions) super.addCrlPath(crlPath);
  }

  @Override
  public PgPoolOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (PgPoolOptions) super.addCrlValue(crlValue);
  }

  @Override
  public PgPoolOptions setTrustAll(boolean trustAll) {
    return (PgPoolOptions) super.setTrustAll(trustAll);
  }

  @Override
  public PgPoolOptions setConnectTimeout(int connectTimeout) {
    return (PgPoolOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public PgPoolOptions setMetricsName(String metricsName) {
    return (PgPoolOptions) super.setMetricsName(metricsName);
  }

  @Override
  public PgPoolOptions setReconnectAttempts(int attempts) {
    return (PgPoolOptions) super.setReconnectAttempts(attempts);
  }

  @Override
  public PgPoolOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (PgPoolOptions) super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public PgPoolOptions setLogActivity(boolean logEnabled) {
    return (PgPoolOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public PgPoolOptions setReconnectInterval(long interval) {
    return (PgPoolOptions) super.setReconnectInterval(interval);
  }

  @Override
  public PgPoolOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (PgPoolOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public PgPoolOptions setLocalAddress(String localAddress) {
    return (PgPoolOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public PgPoolOptions setUseAlpn(boolean useAlpn) {
    return (PgPoolOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public PgPoolOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (PgPoolOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgPoolOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (PgPoolOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgPoolOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (PgPoolOptions) super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PgPoolOptions setReusePort(boolean reusePort) {
    return (PgPoolOptions) super.setReusePort(reusePort);
  }

  @Override
  public PgPoolOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (PgPoolOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public PgPoolOptions setTcpCork(boolean tcpCork) {
    return (PgPoolOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public PgPoolOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (PgPoolOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    PgPoolOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PgPoolOptions)) return false;
    if (!super.equals(o)) return false;

    PgPoolOptions that = (PgPoolOptions) o;

    if (maxSize != that.maxSize) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + maxSize;
    return result;
  }
}
