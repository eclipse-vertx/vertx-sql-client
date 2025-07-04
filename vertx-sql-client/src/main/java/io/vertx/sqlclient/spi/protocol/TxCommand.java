/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.spi.protocol;

public class TxCommand<R> extends CommandBase<R> {

  public enum Kind {

    BEGIN(), ROLLBACK(), COMMIT();

    private final String sql;

    Kind() {
      this.sql = name();
    }

    public String sql() {
      return sql;
    }
  }

  private final R result;
  private final Kind kind;

  public TxCommand(Kind kind, R result) {
    this.kind = kind;
    this.result = result;
  }

  public R result() {
    return result;
  }

  public Kind kind() {
    return kind;
  }
}
