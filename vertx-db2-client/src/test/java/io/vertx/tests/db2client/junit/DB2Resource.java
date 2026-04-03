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

import com.github.dockerjava.api.model.Capability;
import io.netty.util.internal.PlatformDependent;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.db2client.DB2ConnectOptions;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.time.Duration;
import java.util.Objects;

public class DB2Resource extends ExternalResource {

  private static final Logger logger = LoggerFactory.getLogger(DB2Resource.class);

  private static final boolean CUSTOM_DB2 = get("DB2_HOST") != null;
  private static final String CONTAINER_VERSION = get("db2-container.version", "12.1.4.0");

  public static final DB2Resource SHARED_INSTANCE = new DB2Resource();

  private boolean started = false;
  private DB2ConnectOptions options;
  private final GenericContainer instance = new GenericContainer<>("icr.io/db2_community/db2:" + CONTAINER_VERSION)
    .withCreateContainerCmdModifier(cmd -> cmd
      .withCapAdd(Capability.IPC_LOCK)
      .withCapAdd(Capability.IPC_OWNER))
    .withEnv("LICENSE", "accept")
    .withEnv("DB2INSTANCE", "vertx")
    .withEnv("DB2INST1_PASSWORD", "vertx")
    .withEnv("DBNAME", "vertx")
    .withEnv("BLU", "false")
    .withEnv("ENABLE_ORACLE_COMPATIBILITY", "false")
    .withEnv("UPDATEAVAIL", "NO")
    .withEnv("TO_CREATE_SAMPLEDB", "false")
    .withEnv("REPODB", "false")
    .withEnv("IS_OSXFS", String.valueOf(PlatformDependent.isOsx()))
    .withEnv("PERSISTENT_HOME", "true")
    .withEnv("HADR_ENABLED", "false")
    .withEnv("ETCD_ENDPOINT", "")
    .withEnv("ETCD_USERNAME", "")
    .withEnv("ETCD_PASSWORD", "")
    .withEnv("AUTOCONFIG", "false")
    .withEnv("ARCHIVE_LOGS", "false")
    .withExposedPorts(50000, 50001)
    .withClasspathResourceMapping("tls/server/", "/certs/", BindMode.READ_ONLY)
    .withClasspathResourceMapping("tls/01-db2_tls_setup.sh", "/var/custom/01-db2_tls_setup.sh", BindMode.READ_ONLY)
    .withClasspathResourceMapping("init.sql", "/tmp/init.sql", BindMode.READ_ONLY)
    .withClasspathResourceMapping("02-init-db.sh", "/var/custom/02-init-db.sh", BindMode.READ_ONLY)
    .withLogConsumer(out -> logger.debug("[DB2] {}", out.getUtf8String()))
    .waitingFor(new LogMessageWaitStrategy()
      .withRegEx(".*DB2 DATABASE INITIALIZATION COMPLETE.*")
      .withStartupTimeout(Duration.ofMinutes(10)));

  @Override
  protected void before() throws Throwable {
    if (started)
      return;

    if (!CUSTOM_DB2) {
      instance.start();
          options = new DB2ConnectOptions()
            .setHost(instance.getHost())
            .setPort(instance.getMappedPort(50000))
            .setDatabase("vertx")
            .setUser("vertx")
            .setPassword("vertx");
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
      logger.info("Custom DB2 instance must be manually initialized with init.sql");
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

  private static String get(String name) {
    return System.getProperty(name, System.getenv(name));
  }

  private static String get(String name, String defaultValue) {
    String value = System.getProperty(name, System.getenv(name));
    return value != null && !value.isEmpty() ? value : defaultValue;
  }

}
