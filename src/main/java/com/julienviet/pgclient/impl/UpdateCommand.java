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

import com.julienviet.pgclient.UpdateResult;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.decoder.DecodeContext;
import com.julienviet.pgclient.codec.encoder.message.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class UpdateCommand extends UpdateCommandBase {

  private final String sql;
  private final Handler<AsyncResult<UpdateResult>> handler;

  UpdateCommand(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    this.handler = handler;
    this.sql = sql;
  }

  @Override
  void exec(SocketConnection conn) {
    conn.decodeQueue.add(new DecodeContext(true, null, DataFormat.TEXT));
    conn.writeMessage(new Query(sql));
  }

  @Override
  void handleResult(UpdateResult result) {
    handler.handle(Future.succeededFuture(result));
  }


  @Override
  void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }

  public String getSql() {
    return sql;
  }
}
