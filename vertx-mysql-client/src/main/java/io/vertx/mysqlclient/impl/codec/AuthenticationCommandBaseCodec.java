package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.util.RsaPublicKeyEncryptor;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

abstract class AuthenticationCommandBaseCodec<R, C extends CommandBase<R>> extends CommandCodec<R, C> {
  protected static final int NONCE_LENGTH = 20;
  protected static final int AUTH_SWITCH_REQUEST_STATUS_FLAG = 0xFE;

  protected static final int AUTH_MORE_DATA_STATUS_FLAG = 0x01;
  protected static final int AUTH_PUBLIC_KEY_REQUEST_FLAG = 0x02;
  protected static final int FAST_AUTH_STATUS_FLAG = 0x03;
  protected static final int FULL_AUTHENTICATION_STATUS_FLAG = 0x04;

  protected byte[] authPluginData;

  private boolean isWaitingForRsaPublicKey = false;

  AuthenticationCommandBaseCodec(C cmd) {
    super(cmd);
  }

  protected final void handleAuthMoreData(byte[] password, ByteBuf payload) {
    payload.skipBytes(1); // skip the status flag
    if (isWaitingForRsaPublicKey){
      String serverRsaPublicKey = readRestOfPacketString(payload, StandardCharsets.UTF_8);
      sendEncryptedPasswordWithServerRsaPublicKey(password, serverRsaPublicKey);
    } else {
      byte flag = payload.readByte();
      if (flag == FULL_AUTHENTICATION_STATUS_FLAG) {
        if (encoder.socketConnection.isSsl()) {
          // send the non-scrambled password directly since it's on a secure connection
          int nonScrambledPasswordPacketLength = password.length + 1;
          ByteBuf nonScrambledPasswordPacket = allocateBuffer(nonScrambledPasswordPacketLength + 4);
          nonScrambledPasswordPacket.writeMediumLE(nonScrambledPasswordPacketLength);
          nonScrambledPasswordPacket.writeByte(sequenceId);
          nonScrambledPasswordPacket.writeBytes(password);
          nonScrambledPasswordPacket.writeByte(0x00); // end with a 0x00
          sendNonSplitPacket(nonScrambledPasswordPacket);
        } else {
          // use server Public Key to encrypt password
          String serverRsaPublicKey = getServerPublicKey();
          if (serverRsaPublicKey == null) {
            // send a public key request
            isWaitingForRsaPublicKey = true;
            ByteBuf rsaPublicKeyRequest = allocateBuffer(5);
            rsaPublicKeyRequest.writeMediumLE(1);
            rsaPublicKeyRequest.writeByte(sequenceId);
            rsaPublicKeyRequest.writeByte(AUTH_PUBLIC_KEY_REQUEST_FLAG);
            sendNonSplitPacket(rsaPublicKeyRequest);
          } else {
            // send encrypted password
            sendEncryptedPasswordWithServerRsaPublicKey(password, serverRsaPublicKey);
          }
        }
      } else if (flag == FAST_AUTH_STATUS_FLAG) {
        // fast auth success
        return;
      } else {
        throw new UnsupportedOperationException("Unsupported flag for AuthMoreData : " + flag);
      }
    }
  }

  abstract protected String getServerPublicKey();

  protected final void sendEncryptedPasswordWithServerRsaPublicKey(byte[] password, String serverRsaPublicKeyContent) {
    byte[] encryptedPassword;
    try {
      byte[] passwordInput = Arrays.copyOf(password, password.length + 1); // need to append 0x00(NULL) to the password
      encryptedPassword = RsaPublicKeyEncryptor.encrypt(passwordInput, authPluginData, serverRsaPublicKeyContent);
    } catch (Exception e) {
      completionHandler.handle(CommandResponse.failure(e));
      return;
    }
    sendBytesAsPacket(encryptedPassword);
  }
}
