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

import com.julienviet.pgclient.impl.codec.decoder.InboundMessage;
import com.julienviet.pgclient.impl.codec.decoder.message.CloseComplete;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class CloseStatementCommand extends CommandBase<Void> {

  CloseStatementCommand(Handler<? super CommandResponse<Void>> handler) {
    super(handler);
  }

  @Override
  void exec(SocketConnection conn) {
    /*
    if (conn.psCache == null) {
      conn.writeMessage(new Close().setStatement(statement));
      conn.writeMessage(Sync.INSTANCE);
    } else {
    }
    */
    handler.handle(CommandResponse.success(null));
    completionHandler.handle(null);
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    throw new UnsupportedOperationException("Uh");
  }

  @Override
  void fail(Throwable err) {
    handler.handle(CommandResponse.failure(err));
  }
}
