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
package io.reactiverse.pgclient.impl.my.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.impl.command.CommandResponse;
import io.reactiverse.pgclient.impl.command.CommandBase;
import io.vertx.core.Handler;

abstract class CommandCodec<R, C extends CommandBase<R>> {

  Handler<? super CommandResponse<R>> completionHandler;
  public Throwable failure;
  public R result;
  final C cmd;
  int sequenceId;
  MyEncoder encoder;

  CommandCodec(C cmd) {
    this.cmd = cmd;
  }

  void encodePayload(MyEncoder encoder) {
    this.encoder = encoder;
  }

  abstract void decodePayload(ByteBuf payload, MyEncoder encoder, int payloadLength, int sequenceId);
}
