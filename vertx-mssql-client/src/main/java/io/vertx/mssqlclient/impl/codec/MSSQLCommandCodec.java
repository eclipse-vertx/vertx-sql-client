package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.MSSQLException;
import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.netty.buffer.ByteBuf;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_16LE;

abstract class MSSQLCommandCodec<R, C extends CommandBase<R>> {
  final C cmd;
  public Throwable failure;
  public R result;
  Handler<? super CommandResponse<R>> completionHandler;
  TdsMessageEncoder encoder;

  MSSQLCommandCodec(C cmd) {
    this.cmd = cmd;
  }

  void encode(TdsMessageEncoder encoder) {
    this.encoder = encoder;
  }

  void encodeMessage(MessageType type, MessageStatus status, int processId, Consumer<ByteBuf> payloadEncoder) {
    // FIXME split large message into packets
  }

  abstract void decodeMessage(TdsMessage message, TdsMessageEncoder encoder);

  void handleErrorToken(ByteBuf buffer) {
    // token value has been processed
    int length = buffer.readUnsignedShortLE();

    int number = buffer.readIntLE();
    byte state = buffer.readByte();
    byte severity = buffer.readByte();
    String message = readUnsignedShortLenVarChar(buffer);
    String serverName = readByteLenVarchar(buffer);
    String procedureName = readByteLenVarchar(buffer);
    int lineNumber = buffer.readIntLE();

    failure = new MSSQLException(number, state, severity, message, serverName, procedureName, lineNumber);
  }

  void handleDoneToken() {
    CommandResponse<R> resp;
    if (failure != null) {
      resp = CommandResponse.failure(failure);
    } else {
      resp = CommandResponse.success(result);
    }
    completionHandler.handle(resp);
  }

  protected String readByteLenVarchar(ByteBuf buffer) {
    int length = buffer.readUnsignedByte();
    return buffer.readCharSequence(length * 2, UTF_16LE).toString();
  }

  protected String readUnsignedShortLenVarChar(ByteBuf buffer) {
    int length = buffer.readUnsignedShortLE();
    return buffer.readCharSequence(length * 2, UTF_16LE).toString();
  }

  protected void writeUnsignedShortLenVarChar(ByteBuf buffer, String value) {
    buffer.writeShortLE(value.length() * 2);
    buffer.writeCharSequence(value, UTF_16LE);
  }
}
