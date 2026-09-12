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

package io.vertx.sqlclient.spi.protocol;

/**
 * A command that runs a statement written by the application.
 * <p/>
 * Implemented by queries and by any other command carrying user SQL, such as COPY. Tracing and
 * metrics report these and ignore the rest.
 */
public interface SqlCommand {

  /**
   * @return the statement sent to the database
   */
  String sql();
}
