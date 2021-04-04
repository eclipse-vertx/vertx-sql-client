package io.vertx.clickhousenativeclient;

import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.utility.DockerImageName;

public class ClickhouseResource extends ExternalResource {
  private static final String connectionUri = System.getProperty("connection.uri");
  private static final String tlsConnectionUri = System.getProperty("tls.connection.uri");

  private ClickHouseContainer server;
  private ClickhouseNativeConnectOptions options;

  @Override
  protected void before() throws Throwable {
    if (isTestingWithExternalDatabase()) {
      this.options = ClickhouseNativeConnectOptions.fromUri(connectionUri);
      return;
    }
    if (this.server != null) {
      return;
    }
    DockerImageName imageName = DockerImageName.parse("yandex/clickhouse-server").withTag("20.10.2");
    server = new ClickHouseContainer(imageName);
    server.start();
    this.options = (ClickhouseNativeConnectOptions) new ClickhouseNativeConnectOptions()
      .setPort(server.getMappedPort(ClickHouseContainer.NATIVE_PORT))
      .setHost(server.getContainerIpAddress())
      .setUser(server.getUsername())
      .setPassword(server.getPassword())
      .setDatabase("default")
      .addProperty(ClickhouseConstants.OPTION_APPLICATION_NAME, "java-driver")
      .addProperty(ClickhouseConstants.OPTION_COMPRESSOR, "lz4_safe")
      .addProperty(ClickhouseConstants.OPTION_STRING_CHARSET, "utf-8")
      .addProperty(ClickhouseConstants.OPTION_DEFAULT_ZONE_ID, "Europe/Oslo")
      .addProperty(ClickhouseConstants.OPTION_DATETIME64_EXTRA_NANOS_MODE, "saturate")
      .addProperty(ClickhouseConstants.OPTION_SEND_LOGS_LEVEL, "debug");
  }

  @Override
  protected void after() {
    if (server != null) {
      server.stop();
    }
  }

  public ClickhouseNativeConnectOptions options() {
    return new ClickhouseNativeConnectOptions(options);
  }

  public static boolean isTestingWithExternalDatabase() {
    return isSystemPropertyValid(connectionUri) || isSystemPropertyValid(tlsConnectionUri);
  }

  private static boolean isSystemPropertyValid(String systemProperty) {
    return systemProperty != null && !systemProperty.isEmpty();
  }
}
