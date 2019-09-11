package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.command.ChangeUserCommand;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.mysqlclient.impl.util.CachingSha2Authenticator;
import io.vertx.mysqlclient.impl.util.Native41Authenticator;
import io.vertx.mysqlclient.impl.util.RsaPublicKeyEncryptor;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static io.vertx.mysqlclient.impl.codec.CapabilitiesFlag.*;
import static io.vertx.mysqlclient.impl.codec.Packets.*;

class ChangeUserCommandCodec extends CommandCodec<Void, ChangeUserCommand> {
  private static final int AUTH_SWITCH_REQUEST_STATUS_FLAG = 0xFE;
  private static final int NONCE_LENGTH = 20;

  private static final int AUTH_MORE_DATA_STATUS_FLAG = 0x01;
  private static final int AUTH_PUBLIC_KEY_REQUEST_FLAG = 0x02;
  private static final int FAST_AUTH_STATUS_FLAG = 0x03;
  private static final int FULL_AUTHENTICATION_STATUS_FLAG = 0x04;

  private boolean isWaitingForRsaPublicKey = false; // FIXME remove this later
  private byte[] authData;

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
      case AUTH_SWITCH_REQUEST_STATUS_FLAG:
        // Protocol::AuthSwitchRequest
        payload.skipBytes(1); // status flag, always 0xFE
        String pluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.UTF_8);
        authData = new byte[NONCE_LENGTH];
        payload.readBytes(authData);
        byte[] scrambledPassword;
        switch (pluginName) {
          case "mysql_native_password":
            scrambledPassword = Native41Authenticator.encode(cmd.password().getBytes(), authData);
            break;
          case "caching_sha2_password":
            scrambledPassword = CachingSha2Authenticator.encode(cmd.password().getBytes(), authData);
            break;
          default:
            completionHandler.handle(CommandResponse.failure(new UnsupportedOperationException("Unsupported authentication method: " + pluginName)));
            return;
        }
        int scrambledPasswordPacketLength = scrambledPassword.length;
        ByteBuf scrambledPasswordPacket = allocateBuffer(scrambledPasswordPacketLength + 4);
        scrambledPasswordPacket.writeMediumLE(scrambledPasswordPacketLength);
        scrambledPasswordPacket.writeByte(this.sequenceId);
        scrambledPasswordPacket.writeBytes(scrambledPassword);
        sendNonSplitPacket(scrambledPasswordPacket);
        break;
      case AUTH_MORE_DATA_STATUS_FLAG:
        payload.skipBytes(1); // skip the status flag
        if (isWaitingForRsaPublicKey){
          String serverRsaPublicKey = readRestOfPacketString(payload, StandardCharsets.UTF_8);
          sendEncryptedPasswordWithServerRsaPublicKey(serverRsaPublicKey);
        } else {
          byte flag = payload.readByte();
          if (flag == FULL_AUTHENTICATION_STATUS_FLAG) {
            if (encoder.socketConnection.isSsl()) {
              // send the non-scrambled password directly since it's on a secure connection
              byte[] password = cmd.password().getBytes();
              int nonScrambledPasswordPacketLength = password.length + 1;
              ByteBuf nonScrambledPasswordPacket = allocateBuffer(nonScrambledPasswordPacketLength + 4);
              nonScrambledPasswordPacket.writeMediumLE(nonScrambledPasswordPacketLength);
              nonScrambledPasswordPacket.writeByte(this.sequenceId);
              nonScrambledPasswordPacket.writeBytes(password);
              nonScrambledPasswordPacket.writeByte(0x00); // end with a 0x00
              sendNonSplitPacket(nonScrambledPasswordPacket);
            } else {
              // use server Public Key to encrypt password
              String serverRsaPublicKey = cmd.serverRsaPublicKey();
              if (serverRsaPublicKey == null) {
                // send a public key request
                isWaitingForRsaPublicKey = true;
                ByteBuf rsaPublicKeyRequest = allocateBuffer(5);
                rsaPublicKeyRequest.writeMediumLE(1);
                rsaPublicKeyRequest.writeByte(this.sequenceId);
                rsaPublicKeyRequest.writeByte(AUTH_PUBLIC_KEY_REQUEST_FLAG);
                sendNonSplitPacket(rsaPublicKeyRequest);
              } else {
                // send encrypted password
                sendEncryptedPasswordWithServerRsaPublicKey(serverRsaPublicKey);
              }
            }
          } else if (flag == FAST_AUTH_STATUS_FLAG) {
            // fast auth success
            return;
          } else {
            throw new UnsupportedOperationException("Unsupported flag for AuthMoreData : " + flag);
          }
        }
        break;
      case OK_PACKET_HEADER:
        completionHandler.handle(CommandResponse.success(null));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
    }
  }

  private void sendEncryptedPasswordWithServerRsaPublicKey(String serverRsaPublicKeyContent) {
    byte[] encryptedPassword;
    try {
      byte[] password = cmd.password().getBytes();
      byte[] passwordInput = Arrays.copyOf(password, password.length + 1); // need to append 0x00(NULL) to the password
      encryptedPassword = RsaPublicKeyEncryptor.encrypt(passwordInput, authData, serverRsaPublicKeyContent);
    } catch (Exception e) {
      completionHandler.handle(CommandResponse.failure(e));
      return;
    }

    ByteBuf buf = allocateBuffer(encryptedPassword.length + 4);
    buf.writeMediumLE(encryptedPassword.length);
    buf.writeByte(sequenceId);
    buf.writeBytes(encryptedPassword);
    sendNonSplitPacket(buf);
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
        if (nonAttributePropertyKeys.contains(attribute.getKey())) {
          // we store it in the properties but it's not an attribute
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
