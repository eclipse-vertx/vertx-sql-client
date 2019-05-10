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

package io.vertx.mysqlclient.impl;

import io.netty.channel.ChannelPipeline;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.InitCommand;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.mysqlclient.impl.codec.MySQLCodec;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.impl.NetSocketInternal;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MySQLSocketConnection extends SocketConnectionBase {

  private MySQLCodec codec;

  public MySQLSocketConnection(NetSocketInternal socket, int pipeliningLimit, Context context) {
    super(socket, pipeliningLimit, context);
  }

  void sendStartupMessage(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database);
    cmd.handler = completionHandler;
    schedule(cmd);
  }

  @Override
  public void init() {
    codec = new MySQLCodec();
    ChannelPipeline pipeline = socket.channelHandlerContext().pipeline();
    pipeline.addBefore("handler", "codec", codec);
    super.init();
  }

  @Override
  public int getProcessId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSecretKey() {
    throw new UnsupportedOperationException();
  }
}
