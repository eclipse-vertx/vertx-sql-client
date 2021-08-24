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

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An entry point to the Vertx Reactive SQL Client used to load available SQL client drivers via 
 * the service-provider loading mechanism.
 * 
 * Service providers and therefore drivers are located using the thread context class loader which 
 * basically determines the availability of a driver.
 * 
 * An SQL client driver instance can then be obtain for a particular connection URI.
 */
public final class DriverLoader {

  private DriverLoader() {}

  /**
   * Loads SQL client drivers using the service-provider loading mechanism.
   * 
   * @return the Driver service loader
   */
  private static ServiceLoader<Driver> load() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(Driver.class, contextClassLoader);
  }

  /**
   * Returns a stream of SQL client drivers.
   * 
   * Note that drivers are lazily instantiated. 
   * 
   * @return a stream of SQL client drivers
   */
  public static Stream<Driver> getDrivers() {
    return StreamSupport.stream(load().spliterator(), false);
  }

  /**
   * Returns the first SQL client driver that supports the connection {@code uri}.
   * 
   * @param connectionUri a connection URI
   * @return a SQL client driver
   * @throws NullPointerException if {@code uri} is null
   * @throws IllegalStateException if no suitable driver was found
   */
  public static Driver getDriver(String connectionUri) {
    Objects.requireNonNull(connectionUri, "Connection uri must not be null");
    return getDrivers()
      .filter(d -> d.acceptsUri(connectionUri))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No suitable driver found for " + connectionUri));
  }
}
