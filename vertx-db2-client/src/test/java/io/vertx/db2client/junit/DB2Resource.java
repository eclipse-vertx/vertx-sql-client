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

import org.junit.rules.ExternalResource;
import org.testcontainers.containers.Db2Container;

import io.vertx.db2client.DB2ConnectOptions;

public class DB2Resource extends ExternalResource {
	
	private static final String CUSTOM_DB2 = System.getProperty("DB2_HOST", System.getenv("DB2_HOST"));
    
    /**
     * In order for this container to be reused across test runs you need to add the line:
     * <code>testcontainers.reuse.enable=true</code> to your <code>~/.testcontainers.properties</code>
     * file (create it if it does not exist)
     */
    public static final DB2Resource SHARED_INSTANCE = new DB2Resource();
    
    private DB2ConnectOptions options;
    private final Db2Container instance = new Db2Container()
            .acceptLicense()
            .withInitScript("init.sql")
            .withLogConsumer(out -> System.out.print("[DB2] " + out.getUtf8String()))
            .withReuse(true);
    
    @Override
    protected void before() throws Throwable {
    	if (CUSTOM_DB2 == null) {
    		instance.start();
	        options = new DB2ConnectOptions()
	                .setHost(instance.getContainerIpAddress())
	                .setPort(instance.getFirstMappedPort())
	                .setDatabase(instance.getDatabaseName())
	                .setUser(instance.getUsername())
	                .setPassword(instance.getPassword());
    	} else {
	        options = new DB2ConnectOptions()
	                .setHost(get("DB2_HOST"))
	                .setPort(Integer.valueOf(get("DB2_PORT")))
	                .setDatabase(get("DB2_NAME"))
	                .setUser(get("DB2_USER"))
	                .setPassword(get("DB2_PASS"));
    	}
    }
    
	public DB2ConnectOptions options() {
		return new DB2ConnectOptions(options);
	}
	
	private String get(String name) {
		return System.getProperty(name, System.getenv(name));
	}
    
}
