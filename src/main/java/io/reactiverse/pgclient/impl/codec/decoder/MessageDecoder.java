/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.reactiverse.pgclient.impl.codec.decoder;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.reactiverse.pgclient.impl.CommandBase;
import io.reactiverse.pgclient.impl.CommandResponse;
import io.reactiverse.pgclient.impl.QueryCommandBase;
import io.reactiverse.pgclient.impl.codec.ColumnDesc;
import io.reactiverse.pgclient.impl.codec.DataFormat;
import io.reactiverse.pgclient.impl.codec.DataType;
import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.reactiverse.pgclient.impl.codec.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import io.reactiverse.pgclient.impl.codec.decoder.type.AuthenticationType;
import io.reactiverse.pgclient.impl.codec.decoder.type.ErrorOrNoticeType;
import io.reactiverse.pgclient.impl.codec.decoder.type.MessageType;
import io.vertx.core.Handler;

import java.util.Deque;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageDecoder extends ChannelInboundHandlerAdapter {

  private final Deque<CommandBase<?>> inflight;
  private final ByteBufAllocator alloc;
  private Handler<? super CommandResponse<?>> commandResponseHandler;
  private Handler<NoticeResponse> noticeHandler;

  private ByteBuf in;

  public MessageDecoder(Deque<CommandBase<?>> inflight, ByteBufAllocator alloc) {
    this.inflight = inflight;
    this.alloc = alloc;
  }

  public void run(CommandBase<?> cmd) {
    cmd.completionHandler = commandResponseHandler;
    cmd.noticeHandler = noticeHandler;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    commandResponseHandler = ctx::fireChannelRead;
    noticeHandler = ctx::fireChannelRead;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buff = (ByteBuf) msg;
    if (in == null) {
      in = buff;
    } else {
      CompositeByteBuf composite;
      if (in instanceof CompositeByteBuf) {
        composite = (CompositeByteBuf) in;
      } else {
        composite = alloc.compositeBuffer();
        composite.addComponent(true, in);
        in = composite;
      }
      composite.addComponent(true, buff);
    }
    while (true) {
      int available = in.readableBytes();
      if (available < 5) {
        break;
      }
      int beginIdx = in.readerIndex();
      int length = in.getInt(beginIdx + 1);
      if (length + 1 > available) {
        break;
      }
      byte id = in.getByte(beginIdx);
      int endIdx = beginIdx + length + 1;
      final int writerIndex = in.writerIndex();
      try {
        in.setIndex(beginIdx + 5, endIdx);
        switch (id) {
          case MessageType.READY_FOR_QUERY: {
            decodeReadyForQuery(in);
            break;
          }
          case MessageType.DATA_ROW: {
            decodeDataRow(in);
            break;
          }
          case MessageType.COMMAND_COMPLETE: {
            decodeCommandComplete(in);
            break;
          }
          case MessageType.BIND_COMPLETE: {
            decodeBindComplete();
            break;
          }
          default: {
            decodeMessage(ctx, id, in);
          }
        }
      } finally {
        in.setIndex(endIdx, writerIndex);
      }
    }
    if (in != null && !in.isReadable()) {
      in.release();
      in = null;
    }
  }

  private void decodeMessage(ChannelHandlerContext ctx, byte id, ByteBuf in) {
    switch (id) {
      case MessageType.ROW_DESCRIPTION: {
        decodeRowDescription(in);
        break;
      }
      case MessageType.ERROR_RESPONSE: {
        decodeError(in);
        break;
      }
      case MessageType.NOTICE_RESPONSE: {
        decodeNotice(in);
        break;
      }
      case MessageType.AUTHENTICATION: {
        decodeAuthentication(in);
        break;
      }
      case MessageType.EMPTY_QUERY_RESPONSE: {
        decodeEmptyQueryResponse();
        break;
      }
      case MessageType.PARSE_COMPLETE: {
        decodeParseComplete();
        break;
      }
      case MessageType.CLOSE_COMPLETE: {
        decodeCloseComplete();
        break;
      }
      case MessageType.NO_DATA: {
        decodeNoData();
        break;
      }
      case MessageType.PORTAL_SUSPENDED: {
        decodePortalSuspended();
        break;
      }
      case MessageType.PARAMETER_DESCRIPTION: {
        decodeParameterDescription(in);
        break;
      }
      case MessageType.PARAMETER_STATUS: {
        decodeParameterStatus(in);
        break;
      }
      case MessageType.BACKEND_KEY_DATA: {
        decodeBackendKeyData(in);
        break;
      }
      case MessageType.NOTIFICATION_RESPONSE: {
        decodeNotificationResponse(ctx, in);
        break;
      }
      default: {
        throw new UnsupportedOperationException();
      }
    }
  }

  private void decodePortalSuspended() {
    inflight.peek().handlePortalSuspended();
  }

  private void decodeCommandComplete(ByteBuf in) {
    int updated = processor.parse(in);
    inflight.peek().handleCommandComplete(updated);
  }

  private void decodeDataRow(ByteBuf in) {
    QueryCommandBase<?> cmd = (QueryCommandBase<?>) inflight.peek();
    int len = in.readUnsignedShort();
    cmd.decoder.decodeRow(len, in);
  }

  private void  decodeRowDescription(ByteBuf in) {
    ColumnDesc[] columns = new ColumnDesc[in.readUnsignedShort()];
    for (int c = 0; c < columns.length; ++c) {
      String fieldName = Util.readCStringUTF8(in);
      int tableOID = in.readInt();
      short columnAttributeNumber = in.readShort();
      int typeOID = in.readInt();
      short typeSize = in.readShort();
      int typeModifier = in.readInt();
      int textOrBinary = in.readUnsignedShort(); // Useless for now
      ColumnDesc column = new ColumnDesc(
        fieldName,
        tableOID,
        columnAttributeNumber,
        DataType.valueOf(typeOID),
        typeSize,
        typeModifier,
        DataFormat.valueOf(textOrBinary)
      );
      columns[c] = column;
    }
    RowDescription rowDesc = new RowDescription(columns);
    inflight.peek().handleRowDescription(rowDesc);
  }

  private static final byte I = (byte) 'I', T = (byte) 'T';

  private void decodeReadyForQuery(ByteBuf in) {
    byte id = in.readByte();
    TxStatus txStatus;
    if (id == I) {
      txStatus = TxStatus.IDLE;
    } else if (id == T) {
      txStatus = TxStatus.ACTIVE;
    } else {
      txStatus = TxStatus.FAILED;
    }
    inflight.peek().handleReadyForQuery(txStatus);
  }

  private void decodeError(ByteBuf in) {
    ErrorResponse response = new ErrorResponse();
    decodeErrorOrNotice(response, in);
    inflight.peek().handleErrorResponse(response);
  }

  private void decodeNotice(ByteBuf in) {
    NoticeResponse response = new NoticeResponse();
    decodeErrorOrNotice(response, in);
    inflight.peek().handleNoticeResponse(response);
  }

  private void decodeErrorOrNotice(Response response, ByteBuf in) {

    byte type;

    while ((type = in.readByte()) != 0) {

      switch (type) {

        case ErrorOrNoticeType.SEVERITY:
          response.setSeverity(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.CODE:
          response.setCode(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.MESSAGE:
          response.setMessage(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.DETAIL:
          response.setDetail(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.HINT:
          response.setHint(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.INTERNAL_POSITION:
          response.setInternalPosition(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.INTERNAL_QUERY:
          response.setInternalQuery(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.POSITION:
          response.setPosition(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.WHERE:
          response.setWhere(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.FILE:
          response.setFile(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.LINE:
          response.setLine(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.ROUTINE:
          response.setRoutine(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.SCHEMA:
          response.setSchema(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.TABLE:
          response.setTable(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.COLUMN:
          response.setColumn(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.DATA_TYPE:
          response.setDataType(Util.readCStringUTF8(in));
          break;

        case ErrorOrNoticeType.CONSTRAINT:
          response.setConstraint(Util.readCStringUTF8(in));
          break;

        default:
          Util.readCStringUTF8(in);
          break;
      }
    }
  }

  private void decodeAuthentication(ByteBuf in) {

    int type = in.readInt();
    switch (type) {
      case AuthenticationType.OK: {
        inflight.peek().handleAuthenticationOk();
      }
      break;
      case AuthenticationType.MD5_PASSWORD: {
        byte[] salt = new byte[4];
        in.readBytes(salt);
        inflight.peek().handleAuthenticationMD5Password(salt);
      }
      break;
      case AuthenticationType.CLEARTEXT_PASSWORD: {
        inflight.peek().handleAuthenticationClearTextPassword();
      }
      break;
      case AuthenticationType.KERBEROS_V5:
      case AuthenticationType.SCM_CREDENTIAL:
      case AuthenticationType.GSS:
      case AuthenticationType.GSS_CONTINUE:
      case AuthenticationType.SSPI:
      default:
        throw new UnsupportedOperationException("Authentication type " + type + " is not supported in the client");
    }
  }

  private CommandCompleteProcessor processor = new CommandCompleteProcessor();

  static class CommandCompleteProcessor implements ByteProcessor {
    private static final byte SPACE = 32;
    private int rows;
    boolean afterSpace;
    int parse(ByteBuf in) {
      afterSpace = false;
      rows = 0;
      in.forEachByte(in.readerIndex(), in.readableBytes() - 1, this);
      return rows;
    }
    @Override
    public boolean process(byte value) throws Exception {
      boolean space = value == SPACE;
      if (afterSpace) {
        if (space) {
          rows = 0;
        } else {
          rows = rows * 10 + (value - '0');
        }
      } else {
        afterSpace = space;
      }
      return true;
    }
  }

  private void decodeParseComplete() {
    inflight.peek().handleParseComplete();
  }

  private void decodeBindComplete() {
    inflight.peek().handleBindComplete();
  }

  private void decodeCloseComplete() {
    inflight.peek().handleCloseComplete();
  }

  private void decodeNoData() {
    inflight.peek().handleNoData();
  }

  private void decodeParameterDescription(ByteBuf in) {
    DataType[] paramDataTypes = new DataType[in.readUnsignedShort()];
    for (int c = 0; c < paramDataTypes.length; ++c) {
      paramDataTypes[c] = DataType.valueOf(in.readInt());
    }
    inflight.peek().handleParameterDescription(new ParameterDescription(paramDataTypes));
  }

  private void decodeParameterStatus(ByteBuf in) {
    String key = Util.readCStringUTF8(in);
    String value = Util.readCStringUTF8(in);
    inflight.peek().handleParameterStatus(key, value);
  }

  private void decodeEmptyQueryResponse() {
    inflight.peek().handleEmptyQueryResponse();
  }

  private void decodeBackendKeyData(ByteBuf in) {
    int processId = in.readInt();
    int secretKey = in.readInt();
    inflight.peek().handleBackendKeyData(processId, secretKey);
  }

  private void decodeNotificationResponse(ChannelHandlerContext ctx, ByteBuf in) {
    ctx.fireChannelRead(new NotificationResponse(in.readInt(), Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
  }
}
