/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;

/**
 * This option determines whether the session must have certain properties to be acceptable.
 * It's typically used in combination with {@link Driver#createPool(Vertx, List, PoolOptions)} overload
 * to select the first acceptable alternative among several hosts.
 */
public enum ServerRequirement {
  /**
   * Any successful connection is acceptable (default)
   */
  ANY,
  /**
   * Server must not be in hot standby mode, usually but not necessary such server allows read-write connections
   */
  PRIMARY,
  /**
   * Server must be in hot standby mode, only read-only connections are allowed
   */
  REPLICA,
  /**
   * First try to find a standby server, but if none of the listed hosts is a standby server, try again in any mode
   */
  PREFER_REPLICA;
}
