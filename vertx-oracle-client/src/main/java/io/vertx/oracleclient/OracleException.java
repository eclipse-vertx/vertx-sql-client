/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient;

import io.vertx.core.VertxException;

/**
 * A {@link RuntimeException} thrown when a Reactive Oracle Client error occurs.
 */
public class OracleException extends VertxException {

  public OracleException(String message) {
    super(message);
  }

  public OracleException(String message, Throwable cause) {
    super(message, cause);
  }

  public OracleException(Throwable cause) {
    super(cause);
  }

  public OracleException(String message, boolean noStackTrace) {
    super(message, noStackTrace);
  }

  public OracleException(String message, Throwable cause, boolean noStackTrace) {
    super(message, cause, noStackTrace);
  }

  public OracleException(Throwable cause, boolean noStackTrace) {
    super(cause, noStackTrace);
  }
}
