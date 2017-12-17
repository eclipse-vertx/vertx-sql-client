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
import com.julienviet.pgclient.impl.codec.decoder.message.ReadyForQuery;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class CommandBase<R> {

  protected Handler<Void> completionHandler;
  Handler<? super CommandResponse<R>> handler;
  Throwable failure;
  R result;

  public CommandBase(Handler<? super CommandResponse<R>> handler) {
    this.handler = handler;
  }

  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      ReadyForQuery readyForQuery = (ReadyForQuery) msg;
      completionHandler.handle(null);
      if (failure != null) {
        handler.handle(CommandResponse.failure(failure, readyForQuery.txStatus()));
      } else {
        handler.handle(CommandResponse.success(result, readyForQuery.txStatus()));
      }
    } else {
      System.out.println(getClass().getSimpleName() + " should handle message " + msg);
    }
  }

  void foo(SocketConnection conn) {
    conn.bilto(this);
  }

  abstract void exec(SocketConnection conn);

  abstract void fail(Throwable err);

}
