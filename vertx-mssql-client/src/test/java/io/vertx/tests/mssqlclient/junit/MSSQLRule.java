/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.mssqlclient.junit;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import org.junit.AssumptionViolatedException;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import static io.vertx.mssqlclient.MSSQLConnectOptions.DEFAULT_PORT;
import static io.vertx.tests.mssqlclient.junit.MSSQLRule.Config.STANDARD;
import static org.testcontainers.containers.BindMode.READ_ONLY;

public class MSSQLRule extends ExternalResource {

  public enum Config {
    STANDARD("connection.uri", null),
    TLS("tls.connection.uri", "mssql-tls.conf"),
    FORCE_ENCRYPTION("force.encryption.connection.uri", "mssql-force-encryption.conf"),
    STRICT_ENCRYPTION("strict.encryption.connection.uri", "mssql-strict-encryption.conf");

    private final String connectionUriSystemProperty;
    private final String mssqlConf;

    Config(String connectionUriSystemProperty, String mssqlConf) {
      this.connectionUriSystemProperty = connectionUriSystemProperty;
      this.mssqlConf = mssqlConf;
    }
  }

  private enum ServerVersion {
    MSSQL_2019("2019-latest"),
    MSSQL_2022("2022-latest"),
    MSSQL_2025("2025-latest");

    private final String dockerImageTag;

    ServerVersion(String dockerImageTag) {
      this.dockerImageTag = dockerImageTag;
    }

    boolean supportsConfig(Config config) {
      // STRICT_ENCRYPTION (TDS 8.0) is only supported on SQL Server 2025+
      return config != Config.STRICT_ENCRYPTION || this == MSSQL_2025;
    }

    static ServerVersion fromDockerTag(String dockerImageTag) {
      if (dockerImageTag == null || dockerImageTag.isEmpty()) {
        return MSSQL_2025;
      }
      switch (dockerImageTag) {
        case "2019-latest":
          return MSSQL_2019;
        case "2022-latest":
          return MSSQL_2022;
        case "2025-latest":
          return MSSQL_2025;
        default:
          throw new IllegalArgumentException("Unsupported SQL Server version: " + dockerImageTag);
      }
    }
  }

  public static final MSSQLRule SHARED_INSTANCE = new MSSQLRule(STANDARD);

  private static final String USER = "SA";
  private static final String PASSWORD = "A_Str0ng_Required_Password";
  private static final String INIT_SQL = "/opt/data/init.sql";

  private final Config config;
  private final boolean skipOnUnsupportedConfig;

  private ServerContainer<?> server;
  private MSSQLConnectOptions options;
  private ServerVersion serverVersion;

  public MSSQLRule(Config config) {
    this(config, false);
  }

  public MSSQLRule(Config config, boolean skipOnUnsupportedConfig) {
    this.config = Objects.requireNonNull(config);
    this.skipOnUnsupportedConfig = skipOnUnsupportedConfig;
  }

  @Override
  protected void before() throws IOException {
    String connectionUri = System.getProperty(config.connectionUriSystemProperty);
    if (!isNullOrEmpty(connectionUri)) {
      // use an external database for testing
      options = MSSQLConnectOptions.fromUri(connectionUri);
    } else if (this.server == null) {
      options = startMSSQL();
    }
  }

  private boolean isNullOrEmpty(String connectionUri) {
    return connectionUri == null || connectionUri.isEmpty();
  }

  @Override
  protected void after() {
    if (isNullOrEmpty(System.getProperty(config.connectionUriSystemProperty)) && this != SHARED_INSTANCE) {
      stopMSSQL();
    }
  }

