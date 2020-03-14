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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.MSSQLRowImpl;
import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  final MSSQLRowDesc desc;

  RowResultDecoder(Collector<Row, C, R> collector, MSSQLRowDesc desc) {
    super(collector);
    this.desc = desc;
  }

  @Override
  public Row decodeRow(int len, ByteBuf in) {
    Row row = new MSSQLRowImpl(desc);
    for (int c = 0; c < len; c++) {
      Object decoded = null;
      ColumnData columnData = desc.columnDatas[c];
      decoded = MSSQLDataTypeCodec.decode(columnData.dataType(), in);
      row.addValue(decoded);
    }
    return row;
  }
}
