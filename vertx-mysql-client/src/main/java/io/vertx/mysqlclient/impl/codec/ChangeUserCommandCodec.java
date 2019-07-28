package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.command.ChangeUserCommand;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.vertx.mysqlclient.impl.codec.CapabilitiesFlag.*;
import static io.vertx.mysqlclient.impl.codec.Packets.*;

class ChangeUserCommandCodec extends CommandCodec<Void, ChangeUserCommand> {
  ChangeUserCommandCodec(ChangeUserCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendChangeUserCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
    int header = payload.getUnsignedByte(payload.readerIndex());
    switch (header) {
      case 0xFE:
        String pluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);
        if (pluginName.equals("caching_sha2_password")) {
          // TODO support different auth methods later
          completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("unsupported authentication method: " + pluginName)));
          return;
        }
        byte[] scramble = new byte[20];
        payload.readBytes(scramble);
        byte[] scrambledPassword = Native41Authenticator.encode(cmd.password(), StandardCharsets.UTF_8, scramble);
        sendAuthSwitchResponse(scrambledPassword);
        break;
      case OK_PACKET_HEADER:
        completionHandler.handle(CommandResponse.success(null));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
    }
  }

  private void sendChangeUserCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_CHANGE_USER);
    BufferUtils.writeNullTerminatedString(packet, cmd.username(), StandardCharsets.UTF_8);
    String password = cmd.password();
    if (password.isEmpty()) {
      packet.writeByte(0);
    } else {
      packet.writeByte(password.length());
      packet.writeCharSequence(password, StandardCharsets.UTF_8);
    }
    BufferUtils.writeNullTerminatedString(packet, cmd.database(), StandardCharsets.UTF_8);
    MySQLCollation collation = cmd.collation();
    int collationId = collation.collationId();
    encoder.charset = Charset.forName(collation.mappedJavaCharsetName());
    packet.writeShortLE(collationId);

    if ((encoder.clientCapabilitiesFlag & CLIENT_PLUGIN_AUTH) != 0) {
      BufferUtils.writeNullTerminatedString(packet, "mysql_native_password", StandardCharsets.UTF_8);
    }
    Map<String, String> clientConnectionAttributes = cmd.connectionAttributes();
    if (clientConnectionAttributes != null && !clientConnectionAttributes.isEmpty()) {
      encoder.clientCapabilitiesFlag |= CLIENT_CONNECT_ATTRS;
      ByteBuf kv = encoder.chctx.alloc().ioBuffer();
      for (Map.Entry<String, String> attribute : clientConnectionAttributes.entrySet()) {
        if (attribute.getKey().equals("collation")) {
          // we store the collation in the properties but it's not an attribute
        } else {
          BufferUtils.writeLengthEncodedString(kv, attribute.getKey(), StandardCharsets.UTF_8);
          BufferUtils.writeLengthEncodedString(kv, attribute.getValue(), StandardCharsets.UTF_8);
        }
      }
      BufferUtils.writeLengthEncodedInteger(packet, kv.readableBytes());
      packet.writeBytes(kv);
    }

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    sendPacket(packet, lenOfPayload);
  }

  private void sendAuthSwitchResponse(byte[] responseData) {
    int payloadLength = responseData.length;
    ByteBuf packet = allocateBuffer(payloadLength + 4);
    // encode packet header
    packet.writeMediumLE(payloadLength);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeBytes(responseData);

    sendNonSplitPacket(packet);
  }
}
