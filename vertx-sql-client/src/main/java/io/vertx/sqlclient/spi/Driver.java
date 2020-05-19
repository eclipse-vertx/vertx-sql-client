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
 *
 */
package io.vertx.sqlclient.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * An entry point to the Vertx Reactive SQL Client
 * Every driver must implement this interface.
 */
public interface Driver {
  
  public static enum KnownDrivers {
    ANY_DRIVER,
    DB2,
    SQLSERVER,
    MYSQL,
    POSTGRESQL
  }
  
  /**
   * Creates an instance of {@link SqlConnectOptions} that corresponds to the provided <code>driverName</code>
   * @param driverName  The name of the Driver (as returned by {@link #name()}) to create connect options for.
   * Known values can be found in {@link KnownDrivers}
   * @return the new connect options
   * @throws ServiceConfigurationError If 0 or >1 matching drivers are found
   * @throws NullPointerException If <code>driverName</code> is null
   */
  static SqlConnectOptions createConnectOptions(String driverName) {
    Objects.requireNonNull(driverName, "Must provide a valid driverName. Some known values are: " + Arrays.toString(KnownDrivers.values()));
    
    boolean anyDriver = KnownDrivers.ANY_DRIVER.name().equalsIgnoreCase(driverName);
    List<String> discoverdDrivers = new ArrayList<>();
    List<Driver> candidates = new ArrayList<>(1);
    for (Driver d : ServiceLoader.load(Driver.class)) {
      discoverdDrivers.add(d.name());
      if (d.name().equalsIgnoreCase(driverName) || anyDriver) {
        candidates.add(d);
      }
    }
    
    if (candidates.size() == 0) {
      if (anyDriver) {
        throw new ServiceConfigurationError("No implementations of " + Driver.class.getCanonicalName() + " were found");
      } else {
        throw new ServiceConfigurationError("No implementations of " + Driver.class.getCanonicalName() + " were found with a name of '" + driverName + "'");
      }
    } else if (candidates.size() > 1) {
      if (anyDriver) {
        throw new ServiceConfigurationError("Multiple implementations of " + Driver.class.getCanonicalName() + " were found: " + 
            candidates.stream().map(Driver::name).collect(Collectors.toList()));        
      } else {
        throw new ServiceConfigurationError("Multiple implementations of " + Driver.class.getCanonicalName() + " were found with a name of '" + 
            driverName + "': " + candidates);
      }
    }
    
    return candidates.get(0).createConnectOptions();
  }
  
  /**
   * Creates an instance of {@link SqlConnectOptions} that corresponds to the provided <code>driverName</code>
   * @param driverName The name of the Driver (as returned by {@link #name()}) to create connect options for
   * @return the new connect options
   * @throws ServiceConfigurationError If 0 or >1 matching drivers are found
   * @throws NullPointerException If <code>driverName</code> is null
   */
  static SqlConnectOptions createConnectOptions(KnownDrivers driverName) {
    Objects.requireNonNull(driverName, "Must provide a valid driverName. Some known values are: " + Arrays.toString(KnownDrivers.values()));
    return createConnectOptions(driverName.name());
  }
  
  /**
   * @return Creates a new set of {@link SqlConnectOptions}
   */
  SqlConnectOptions createConnectOptions();
  
  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and default {@link PoolOptions}
   *
   * @param connectOptions the options used to create the connection pool, such as database hostname
   * @return the connection pool
   */
  default Pool createPool(SqlConnectOptions connectOptions) {
    return createPool(connectOptions, new PoolOptions());
  }
  
  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param connectOptions the options used to create the connection pool, such as database hostname
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  Pool createPool(SqlConnectOptions connectOptions, PoolOptions poolOptions);
  
  /**
   * Create a connection pool to the database configured with the given {@code connectOptions} and {@code poolOptions}.
   *
   * @param vertx the Vertx instance to be used with the connection pool
   * @param connectOptions the options used to create the connection pool, such as database hostname
   * @param poolOptions the options for creating the pool
   * @return the connection pool
   */
  Pool createPool(Vertx vertx, SqlConnectOptions connectOptions, PoolOptions poolOptions);
  
  /**
   * @return true if the driver accepts the {@code connectOptions}, false otherwise
   */
  default boolean acceptsOptions(SqlConnectOptions connectOptions) {
    return createConnectOptions().getClass().isAssignableFrom(connectOptions.getClass()) ||
        SqlConnectOptions.class.equals(connectOptions.getClass());
  }
  
  /**
   * @return The name of the driver. Names for known drivers are enumerated in {@link KnownDrivers}
   */
  String name();
  
}
