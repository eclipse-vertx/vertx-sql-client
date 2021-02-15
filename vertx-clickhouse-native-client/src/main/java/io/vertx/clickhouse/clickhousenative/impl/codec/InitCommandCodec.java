package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseServerException;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.InitCommand;

public class InitCommandCodec extends ClickhouseNativeCommandCodec<Connection, InitCommand> {
  private static final Logger LOG = LoggerFactory.getLogger(InitCommandCodec.class);

  //server-related info
  private Integer packetType;
  private String productName;
  private Integer major;
  private Integer minor;
  private Integer revision;
  private String timezone;
  private String displayName;
  private Integer patchVersion;

  //server-error related info
  private Integer code;
  private String name;
  private String message;
  private String stacktrace;

  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);

    ByteBuf buf = allocateBuffer();
    ByteBufUtils.writeULeb128(ClientPacketTypes.HELLO, buf);
    String clientId = "ClickHouse " + cmd.properties()
      .getOrDefault(ClickhouseConstants.CLIENT_NAME, "vertx-sql");
    ByteBufUtils.writePascalString(clientId, buf);
    ByteBufUtils.writeULeb128(20, buf);
    ByteBufUtils.writeULeb128(10, buf);
    ByteBufUtils.writeULeb128(54441, buf);
    ByteBufUtils.writePascalString(cmd.database(), buf);
    ByteBufUtils.writePascalString(cmd.username(), buf);
    ByteBufUtils.writePascalString(cmd.password(), buf);
    encoder.chctx().writeAndFlush(buf, encoder.chctx().voidPromise());
    LOG.info("sent hello packet");
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
    if (packetType == null) {
      packetType = ByteBufUtils.readULeb128(in);
      LOG.info("packet type: " + packetType);
      if (packetType == null) {
        return;
      }
    }
    if (packetType == ServerPacketTypes.HELLO) {
      productName = ByteBufUtils.readPascalString(in);
      if (productName == null) {
        return;
      }
      if (major == null) {
        major = ByteBufUtils.readULeb128(in);
        if (major == null) {
          return;
        }
      }
      if (minor == null) {
        minor = ByteBufUtils.readULeb128(in);
        if (minor == null) {
          return;
        }
      }
      if (revision == null) {
        revision = ByteBufUtils.readULeb128(in);
        if (revision == null) {
          return;
        }
      }
      if (timezone == null && revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_SERVER_TIMEZONE) {
        timezone = ByteBufUtils.readPascalString(in);
        if (timezone == null) {
          return;
        }
      }
      if (displayName == null && revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_SERVER_DISPLAY_NAME ) {
        displayName = ByteBufUtils.readPascalString(in);
        if (displayName == null) {
          return;
        }
      }
      patchVersion = revision;
      if (revision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_VERSION_PATCH) {
        patchVersion = ByteBufUtils.readULeb128(in);
        if (patchVersion == null) {
          return;
        }
      }
      ClickhouseNativeDatabaseMetadata md = new ClickhouseNativeDatabaseMetadata(productName,
        String.format("%d.%d.%d", major, minor, revision),
        major, minor, revision, patchVersion, displayName, timezone);
      encoder.getConn().setDatabaseMetadata(md);
      LOG.info("decode: " + md);
      completionHandler.handle(CommandResponse.success(null));
    } else if (packetType == ServerPacketTypes.EXCEPTION) {
      if (code == null) {
        if (in.readableBytes() >= 4) {
          code = in.readIntLE();
        } else {
          return;
        }
      }
      if (name == null) {
        name = ByteBufUtils.readPascalString(in);
        if (name == null) {
          return;
        }
      }
      if (message == null) {
        message = ByteBufUtils.readPascalString(in);
        if (message == null) {
          return;
        }
      }
      if (stacktrace == null) {
        stacktrace = ByteBufUtils.readPascalString(in);
        if (stacktrace == null) {
          return;
        }
      }
      completionHandler.handle(CommandResponse.failure(new ClickhouseServerException(code, name, message, stacktrace)));
    } else {
      String msg = "unknown packet type: " + packetType;
      LOG.error(msg);
      completionHandler.handle(CommandResponse.failure(new RuntimeException(msg)));
    }
  }
}
