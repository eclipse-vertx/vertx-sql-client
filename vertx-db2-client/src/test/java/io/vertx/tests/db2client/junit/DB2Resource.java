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
package io.vertx.tests.db2client.junit;

import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.db2client.DB2ConnectOptions;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLSyntaxErrorException;
import java.time.Duration;
import java.util.Objects;

public class DB2Resource extends ExternalResource {

  private static final Logger logger = LoggerFactory.getLogger(DB2Resource.class);

  private static final boolean CUSTOM_DB2 = get("DB2_HOST") != null;

  /**
   * In order for this container to be reused across test runs you need to add the line:
   * <code>testcontainers.reuse.enable=true</code> to your <code>~/.testcontainers.properties</code>
   * file (create it if it does not exist)
   */
  public static final DB2Resource SHARED_INSTANCE = new DB2Resource();

  private boolean started = false;
  private boolean isDb2OnZ = false;
  private DB2ConnectOptions options;
  private final Db2Container instance = new Db2Container("ibmcom/db2:11.5.0.0a")
    .acceptLicense()
    .withLogConsumer(out -> logger.debug("[DB2] {}", out.getUtf8String()))
    .withUsername("vertx")
    .withPassword("vertx")
    .withDatabaseName("vertx")
    .withExposedPorts(50000, 50001)
    .withFileSystemBind("src/test/resources/tls/server/", "/certs/")
    .withFileSystemBind("src/test/resources/tls/db2_tls_setup.sh", "/var/custom/db2_tls_setup.sh")
    .waitingFor(new LogMessageWaitStrategy()
      .withRegEx(".*VERTX SSH SETUP DONE.*")
      .withStartupTimeout(Duration.ofMinutes(10)))
    .withReuse(true);

  @Override
  protected void before() throws Throwable {
    if (started)
      return;

    if (!CUSTOM_DB2) {
      instance.start();
          options = new DB2ConnectOptions()
            .setHost(instance.getHost())
            .setPort(instance.getMappedPort(50000))
                  .setDatabase(instance.getDatabaseName())
                  .setUser(instance.getUsername())
                  .setPassword(instance.getPassword());
      } else {
      logger.info("Using custom DB2 instance as requested via DB2_HOST={}", get("DB2_HOST"));
          Objects.requireNonNull(get("DB2_PORT"), "Must set DB2_PORT to a non-null value if DB2_HOST is set");
          Objects.requireNonNull(get("DB2_NAME"), "Must set DB2_NAME to a non-null value if DB2_HOST is set");
          Objects.requireNonNull(get("DB2_USER"), "Must set DB2_USER to a non-null value if DB2_HOST is set");
          Objects.requireNonNull(get("DB2_PASS"), "Must set DB2_PASS to a non-null value if DB2_HOST is set");
          options = new DB2ConnectOptions()
                  .setHost(get("DB2_HOST"))
                  .setPort(Integer.valueOf(get("DB2_PORT")))
                  .setDatabase(get("DB2_NAME"))
                  .setUser(get("DB2_USER"))
                  .setPassword(get("DB2_PASS"));
      }
      String jdbcUrl = "jdbc:db2://" + options.getHost() + ":" + options.getPort() + "/" + options.getDatabase();
    logger.info("Initializing DB2 database at: {}", jdbcUrl);
      try (Connection con = DriverManager.getConnection(jdbcUrl, options.getUser(), options.getPassword())) {
        runInitSql(con);
      }
      started = true;
    }

  public DB2ConnectOptions options() {
    return new DB2ConnectOptions(options);
  }

  public DB2ConnectOptions secureOptions() {
    int securePort = CUSTOM_DB2 ? 50001 : instance.getMappedPort(50001);
      return new DB2ConnectOptions(options())
          .setPort(securePort)
          .setSsl(true)
          .setSslOptions(new ClientSSLOptions().setTrustOptions(new JksOptions()
            .setPath("src/test/resources/tls/db2-keystore.p12")
            .setPassword("db2test")));
  }

  public boolean isZOS() {
    return isDb2OnZ;
  }

  private static String get(String name) {
    return System.getProperty(name, System.getenv(name));
  }

    private void runInitSql(Connection con) throws Exception {
      isDb2OnZ = con.getMetaData().getDatabaseProductVersion().startsWith("DSN");
      String currentLine = "";
      Path initScript = Paths.get("src", "test", "resources", isDb2OnZ ? "init.zos.sql" : "init.sql");
      logger.info("Running init script at: {}", initScript);
      for (String sql : Files.readAllLines(initScript)) {
          if (sql.startsWith("--"))
              continue;
          currentLine += sql;
          if (sql.endsWith(";")) {
            logger.debug("  {}", currentLine);
              try {
                con.createStatement().execute(currentLine);
              } catch (SQLSyntaxErrorException e) {
                if (sql.startsWith("DROP ") && e.getErrorCode() == -204) {
                  logger.debug("  ignoring syntax exception: {}", e.getMessage());
                } else {
                  throw e;
                }
              }
              currentLine = "";
          }
      }
      if (!currentLine.isEmpty()) {
        throw new IllegalStateException("Dangling SQL on init script. Ensure all statements are terminated with ';' char. SQL: " + currentLine);
      }
  }

}
