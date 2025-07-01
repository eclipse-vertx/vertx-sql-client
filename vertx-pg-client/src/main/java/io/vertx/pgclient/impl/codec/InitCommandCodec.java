/*
 * Copyright (C) 2018 Julien Viet
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
package io.vertx.pgclient.impl.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.vertx.core.VertxException;
import io.vertx.pgclient.impl.PgDatabaseMetadata;
import io.vertx.pgclient.impl.PgSocketConnection;
import io.vertx.pgclient.impl.auth.scram.ScramAuthentication;
import io.vertx.pgclient.impl.auth.scram.ScramSession;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.impl.CommandResponse;
import io.vertx.sqlclient.internal.command.InitCommand;

class InitCommandCodec extends PgCommandCodec<Connection, InitCommand> {

  private PgEncoder encoder;
  private String encoding;
  private ScramSession scramSession;

  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(PgEncoder encoder) {
    this.encoder = encoder;
    encoder.writeStartupMessage(new StartupMessage(cmd.username(), cmd.database(), cmd.properties()));
  }

  @Override
  public void handleAuthenticationMD5Password(byte[] salt) {
    encoder.writePasswordMessage(new PasswordMessage(cmd.username(), cmd.password(), salt));
    encoder.flush();
  }

  @Override
  public void handleAuthenticationClearTextPassword() {
    encoder.writePasswordMessage(new PasswordMessage(cmd.username(), cmd.password(), null));
    encoder.flush();
  }

  @Override
  void handleAuthenticationSasl(ByteBuf in) {
    ScramAuthentication scramAuth = ScramAuthentication.INSTANCE;
    if (scramAuth == null) {
      // This will close the connection
      throw new VertxException("Scram authentication not supported, missing com.ongres.scram:scram-client on the class/module path");
    }
    scramSession = scramAuth.session(cmd.username(), cmd.password().toCharArray());
    encoder.writeScramClientInitialMessage(
        scramSession.createInitialSaslMessage(in, encoder.channelHandlerContext()));
    encoder.flush();
  }

  @Override
  void handleAuthenticationSaslContinue(ByteBuf in) {
    encoder.writeScramClientFinalMessage(new ScramClientFinalMessage(scramSession.receiveServerFirstMessage(in)));
    encoder.flush();
  }

  @Override
  void handleAuthenticationSaslFinal(ByteBuf in) {
    scramSession.checkServerFinalMessage(in);
  }

  @Override
  public void handleAuthenticationOk() {
//      handler.handle(Future.succeededFuture(conn));
//      handler = null;
  }

  @Override
  public void handleParameterStatus(String key, String value) {
    if(key.equals("client_encoding")) {
      encoding = value;
    }
    if(key.equals("server_version")) {
      ((PgSocketConnection)cmd.connection()).dbMetaData = new PgDatabaseMetadata(value);
    }
  }

  @Override
  public void handleBackendKeyData(int processId, int secretKey) {
    ((PgSocketConnection)cmd.connection()).processId = processId;
    ((PgSocketConnection)cmd.connection()).secretKey = secretKey;
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    decoder.fireCommandResponse(CommandResponse.failure(errorResponse.toException()));
  }

  @Override
  public void handleReadyForQuery() {
    // The final phase before returning the connection
    // We should make sure we are supporting only UTF8
    // https://www.postgresql.org/docs/9.5/static/multibyte.html#MULTIBYTE-CHARSET-SUPPORTED
    Charset cs = null;
    try {
      cs = Charset.forName(encoding);
    } catch (Exception ignore) {
    }
    CommandResponse<Connection> fut;
    if (cs == null || !cs.equals(StandardCharsets.UTF_8)) {
      fut = CommandResponse.failure(encoding + " is not supported in the client only UTF8");
    } else {
      fut = CommandResponse.success(cmd.connection());
    }
    decoder.fireCommandResponse(fut);
  }
}
