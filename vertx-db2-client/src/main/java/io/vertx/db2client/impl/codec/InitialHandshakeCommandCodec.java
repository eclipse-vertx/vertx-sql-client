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

import java.sql.SQLException;

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.command.InitialHandshakeCommand;
import io.vertx.db2client.impl.drda.CCSIDManager;
import io.vertx.db2client.impl.drda.DRDAConnectRequest;
import io.vertx.db2client.impl.drda.DRDAConnectResponse;
import io.vertx.db2client.impl.drda.DRDAConnectResponse.RDBAccessData;
import io.vertx.db2client.impl.drda.DRDAConstants;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;

class InitialHandshakeCommandCodec extends AuthenticationCommandBaseCodec<Connection, InitialHandshakeCommand> {

    private static final int ST_CONNECTING = 0;
    private static final int ST_AUTHENTICATING = 1;
    private static final int ST_CONNECTED = 2;
    
    private static final int TARGET_SECURITY_MEASURE = DRDAConstants.SECMEC_USRIDPWD;
    
    // TODO: May need to move this to a higher scope?
    private final CCSIDManager ccsidManager = new CCSIDManager();
    
    // TODO: @AGG may need to move this to connection level
    // Correlation Token of the source sent to the server in the accrdb.
    // It is saved like the prddta in case it is needed for a connect reflow.
    private byte[] correlationToken;
    
    private int status = ST_CONNECTING;

    InitialHandshakeCommandCodec(InitialHandshakeCommand cmd) {
        super(cmd);
    }

    @Override
    void encode(DB2Encoder encoder) {
        super.encode(encoder);
        sendInitialHandshake();
    }

    @Override
    void decodePayload(ByteBuf payload, int payloadLength) {
        DRDAConnectResponse response = new DRDAConnectResponse(payload, ccsidManager);
        try {
            switch (status) {
            case ST_CONNECTING:
                response.readExchangeServerAttributes();
                response.readAccessSecurity(TARGET_SECURITY_MEASURE);
                status = ST_AUTHENTICATING;
                ByteBuf packet = allocateBuffer();
                int packetStartIdx = packet.writerIndex();
                DRDAConnectRequest securityCheck = new DRDAConnectRequest(packet, ccsidManager);
                correlationToken = securityCheck.getCorrelationToken(encoder.socketConnection.socket().localAddress().port());
                securityCheck.buildSECCHK(TARGET_SECURITY_MEASURE,
                        cmd.database(),
                        cmd.username(),
                        cmd.password(),
                        null, //sectkn, 
                        null); //sectkn2
                securityCheck.buildACCRDB(cmd.database(), 
                        false, //readOnly, 
                        correlationToken,
                        DRDAConstants.SYSTEM_ASC);
                securityCheck.completeCommand();
                int lenOfPayload = packet.writerIndex() - packetStartIdx;
                sendPacket(packet, lenOfPayload);
                return;
            case ST_AUTHENTICATING:
                response.readSecurityCheck();
                RDBAccessData accData = response.readAccessDatabase();
                if (accData.correlationToken != null)
                    correlationToken = accData.correlationToken;
                status = ST_CONNECTED;
                completionHandler.handle(CommandResponse.success(cmd.connection()));
                return;
            default: 
                throw new IllegalStateException("Unknown state: " + status);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            completionHandler.handle(CommandResponse.failure(t));
        }
    }
    
    private void sendInitialHandshake() {
        ByteBuf packet = allocateBuffer();
        int packetStartIdx = packet.writerIndex();
        DRDAConnectRequest cmd = new DRDAConnectRequest(packet, ccsidManager);
        try {
            cmd.buildEXCSAT(DRDAConstants.EXTNAM, // externalName,
                    0x07, // 0x0A, // targetAgent,
                    DRDAConstants.TARGET_SQL_AM, // targetSqlam,
                    0x0C, // targetRdb,
                    TARGET_SECURITY_MEASURE, //targetSecmgr,
                    0, // targetCmntcpip,
                    0, // targetCmnappc, (not used)
                    0, // targetXamgr,
                    0, // targetSyncptmgr,
                    0, // targetRsyncmgr,
                    CCSIDManager.TARGET_UNICODE_MGR // targetUnicodemgr
            );
            cmd.buildACCSEC(TARGET_SECURITY_MEASURE, this.cmd.database(), null);
            cmd.completeCommand();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int lenOfPayload = packet.writerIndex() - packetStartIdx;
        sendPacket(packet, lenOfPayload);
    }

}
