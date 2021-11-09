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

/**
 * Indicates a particular property of a session
 */
public enum ServerType {
  /**
   * No certain properties are known about server yet (default)
   */
  UNDEFINED,
  /**
   * Server is in hot standby mode, usually but not necessary such server allows read-write connections
   */
  PRIMARY,
  /**
   * Server is in hot standby mode, only read-only connections are allowed
   */
  REPLICA
}
