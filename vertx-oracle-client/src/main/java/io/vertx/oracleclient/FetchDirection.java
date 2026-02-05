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
package io.vertx.oracleclient;

import java.sql.ResultSet;

/**
 * Represents the fetch direction hint
 */
public enum FetchDirection {

  FORWARD(ResultSet.FETCH_FORWARD),
  REVERSE(ResultSet.FETCH_REVERSE),
  UNKNOWN(ResultSet.FETCH_UNKNOWN);

  private final int type;

  FetchDirection(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
