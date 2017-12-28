/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Tuple;
import com.julienviet.pgclient.impl.codec.DataType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class PgFunction<R> {

  private final Connection conn;
  private final long oid;
  private final DataType<?>[] dataTypes;
  private final DataType<?> returnType;

  public PgFunction(Connection conn, long oid, DataType[] dataTypes, DataType<?> returnType) {
    this.conn = conn;
    this.oid = oid;
    this.dataTypes = dataTypes;
    this.returnType = returnType;
  }

  public void call(Tuple args, Handler<AsyncResult<R>> handler) {
    conn.schedule(new FunctionCallCommand<>(oid, dataTypes, args, returnType, ar -> {
      if (ar.succeeded()) {
        Future<R> tFuture = Future.succeededFuture((R) ar.result());
        handler.handle(tFuture);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }));
  }
}
