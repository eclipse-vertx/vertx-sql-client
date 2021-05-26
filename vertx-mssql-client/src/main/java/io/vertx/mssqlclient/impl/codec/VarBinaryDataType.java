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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.mssqlclient.impl.protocol.datatype.MSSQLDataType;

import java.sql.JDBCType;

public class VarBinaryDataType extends MSSQLDataType {

  private final int length;

  public VarBinaryDataType(int id, int length) {
    super(id, Buffer.class, JDBCType.VARBINARY);
    this.length = length;
  }

  public int getLength() {
    return length;
  }
}
