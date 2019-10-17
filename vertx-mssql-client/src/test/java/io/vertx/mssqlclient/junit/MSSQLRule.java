package io.vertx.mssqlclient.junit;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.MSSQLServerContainer;

public class MSSQLRule extends ExternalResource {
  private MSSQLServer server;
  private MSSQLConnectOptions options;

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule();

  @Override
  protected void before() {
    if (this.server == null) {
      this.options = startMSSQL();
    }
  }

  @Override
  protected void after() {
    if (this != SHARED_INSTANCE) {
      stopMSSQL();
    }
  }

  private MSSQLConnectOptions startMSSQL() {
    server = new MSSQLServer();
    server.withInitScript("init.sql");
    server.start();

    MSSQLConnectOptions options = new MSSQLConnectOptions()
      .setHost(server.getContainerIpAddress())
      .setPort(server.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT))
      .setUser(server.getUsername())
      .setPassword(server.getPassword());
//      .setDatabase(server.getDatabaseName()); // unsupported by Testcontainers
    return options;
  }

  private void stopMSSQL() {
    if (server != null) {
      try {
        server.stop();
      } finally {
        server = null;
      }
    }
  }

  public MSSQLConnectOptions options() {
    return new MSSQLConnectOptions(options);
  }

  private static class MSSQLServer extends MSSQLServerContainer {
    @Override
    protected void configure() {
      this.addExposedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT);
      this.addEnv("ACCEPT_EULA", "Y");
      this.addEnv("SA_PASSWORD", this.getPassword());
    }
  }
}
