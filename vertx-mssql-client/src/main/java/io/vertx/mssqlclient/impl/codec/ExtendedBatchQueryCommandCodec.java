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

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.internal.TupleInternal;
import io.vertx.sqlclient.impl.CommandResponse;
import io.vertx.sqlclient.internal.command.ExtendedQueryCommand;

import java.util.List;

class ExtendedBatchQueryCommandCodec<T> extends ExtendedQueryCommandBaseCodec<T> {

  private final List<TupleInternal> paramsList;

  private int paramsIdx;
  private int messageDecoded;

  ExtendedBatchQueryCommandCodec(ExtendedQueryCommand<T> cmd) {
    super(cmd);
    paramsList = cmd.paramsList();
  }

  @Override
  void encode() {
    if (paramsList.isEmpty()) {
      tdsMessageCodec.decoder().fireCommandResponse(CommandResponse.failure("Can not execute batch query with 0 sets of batch parameters."));
      return;
    }
    super.encode();
  }

  @Override
  protected void handleDecodingComplete() {
    if (paramsList.size() == 1 || ++messageDecoded == 2) {
      complete();
    } else {
      sendExecRequest();
    }
  }

  @Override
  protected TupleInternal prepexecRequestParams() {
    paramsIdx = 1;
    return paramsList.get(0);
  }

  @Override
  protected void writeRpcRequestBatch(ByteBuf packet) {
    for (int initial = paramsIdx; paramsIdx < paramsList.size(); paramsIdx++) {
      if (initial != paramsIdx) {
        packet.writeByte(0xFF); // batch separator
      }
      super.writeRpcRequestBatch(packet);
    }
  }

  @Override
  protected TupleInternal execRequestParams() {
    return paramsList.get(paramsIdx);
  }
}