  private MSSQLConnectOptions startMSSQL() throws IOException {
    String containerVersion = System.getProperty("mssql-container.version");
    serverVersion = ServerVersion.fromDockerTag(containerVersion);
    if (!serverVersion.supportsConfig(config)) {
      String message = String.format("SQL Server %s does not support %s configuration.", serverVersion.dockerImageTag, config);
      if (skipOnUnsupportedConfig) {
        throw new AssumptionViolatedException(message);
      } else {
        throw new IllegalStateException(message);
      }
    }

    server = new ServerContainer<>("mcr.microsoft.com/mssql/server:" + serverVersion.dockerImageTag)
      .withEnv("ACCEPT_EULA", "Y")
      .withEnv("TZ", "UTC")
      .withEnv("SA_PASSWORD", PASSWORD)
      .withClasspathResourceMapping("init.sql", INIT_SQL, READ_ONLY)
      .waitingFor(Wait.forLogMessage(".*The tempdb database has \\d+ data file\\(s\\).*\\n", 2));

    if (System.getProperties().containsKey("containerFixedPort")) {
      server.withFixedExposedPort(DEFAULT_PORT, DEFAULT_PORT);
    } else {
      server.withExposedPorts(DEFAULT_PORT);
    }
    if (!isNullOrEmpty(config.mssqlConf)) {
      server
        .withClasspathResourceMapping(config.mssqlConf, "/var/opt/mssql/mssql.conf", READ_ONLY)
        .withClasspathResourceMapping("mssql.key", "/etc/ssl/certs/mssql.key", READ_ONLY)
        .withClasspathResourceMapping("mssql.pem", "/etc/ssl/certs/mssql.pem", READ_ONLY);
    }

    // Workaround for SQL Server 2025 CPU topology bug (microsoft/mssql-docker#954)
    // SQL Server 2025 crashes when processor count doesn't divide evenly by cores-per-socket
    // Limit container to a safe CPU count to avoid assertion failure in sosnumap.cpp
    if (serverVersion == ServerVersion.MSSQL_2025) {
      int safeCpuCount = calculateSafeCpuCount();
      server.withCreateContainerCmdModifier(cmd ->
        cmd.getHostConfig().withCpusetCpus("0-" + (safeCpuCount - 1))
      );
    }

    server.start();

    initDb();

    return new MSSQLConnectOptions()
      .setHost(server.getHost())
      .setPort(server.getMappedPort(DEFAULT_PORT))
      .setUser("SA")
      .setPassword(PASSWORD);
  }

  private void initDb() throws IOException {
    try {
      ExecResult execResult = server.execInContainer(cmdArgs());
      if (execResult.getExitCode() != 0) {
        throw new RuntimeException(String.format("Failure while initializing database%nstdout:%s%nstderr:%s%n", execResult.getStdout(), execResult.getStderr()));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private String[] cmdArgs() {
    Stream.Builder<String> builder = Stream.builder();
    builder.add("/opt/mssql-tools18/bin/sqlcmd");
    builder.add("-S").add("localhost");
    builder.add("-U").add(USER);
    builder.add("-P").add(PASSWORD);
    builder.add("-i").add(INIT_SQL);
    if (config == Config.STRICT_ENCRYPTION) {
      builder.add("-Ns");
      builder.add("-J").add("/etc/ssl/certs/mssql.pem");
      builder.add("-F").add("sql1");
    } else {
      builder.add("-C");
      builder.add("-No");
    }
    return builder.build().toArray(String[]::new);
  }

  /**
   * Calculate a safe CPU count for SQL Server 2025 container to avoid CPU topology assertion failure.
   * Returns the largest power of 2 that is <= available processors.
   * Powers of 2 are safe because they divide evenly by common core-per-socket configurations.
   *
   * @return safe CPU count (minimum 1, maximum available processors if already power of 2)
   */
  private static int calculateSafeCpuCount() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();

    // If already a power of 2, use it as-is
    if ((availableProcessors & (availableProcessors - 1)) == 0) {
      return availableProcessors;
    }

    // Find the largest power of 2 that is less than availableProcessors
    // This ensures the count divides evenly in most CPU topologies
    int safeCpuCount = 1;
    while (safeCpuCount * 2 <= availableProcessors) {
      safeCpuCount *= 2;
    }

    return safeCpuCount;
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

  private static class ServerContainer<SELF extends ServerContainer<SELF>> extends GenericContainer<SELF> {

    public ServerContainer(String dockerImageName) {
      super(dockerImageName);
    }

    public SELF withFixedExposedPort(int hostPort, int containerPort) {
      super.addFixedExposedPort(hostPort, containerPort, InternetProtocol.TCP);
      return self();
    }
  }
}
