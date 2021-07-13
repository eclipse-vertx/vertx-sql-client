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
package io.vertx.oracle;

import io.vertx.codegen.annotations.VertxGen;

import java.sql.ResultSet;

/**
 * Represents the resultset concurrency hint
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public enum ResultSetConcurrency {

  READ_ONLY(ResultSet.CONCUR_READ_ONLY),
  UPDATABLE(ResultSet.CONCUR_UPDATABLE);

  private final int type;

  ResultSetConcurrency(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
