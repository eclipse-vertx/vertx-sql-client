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

package io.vertx.pgclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import java.sql.JDBCType;

@RunWith(VertxUnitRunner.class)
public class PgBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected JDBCType getNumericJDBCType() {
    return JDBCType.NUMERIC;
  }

  @Override
  protected Class<? extends Number> getNumericClass() {
    return Numeric.class;
  }

  @Override
  protected Number getNumericValue(Number value) {
    return Numeric.create(value);
  }

  @Override
  protected Number getNumericValue(String value) {
    return Numeric.parse(value);
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }
}
