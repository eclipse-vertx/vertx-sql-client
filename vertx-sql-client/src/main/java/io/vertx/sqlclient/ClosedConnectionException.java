/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient;

import io.vertx.core.VertxException;

/**
 * When a client operation fails with this exception, the underlying connection may have been lost unexpectedly.
 */
public class ClosedConnectionException extends VertxException {

  public static ClosedConnectionException INSTANCE = new ClosedConnectionException();

  private ClosedConnectionException() {
    super("Failed to read any response from the server, the underlying connection may have been lost unexpectedly.", true);
  }
}
