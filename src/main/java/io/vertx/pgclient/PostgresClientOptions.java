package io.vertx.pgclient;

import io.vertx.core.buffer.Buffer;
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
public class PostgresClientOptions extends NetClientOptions {

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

  @Override
  public PostgresClientOptions setSendBufferSize(int sendBufferSize) {
    return (PostgresClientOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public PostgresClientOptions setReceiveBufferSize(int receiveBufferSize) {
    return (PostgresClientOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public PostgresClientOptions setReuseAddress(boolean reuseAddress) {
    return (PostgresClientOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public PostgresClientOptions setTrafficClass(int trafficClass) {
    return (PostgresClientOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public PostgresClientOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (PostgresClientOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public PostgresClientOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (PostgresClientOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public PostgresClientOptions setSoLinger(int soLinger) {
    return (PostgresClientOptions) super.setSoLinger(soLinger);
  }

  @Override
  public PostgresClientOptions setUsePooledBuffers(boolean usePooledBuffers) {
    return (PostgresClientOptions) super.setUsePooledBuffers(usePooledBuffers);
  }

  @Override
  public PostgresClientOptions setIdleTimeout(int idleTimeout) {
    return (PostgresClientOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public PostgresClientOptions setSsl(boolean ssl) {
    return (PostgresClientOptions) super.setSsl(ssl);
  }

  @Override
  public PostgresClientOptions setKeyCertOptions(KeyCertOptions options) {
    return (PostgresClientOptions) super.setKeyCertOptions(options);
  }

  @Override
  public PostgresClientOptions setKeyStoreOptions(JksOptions options) {
    return (PostgresClientOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public PostgresClientOptions setPfxKeyCertOptions(PfxOptions options) {
    return (PostgresClientOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public PostgresClientOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (PostgresClientOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public PostgresClientOptions setTrustOptions(TrustOptions options) {
    return (PostgresClientOptions) super.setTrustOptions(options);
  }

  @Override
  public PostgresClientOptions setTrustStoreOptions(JksOptions options) {
    return (PostgresClientOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public PostgresClientOptions setPemTrustOptions(PemTrustOptions options) {
    return (PostgresClientOptions) super.setPemTrustOptions(options);
  }

  @Override
  public PostgresClientOptions setPfxTrustOptions(PfxOptions options) {
    return (PostgresClientOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public PostgresClientOptions addEnabledCipherSuite(String suite) {
    return (PostgresClientOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public PostgresClientOptions addEnabledSecureTransportProtocol(String protocol) {
    return (PostgresClientOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public PostgresClientOptions addCrlPath(String crlPath) throws NullPointerException {
    return (PostgresClientOptions) super.addCrlPath(crlPath);
  }

  @Override
  public PostgresClientOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (PostgresClientOptions) super.addCrlValue(crlValue);
  }

  @Override
  public PostgresClientOptions setTrustAll(boolean trustAll) {
    return (PostgresClientOptions) super.setTrustAll(trustAll);
  }

  @Override
  public PostgresClientOptions setConnectTimeout(int connectTimeout) {
    return (PostgresClientOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public PostgresClientOptions setMetricsName(String metricsName) {
    return (PostgresClientOptions) super.setMetricsName(metricsName);
  }

  @Override
  public PostgresClientOptions setReconnectAttempts(int attempts) {
    return (PostgresClientOptions) super.setReconnectAttempts(attempts);
  }

  @Override
  public PostgresClientOptions setReconnectInterval(long interval) {
    return (PostgresClientOptions) super.setReconnectInterval(interval);
  }

  @Override
  public PostgresClientOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (PostgresClientOptions) super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public PostgresClientOptions setLogActivity(boolean logEnabled) {
    return (PostgresClientOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public PostgresClientOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (PostgresClientOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public PostgresClientOptions setLocalAddress(String localAddress) {
    return (PostgresClientOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public PostgresClientOptions setUseAlpn(boolean useAlpn) {
    return (PostgresClientOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public PostgresClientOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (PostgresClientOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PostgresClientOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (PostgresClientOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public PostgresClientOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (PostgresClientOptions) super.setOpenSslEngineOptions(sslEngineOptions);
  }
}
