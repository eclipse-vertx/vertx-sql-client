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
package io.vertx.oracleclient.impl.commands;

import io.vertx.oracleclient.OracleClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.internal.RowDescriptorBase;

import java.util.ArrayList;
import java.util.List;

public class OracleResponse<R> {

  static class RS<R> {
    R holder;
    int size;
    RowDescriptorBase desc;

    RS(R holder, RowDescriptorBase desc, int size) {
      this.holder = holder;
      this.desc = desc;
      this.size = size;
    }
  }

  private final int update;
  private List<RS<R>> rs;
  private Row ids;
  private List<RS<R>> output;
  private R empty;

  public OracleResponse(int updateCount) {
    this.update = updateCount;
  }

  public void push(R decodeResultSet, RowDescriptorBase desc, int size) {
    if (rs == null) {
      rs = new ArrayList<>();
    }
    rs.add(new RS<>(decodeResultSet, desc, size));
  }

  public void returnedKeys(Row keys) {
    this.ids = keys;
  }

  public void empty(R apply) {
    this.empty = apply;
  }

  public void outputs(R decodeResultSet, RowDescriptorBase desc, int size) {
    if (output == null) {
      output = new ArrayList<>();
    }
    output.add(new RS<>(decodeResultSet, desc, size));
  }

  public void handle(QueryResultHandler<R> handler) {
    if (rs != null) {
      for (RS<R> rs : this.rs) {
        handler.handleResult(update, rs.size, rs.desc, rs.holder, null);
        if (ids != null) {
          handler.addProperty(OracleClient.GENERATED_KEYS, ids);
        }
      }
    }
    if (output != null) {
      for (RS<R> rs : this.output) {
        handler.handleResult(update, rs.size, null, rs.holder, null);
        handler.addProperty(OracleClient.OUTPUT, true);
      }
    }
    if (rs == null && output == null) {
      handler.handleResult(update, -1, null, empty, null);
      if (ids != null) {
        handler.addProperty(OracleClient.GENERATED_KEYS, ids);
      }
    }
  }
}
