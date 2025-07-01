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
import io.vertx.db2client.impl.command.PingCommand;
import io.vertx.db2client.impl.drda.CCSIDConstants;
import io.vertx.db2client.impl.drda.DRDAConnectRequest;
import io.vertx.db2client.impl.drda.DRDAConnectResponse;
import io.vertx.db2client.impl.drda.DRDAConstants;
import io.vertx.db2client.impl.drda.ConnectionMetaData;
import io.vertx.sqlclient.impl.CommandResponse;

class PingCommandCodec extends CommandCodec<Void, PingCommand> {

  // Use an isolated metadata instance since we will flow a new EXCSAT
  private final ConnectionMetaData md = new ConnectionMetaData();

  PingCommandCodec(PingCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    sendPingRequest();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    DRDAConnectResponse response = new DRDAConnectResponse(payload, md);
    response.readExchangeServerAttributes();
    completionHandler.handle(CommandResponse.success(null));
    return;
  }

  private void sendPingRequest() {
    ByteBuf packet = allocateBuffer();
    int packetStartIdx = packet.writerIndex();
    DRDAConnectRequest cmd = new DRDAConnectRequest(packet, md);
    cmd.buildEXCSAT(DRDAConstants.EXTNAM, // externalName,
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
    cmd.completeCommand();

    int lenOfPayload = packet.writerIndex() - packetStartIdx;
    sendPacket(packet, lenOfPayload);
  }
}
