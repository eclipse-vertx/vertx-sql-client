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

package io.vertx.pgclient.impl.auth.scram;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.pgclient.impl.codec.ScramClientInitialMessage;

public interface ScramSession {

  /*
   * The client selects one of the supported mechanisms from the list,
   *  and sends a SASLInitialResponse message to the server.
   * The message includes the name of the selected mechanism, and
   *  an optional Initial Client Response, if the selected mechanism uses that.
   */
  ScramClientInitialMessage createInitialSaslMessage(ByteBuf in, ChannelHandlerContext ctx);

  /*
   * One or more server-challenge and client-response message will follow.
   * Each server-challenge is sent in an AuthenticationSASLContinue message,
   *   followed by a response from client in an SASLResponse message.
   * The particulars of the messages are mechanism specific.
   */
  String receiveServerFirstMessage(ByteBuf in);

  /*
   * Finally, when the authentication exchange is completed successfully,
   *   the server sends an AuthenticationSASLFinal message, followed immediately by an AuthenticationOk message.
   * The AuthenticationSASLFinal contains additional server-to-client data,
   *   whose content is particular to the selected authentication mechanism.
   * If the authentication mechanism doesn't use additional data that's sent at completion,
   *   the AuthenticationSASLFinal message is not sent
   */
  void checkServerFinalMessage(ByteBuf in);

}
