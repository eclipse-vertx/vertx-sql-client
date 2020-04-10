/*
 * Copyright (C) 2020 IBM Corporation
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
 */
package io.vertx.db2client.junit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLSyntaxErrorException;
import java.util.Objects;

import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Db2Container;

import io.vertx.db2client.DB2ConnectOptions;

public class DB2Resource extends ExternalResource {
	
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
    private final Db2Container instance = new Db2Container()
            .acceptLicense()
            .withLogConsumer(out -> System.out.print("[DB2] " + out.getUtf8String()))
            .withReuse(true);
    
    @Override
    protected void before() throws Throwable {
        if (started)
          return;

    	if (!CUSTOM_DB2) {
    		instance.start();
	        options = new DB2ConnectOptions()
	                .setHost(instance.getContainerIpAddress())
	                .setPort(instance.getFirstMappedPort())
	                .setDatabase(instance.getDatabaseName())
	                .setUser(instance.getUsername())
	                .setPassword(instance.getPassword());
    	} else {
    	    System.out.println("Using custom DB2 instance as requested via DB2_HOST=" + get("DB2_HOST"));
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
    	try (Connection con = DriverManager.getConnection(jdbcUrl, options.getUser(), options.getPassword())) {
    	  runInitSql(con);
    	}
    	started = true;
    }
    
	public DB2ConnectOptions options() {
		return new DB2ConnectOptions(options);
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
      System.out.println("Running init script at: " + initScript);
      for (String sql : Files.readAllLines(initScript)) {
          if (sql.startsWith("--"))
              continue;
          currentLine += sql;
          if (sql.endsWith(";")) {
              System.out.println("  " + currentLine);
              try {
                con.createStatement().execute(currentLine);
              } catch (SQLSyntaxErrorException e) {
                if (sql.startsWith("DROP ") && e.getErrorCode() == -204) {
                  System.out.println("  ignoring syntax exception: " + e.getMessage());
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
