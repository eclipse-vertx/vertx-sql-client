/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.spi.protocol;

public class SavepointCommand<R> extends CommandBase<R> {

  public enum Kind {
    CREATE("SAVEPOINT "),
    ROLLBACK_TO("ROLLBACK TO SAVEPOINT "),
    RELEASE("RELEASE SAVEPOINT ");

    private final String sqlPrefix;

    Kind(String sqlPrefix) {
      this.sqlPrefix = sqlPrefix;
    }

    public String sql(String name) {
      return sqlPrefix + name;
    }
  }

  private final Kind kind;
  private final String name;
  private final R result;

  public SavepointCommand(Kind kind, String name, R result) {
    this.kind = kind;
    this.name = name;
    this.result = result;
  }

  public Kind kind() {
    return kind;
  }

  public String name() {
    return name;
  }

  public String sql() {
    return kind.sql(name);
  }

  public R result() {
    return result;
  }
}
