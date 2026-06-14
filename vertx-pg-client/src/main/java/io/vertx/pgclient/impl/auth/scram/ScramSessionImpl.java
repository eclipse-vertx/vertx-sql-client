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

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import com.ongres.scram.client.ChannelBindingException;
import com.ongres.scram.client.ChannelBindingPolicy;
import com.ongres.scram.client.ScramClient;
import com.ongres.scram.common.StringPreparation;
import com.ongres.scram.common.exception.ScramInvalidServerSignatureException;
import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.exception.ScramRuntimeException;
import com.ongres.scram.common.exception.ScramServerErrorException;
import com.ongres.scram.common.util.TlsServerEndpoint;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.pgclient.ChannelBinding;
import io.vertx.pgclient.impl.codec.ScramClientInitialMessage;
import io.vertx.pgclient.impl.util.Util;

public class ScramSessionImpl implements ScramSession {

  private static final Logger logger = LoggerFactory.getLogger(ScramSessionImpl.class);

  private final String username;
  private final char[] password;
  private final ChannelBindingPolicy channelBinding;
  private ScramClient scramClient;

  public ScramSessionImpl(String username, char[] password, ChannelBinding channelBinding) {
    this.username = username;
    this.password = password;
    this.channelBinding = getChannelBindingPolicy(channelBinding);
  }

  /*
   * The client selects one of the supported mechanisms from the list,
   * and sends a SASLInitialResponse message to the server.
   *
   * The message includes the name of the selected mechanism, and
   * an optional Initial Client Response, if the selected mechanism uses that.
   */
  public ScramClientInitialMessage createInitialSaslMessage(ByteBuf in, ChannelHandlerContext ctx) {
    List<String> mechanisms = new ArrayList<>();

    while (0 != in.getByte(in.readerIndex())) {
      String mechanism = Util.readCStringUTF8(in);
      mechanisms.add(mechanism);
    }

    logger.debug("Advertised SCRAM mechanisms: " + mechanisms);

    this.scramClient = ScramClient.builder()
        .advertisedMechanisms(mechanisms)
        .username(username) // ignored by the server, use startup message
        .password(password)
        .stringPreparation(StringPreparation.POSTGRESQL_PREPARATION)
        .channelBindingPolicy(channelBinding)
        .channelBinding(TlsServerEndpoint.TLS_SERVER_END_POINT, extractChannelBindingData(ctx))
        .build();

    logger.debug("Selected SCRAM mechanism: " + scramClient.getScramMechanism().getName());

    String clientFirstMessage = scramClient.clientFirstMessage().toString();
    logger.trace("SASLInitialResponse: " + clientFirstMessage);

    return new ScramClientInitialMessage(clientFirstMessage,
        scramClient.getScramMechanism().getName());
  }

  /*
   * One or more server-challenge and client-response message will follow.
   * Each server-challenge is sent in an AuthenticationSASLContinue message,
   * followed by a response from client in an SASLResponse message.
   * The particulars of the messages are mechanism specific.
   */
  public String receiveServerFirstMessage(ByteBuf in) {
    String serverFirstMessage = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
    logger.trace("AuthenticationSASLContinue: " + serverFirstMessage);

    try {
      scramClient.serverFirstMessage(serverFirstMessage);
    } catch (ScramParseException e) {
      throw new ScramRuntimeException(e.getMessage(), e);
    }

    String clientFinalMessage = scramClient.clientFinalMessage().toString();
    logger.trace("SASLResponse: " + clientFinalMessage);

    return clientFinalMessage;
  }

  /*
   * Finally, when the authentication exchange is completed successfully,
   * the server sends an AuthenticationSASLFinal message, followed immediately by an AuthenticationOk message.
   * The AuthenticationSASLFinal contains additional server-to-client data,
   * whose content is particular to the selected authentication mechanism.
   * If the authentication mechanism doesn't use additional data that's sent at completion,
   * the AuthenticationSASLFinal message is not sent
   */
  public void checkServerFinalMessage(ByteBuf in) {
    String serverFinalMessage = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
    logger.trace("AuthenticationSASLFinal: " + serverFinalMessage);

    try {
      scramClient.serverFinalMessage(serverFinalMessage);
    } catch (ScramParseException | ScramServerErrorException | ScramInvalidServerSignatureException e) {
      throw new ScramRuntimeException(e.getMessage(), e);
    }
  }

  private byte[] extractChannelBindingData(ChannelHandlerContext ctx) {
    SslHandler sslHandler = ctx.channel().pipeline().get(SslHandler.class);
    if (sslHandler != null) {
      SSLSession sslSession = sslHandler.engine().getSession();
      if (sslSession != null && sslSession.isValid()) {
        try {
          // Get the certificate chain from the session
          Certificate[] certificates = sslSession.getPeerCertificates();
          if (certificates != null && certificates.length > 0) {
            Certificate peerCert = certificates[0]; // First certificate is the peer's certificate
            if (peerCert instanceof X509Certificate) {
              X509Certificate cert = (X509Certificate) peerCert;
              return TlsServerEndpoint.getChannelBindingHash(cert);
            }
          }
        } catch (CertificateEncodingException | SSLException | NoSuchAlgorithmException e) {
          logger.debug("Error extracting channel binding data", e);
          if (channelBinding == ChannelBindingPolicy.REQUIRE) {
            throw new ChannelBindingException(e.getMessage());
          }
        }
      }
    }
    return new byte[0]; // handle as no channel binding available
  }

  private static ChannelBindingPolicy getChannelBindingPolicy(ChannelBinding channelBinding) {
    logger.debug("ChannelBinding: " + channelBinding);
    switch (channelBinding) {
      case DISABLE:
        return ChannelBindingPolicy.DISABLE;
      case PREFER:
        return ChannelBindingPolicy.ALLOW;
      case REQUIRE:
        return ChannelBindingPolicy.REQUIRE;
      default:
        throw new IllegalArgumentException("Invalid channel binding value");
    }
  }
}
