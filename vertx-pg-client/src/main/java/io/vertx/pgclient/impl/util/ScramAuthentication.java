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

package io.vertx.pgclient.impl.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.ongres.scram.client.ScramClient;
import com.ongres.scram.client.ScramSession;
import com.ongres.scram.common.exception.ScramException;
import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import com.ongres.scram.common.stringprep.StringPreparations;

import io.netty.buffer.ByteBuf;
import io.vertx.pgclient.impl.codec.ScramClientInitialMessage;

public class ScramAuthentication {

  private static final String SCRAM_SHA_256 = "SCRAM-SHA-256";

  private final String username;
  private final String password;
  private ScramSession scramSession;
  private ScramSession.ClientFinalProcessor clientFinalProcessor;


  public ScramAuthentication(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /*
   * The client selects one of the supported mechanisms from the list,
   *  and sends a SASLInitialResponse message to the server.
   * The message includes the name of the selected mechanism, and
   *  an optional Initial Client Response, if the selected mechanism uses that.
   */
  public ScramClientInitialMessage createInitialSaslMessage(ByteBuf in) {
    List<String> mechanisms = new ArrayList<>();

    while (0 != in.getByte(in.readerIndex())) {
       String mechanism = Util.readCStringUTF8(in);
         mechanisms.add(mechanism);
    }

    if (mechanisms.isEmpty()) {
      throw new UnsupportedOperationException("SASL Authentication : the server returned no mecanism");
    }

    // SCRAM-SHA-256-PLUS added in postgresql 11 is not supported
    if (!mechanisms.contains(SCRAM_SHA_256)) {
        throw new UnsupportedOperationException("SASL Authentication : only SCRAM-SHA-256 is currently supported, server wants " + mechanisms);
    }


    ScramClient scramClient = ScramClient
          .channelBinding(ScramClient.ChannelBinding.NO)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised(mechanisms.toArray(new String[0]))
          .setup();


    // this user name will be ignored, the user name that was already sent in the startup message is used instead
    // see https://www.postgresql.org/docs/11/sasl-authentication.html#SASL-SCRAM-SHA-256 §53.3.1
    scramSession = scramClient.scramSession(this.username);

    return new ScramClientInitialMessage(scramSession.clientFirstMessage(), scramClient.getScramMechanism().getName());
  }


  /*
   * One or more server-challenge and client-response message will follow.
   * Each server-challenge is sent in an AuthenticationSASLContinue message,
   *   followed by a response from client in an SASLResponse message.
   * The particulars of the messages are mechanism specific.
   */
  public String receiveServerFirstMessage(ByteBuf in)  {
    String serverFirstMessage = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();

    ScramSession.ServerFirstProcessor serverFirstProcessor = null;
    try {
      serverFirstProcessor = scramSession.receiveServerFirstMessage(serverFirstMessage);
    } catch (ScramException e) {
      throw new UnsupportedOperationException(e);
    }

    clientFinalProcessor = serverFirstProcessor.clientFinalProcessor(password);

    return clientFinalProcessor.clientFinalMessage();
  }

  /*
   * Finally, when the authentication exchange is completed successfully,
   *   the server sends an AuthenticationSASLFinal message, followed immediately by an AuthenticationOk message.
   * The AuthenticationSASLFinal contains additional server-to-client data,
   *   whose content is particular to the selected authentication mechanism.
   * If the authentication mechanism doesn't use additional data that's sent at completion,
   *   the AuthenticationSASLFinal message is not sent
   */
  public void checkServerFinalMessage(ByteBuf in) {
    String serverFinalMessage = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();

    try {
      clientFinalProcessor.receiveServerFinalMessage(serverFinalMessage);
    } catch (ScramParseException | ScramServerErrorException | ScramInvalidServerSignatureException e) {
      throw new UnsupportedOperationException(e);
    }
  }
}
