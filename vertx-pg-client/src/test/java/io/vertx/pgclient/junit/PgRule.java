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
package io.vertx.pgclient.junit;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.store.IArtifactStore;
import io.vertx.pgclient.PgConnectOptions;
import org.junit.AssumptionViolatedException;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.V10_6;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.V9_6_11;


/**
 * @deprecated EmbeddedPostgres is not maintained any more (must be kept until the unix domain socket tests migration)
 */
@Deprecated
public class PgRule extends ExternalResource {

  private static final String connectionUri = System.getProperty("connection.uri");
  private static EmbeddedPostgres postgres;

  public synchronized static PgConnectOptions startPg() throws Exception {
    return startPg(false);
  }

  public synchronized static PgConnectOptions startPg(boolean domainSockets) throws Exception {

    if (connectionUri != null && !connectionUri.isEmpty()) {
      return PgConnectOptions.fromUri(connectionUri);
    }
    if (postgres != null) {
      throw new IllegalStateException();
    }
    IRuntimeConfig config;
    String a = System.getProperty("target.dir", "target");
    File targetDir = new File(a);
    if (targetDir.exists() && targetDir.isDirectory()) {
      config = EmbeddedPostgres.cachedRuntimeConfig(targetDir.toPath());
    } else {
      throw new AssertionError("Cannot access target dir");
    }

    // Domain sockets
    File sock;
    if (domainSockets) {
      // Create temp file, length must be < 107 chars (Linux limitation)
      sock = Files.createTempFile("pg_", ".sock").toFile();
      assertTrue(sock.delete());
      assertTrue(sock.mkdir());
      sock.deleteOnExit();
      Files.setPosixFilePermissions(sock.toPath(), new HashSet<>(Arrays.asList(
        PosixFilePermission.OWNER_EXECUTE,
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.GROUP_EXECUTE,
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.GROUP_WRITE
      )));
      config = useDomainSocketRunTimeConfig(config, sock);
    } else {
      sock = null;
    }

    File dataDir = Files.createTempFile(targetDir.toPath(), "pg_", ".data").toFile();
    assertTrue(dataDir.delete());
    assertTrue(dataDir.mkdir());

    postgres = new EmbeddedPostgres(getPostgresVersion(), dataDir.getAbsolutePath());
    postgres.start(config,
      "localhost",
      8081,
      "postgres",
      "postgres",
      "postgres",
      Collections.emptyList());
    File setupFile = getTestResource("create-postgres.sql");
    postgres.getProcess().get().importFromFile(setupFile);

    PgConnectOptions options = new PgConnectOptions();
    options.setHost(domainSockets ? sock.getAbsolutePath() : "localhost");
    options.setPort(8081);
    options.setUser("postgres");
    options.setPassword("postgres");
    options.setDatabase("postgres");

    // Get the real error log from PG that is never considered otherwise
    if (!new File(dataDir, "postmaster.pid").exists()) {
      PostgresProcess process = postgres.getProcess().get();
      Field f = de.flapdoodle.embed.process.runtime.AbstractProcess.class.getDeclaredField("process");
      f.setAccessible(true);
      ProcessControl ctrl = (ProcessControl) f.get(process);
      InputStreamReader reader = ctrl.getError();
      char[] buff = new char[1024];
      int amount;
      while ((amount = reader.read(buff)) != -1) {
        String s = new String(buff, 0, amount);
        System.out.println(s);
      }
    }
    return options;
  }

  public synchronized static void stopPg() {
    if (postgres != null) {
      try {
        postgres.stop();
      } finally {
        postgres = null;
      }
    }
  }

  private static File getTestResource(String name) throws Exception {
    try (InputStream in = PgRule.class.getClassLoader().getResourceAsStream(name)) {
      Path path = Files.createTempFile("pg-client", ".tmp");
      Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
      File file = path.toFile();
      file.deleteOnExit();
      return file;
    }
  }

  private static IRuntimeConfig useDomainSocketRunTimeConfig(IRuntimeConfig config, File sock) throws Exception {
    return new RunTimeConfigBase(config) {
      @Override
      public ICommandLinePostProcessor getCommandLinePostProcessor() {
        ICommandLinePostProcessor commandLinePostProcessor = config.getCommandLinePostProcessor();
        return (distribution, args) -> {
          List<String> result = commandLinePostProcessor.process(distribution, args);
          if (result.get(0).endsWith("postgres")) {
            result = new ArrayList<>(result);
            result.add("-k");
            result.add(sock.getAbsolutePath());
          }
          return result;
        };
      }
    };
  }

  private static Version getPostgresVersion() {
    String specifiedVersion = System.getProperty("embedded.postgres.version");
    Version version;
    if (specifiedVersion == null || specifiedVersion.isEmpty()) {
      // if version is not specified then V10 will be used by default
      version = V10_6;
    } else {
      version = supportedPgVersions.get(specifiedVersion);
    }
    if (version == null) {
      throw new IllegalArgumentException("embedded postgres only supports the following versions: " + supportedPgVersions.keySet().toString() + "instead of " + specifiedVersion);
    }
    return version;
  }

  private static abstract class RunTimeConfigBase implements IRuntimeConfig {
    private final IRuntimeConfig config;

    private RunTimeConfigBase(IRuntimeConfig config) {
      this.config = config;
    }

    @Override
    public ProcessOutput getProcessOutput() {
      return config.getProcessOutput();
    }

    @Override
    public IArtifactStore getArtifactStore() {
      return config.getArtifactStore();
    }

    @Override
    public boolean isDaemonProcess() {
      return config.isDaemonProcess();
    }
  }

  private static final Map<String, Version> supportedPgVersions = new HashMap<>();

  static {
    supportedPgVersions.put("9.6", V9_6_11);
    supportedPgVersions.put("10.6", V10_6);
  }

  private boolean domainSockets;
  private PgConnectOptions options;

  public PgConnectOptions options() {
    return new PgConnectOptions(options);
  }


  public PgRule domainSockets(boolean domainSockets) {
    this.domainSockets = domainSockets;
    return this;
  }

  @Override
  protected void before() throws Throwable {
    options = startPg(domainSockets);
  }

  @Override
  protected void after() {
    if (options != null) {
      try {
        stopPg();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          getPostgresVersion();
        }
        catch (Exception e) {
          throw new AssumptionViolatedException(e.getMessage());
        }

        before();
        try {
          base.evaluate();
        } finally {
          after();
        }
      }
    };
  }

}
