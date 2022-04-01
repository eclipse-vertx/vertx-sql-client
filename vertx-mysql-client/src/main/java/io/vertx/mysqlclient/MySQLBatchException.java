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

package io.vertx.mysqlclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code MySQLBatchException} is thrown if an error occurs during executions when using {@link io.vertx.sqlclient.PreparedQuery#executeBatch(List)}.
 * The client will try to execute with all the params no matter if one iteration of the executions fails, the iteration count is counted from zero.
 */
public class MySQLBatchException extends RuntimeException {
  /**
   * A mapping between the iteration count and error message, the key is consistent with the batching param list index.
   */
  private final Map<Integer, String> iterationError = new HashMap<>();

  public MySQLBatchException() {
    super("Error occurs during batch execution");
  }

  /**
   * Get the detailed error message of all failed iterations in batching.
   *
   * @return the iteration count and detailed error message mapping
   */
  public Map<Integer, String> getIterationError() {
    return iterationError;
  }

  public void reportError(int iteration, String errorMessage) {
    iterationError.put(iteration, errorMessage);
  }
}
