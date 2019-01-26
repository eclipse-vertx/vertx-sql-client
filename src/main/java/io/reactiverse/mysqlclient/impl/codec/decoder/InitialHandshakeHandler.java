package io.reactiverse.mysqlclient.impl.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.reactiverse.mysqlclient.impl.MySQLSocketConnection;
import io.reactiverse.mysqlclient.impl.CapabilitiesNegotiator;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketDecoder;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CapabilitiesFlag;
import io.reactiverse.mysqlclient.impl.protocol.backend.InitialHandshakePacket;
import io.reactiverse.mysqlclient.impl.protocol.frontend.HandshakeResponse;
import io.reactiverse.mysqlclient.impl.util.BufferUtils;
import io.vertx.core.Future;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.reactiverse.mysqlclient.impl.MySQLSocketConnection.State.*;

public class InitialHandshakeHandler extends MySQLPacketDecoder {
  private static final int AUTH_PLUGIN_DATA_PART1_LENGTH = 8;
  private static final int SCRAMBLE_LENGTH = 20;

  private final MySQLPacketEncoder packetEncoder;

  private final String username;
  private final String password;
  private final String database;
  private final boolean ssl;
  private final Future<Void> connectionPhaseFuture;

  public InitialHandshakeHandler(Charset charset,
                                 MySQLSocketConnection socketConnection,
                                 String username,
                                 String password,
                                 String database,
                                 boolean ssl,
                                 Future<Void> connectionPhaseFuture) {
    super(charset, socketConnection);
    this.packetEncoder = socketConnection.packetEncoder();

    // apply connect options
    this.username = username;
    this.password = password;
    this.database = database;
    this.ssl = ssl;

    this.connectionPhaseFuture = connectionPhaseFuture;
  }

  @Override
  protected void decodePayload(ChannelHandlerContext ctx, ByteBuf payload, int payloadLength, int sequenceId, List<Object> out) {
    short protocolVersion = payload.readUnsignedByte();

    String serverVersion = BufferUtils.readNullTerminatedString(payload, StandardCharsets.US_ASCII);
    long connectionId = payload.readUnsignedIntLE();

    // read first part of scramble
    byte[] scramble = new byte[SCRAMBLE_LENGTH];
    payload.readBytes(scramble, 0, AUTH_PLUGIN_DATA_PART1_LENGTH);

    //filler
    payload.readByte();

    // read lower 2 bytes of Capabilities flags
    int serverCapabilitiesFlags = payload.readUnsignedShortLE();

    short characterSet = payload.readUnsignedByte();

    int statusFlags = payload.readUnsignedShortLE();

    // read upper 2 bytes of Capabilities flags
    int capabilityFlagsUpper = payload.readUnsignedShortLE();
    serverCapabilitiesFlags |= (capabilityFlagsUpper << 16);

    // length of the combined auth_plugin_data (scramble)
    short lenOfAuthPluginData;
    boolean isClientPluginAuthSupported = (serverCapabilitiesFlags & CapabilitiesFlag.CLIENT_PLUGIN_AUTH) != 0;
    if (isClientPluginAuthSupported) {
      lenOfAuthPluginData = payload.readUnsignedByte();
    } else {
      payload.readerIndex(payload.readerIndex() + 1);
      lenOfAuthPluginData = 0;
    }

    // 10 bytes reserved
    payload.readerIndex(payload.readerIndex() + 10);

    // Rest of the plugin provided data
    payload.readBytes(scramble, AUTH_PLUGIN_DATA_PART1_LENGTH, SCRAMBLE_LENGTH - AUTH_PLUGIN_DATA_PART1_LENGTH);
    // 20 byte long scramble end with a '/0' character
    payload.readByte();

    String authPluginName = null;
    if (isClientPluginAuthSupported) {
      authPluginName = BufferUtils.readNullTerminatedString(payload, StandardCharsets.US_ASCII);
    }

    //TODO we may not need an extra object here?(inline)
    InitialHandshakePacket initialHandshakePacket = new InitialHandshakePacket(serverVersion,
      connectionId,
      serverCapabilitiesFlags,
      characterSet,
      statusFlags,
      scramble,
      authPluginName
    );

    if (ssl) {
      //TODO ssl
    } else {
      int negotiatedCapabilities = CapabilitiesNegotiator.negotiate(initialHandshakePacket.getServerCapabilitiesFlags(), database);
      String authMethodName = initialHandshakePacket.getAuthMethodName();
      byte[] serverScramble = initialHandshakePacket.getScramble();

      HandshakeResponse handshakeResponse = new HandshakeResponse(username, charset, password, database, serverScramble, negotiatedCapabilities, authMethodName, null);

      Future<Void> authenticationFuture = Future.future();
      authenticationFuture.setHandler(ar -> {
        if (ar.succeeded()) {
          connectionPhaseFuture.complete();
        } else {
          connectionPhaseFuture.fail(ar.cause());
        }
      });

      // insert authentication handler
      ctx.pipeline().addBefore("handler", "authenticationHandler", new AuthenticationHandler(charset, socketConnection, authenticationFuture));

      packetEncoder.writeHandshakeResponseMessage(handshakeResponse, ctx.newPromise().addListener(future -> {
        socketConnection.switchToState(AUTHENTICATING);
        ctx.pipeline().remove("handshakeHandler");
      }));
    }
  }
}
