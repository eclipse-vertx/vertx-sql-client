package io.vertx.db2client.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.drda.CCSIDManager;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.db2client.impl.drda.SectionManager;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

class CloseConnectionCommandCodec extends CommandCodec<Void, CloseConnectionCommand> {
    
    private final CCSIDManager cm = new CCSIDManager();

  CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    System.out.println("@AGG encode close");
    // TODO: @AGG should we also close statements/queries here?
    try {
        ByteBuf packet = allocateBuffer();
        DRDAQueryRequest closeCursor = new DRDAQueryRequest(packet, cm);
        closeCursor.buildCLSQRY(SectionManager.INSTANCE.getDynamicSection(), encoder.socketConnection.database(), 1); // @AGG guessing 1 on queryInstanceId
        closeCursor.buildRDBCMM();
        closeCursor.completeCommand();
        sendNonSplitPacket(packet);
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
      System.out.println("@AGG disconnect reply");
      // @AGG closes failing due to magic bit 'd0' never being set
      try {
          DRDAQueryResponse closeCursor = new DRDAQueryResponse(payload, cm);
          closeCursor.readCursorClose();
          closeCursor.readLocalCommit();
      } catch (Exception e) {
          payload.clear();
          e.printStackTrace();
      }
  }
}
