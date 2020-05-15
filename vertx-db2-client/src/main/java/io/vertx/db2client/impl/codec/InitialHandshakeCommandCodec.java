/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.Future;
import io.vertx.db2client.DB2Exception;
import io.vertx.db2client.impl.command.InitialHandshakeCommand;
import io.vertx.db2client.impl.drda.CCSIDConstants;
import io.vertx.db2client.impl.drda.DRDAConnectRequest;
import io.vertx.db2client.impl.drda.DRDAConnectResponse;
import io.vertx.db2client.impl.drda.DRDAConnectResponse.RDBAccessData;
import io.vertx.db2client.impl.drda.DRDAConstants;
import io.vertx.db2client.impl.drda.SQLState;
import io.vertx.db2client.impl.drda.SqlCode;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;

class InitialHandshakeCommandCodec extends AuthenticationCommandBaseCodec<Connection, InitialHandshakeCommand> {

  private static enum ConnectionState {
    CONNECTING, AUTHENTICATING, CONNECTED, CONNECT_FAILED
  }

  private static final int TARGET_SECURITY_MEASURE = DRDAConstants.SECMEC_USRIDPWD;

  // TODO: @AGG may need to move this to connection level
  // Correlation Token of the source sent to the server in the accrdb.
  // It is saved like the prddta in case it is needed for a connect reflow.
  private byte[] correlationToken;

  private ConnectionState status = ConnectionState.CONNECTING;

  InitialHandshakeCommandCodec(InitialHandshakeCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    encoder.connMetadata.databaseName = cmd.database();
    encoder.socketConnection.closeHandler(h -> {
      if (status == ConnectionState.CONNECTING) {
        // Sometimes DB2 closes the connection when sending an invalid Database name.
        // -4499 = A fatal error occurred that resulted in a disconnect from the data
        // source.
        // 08001 = "The connection was unable to be established"
        System.out.println("@AGG error?");
//        cmd.fail(new DB2Exception("The connection was closed by the database server.", SqlCode.CONNECTION_REFUSED,
//            SQLState.AUTH_DATABASE_CONNECTION_REFUSED));
      }
    });

    DRDAConnectRequest connectRequest = encoder.getOrCreateRequest();
    connectRequest.buildEXCSAT(DRDAConstants.EXTNAM, // externalName,
        0x0A, // targetAgent,
        DRDAConstants.TARGET_SQL_AM, // targetSqlam,
        0x0C, // targetRdb,
        0x0A, // TARGET_SECURITY_MEASURE, //targetSecmgr,
        0, // targetCmntcpip,
        0, // targetCmnappc, (not used)
        0, // targetXamgr,
        0, // targetSyncptmgr,
        0, // targetRsyncmgr,
        CCSIDConstants.TARGET_UNICODE_MGR // targetUnicodemgr
    );
    connectRequest.buildACCSEC(TARGET_SECURITY_MEASURE, this.cmd.database(), null);
    correlationToken = connectRequest.getCorrelationToken(encoder.socketConnection.socket().localAddress().port());
    connectRequest.buildSECCHK(TARGET_SECURITY_MEASURE, cmd.database(), cmd.username(), cmd.password(), null, // sectkn,
        null); // sectkn2
    connectRequest.buildACCRDB(cmd.database(), false, // readOnly,
        correlationToken, DRDAConstants.SYSTEM_ASC);
    connectRequest.completeCommand();

//    encoder.writePacket();
    //encoder.sendPacket();
    //sendPacket(packet, lenOfPayload);
    System.out.println("@AGG encode complete");
    cmd.handler.handle(Future.succeededFuture(cmd.connection()));
//    completionHandler.handle(CommandResponse.success(cmd.connection()));
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    System.out.println("@AGG decode handshake");
    DRDAConnectResponse response = new DRDAConnectResponse(payload, encoder.connMetadata);
    response.readExchangeServerAttributes();
    // readAccessSecurity can throw a DB2Exception if there are problems connecting.
    // In that case, we want to catch that exception and
    // make sure to set the status to something other than ST_CONNECTING so we don't
    // try to complete the result twice (when we hit encode)
    try {
      response.readAccessSecurity(TARGET_SECURITY_MEASURE);
    } catch (DB2Exception de) {
      status = ConnectionState.CONNECT_FAILED;
      throw de;
    }
    status = ConnectionState.AUTHENTICATING;
    response.readSecurityCheck();
    RDBAccessData accData = response.readAccessDatabase();
    if (accData.correlationToken != null) {
      correlationToken = accData.correlationToken;
    }
    status = ConnectionState.CONNECTED;
    completionHandler.handle(CommandResponse.success(cmd.connection()));
  }

}
