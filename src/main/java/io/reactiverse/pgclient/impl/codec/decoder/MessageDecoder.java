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

import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.impl.codec.ColumnDesc;
import io.reactiverse.pgclient.impl.codec.DataFormat;
import io.reactiverse.pgclient.impl.codec.DataType;
import io.reactiverse.pgclient.impl.codec.decoder.message.*;
import io.reactiverse.pgclient.impl.codec.util.Util;
import io.reactiverse.pgclient.impl.PgResultImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;
import io.reactiverse.pgclient.impl.codec.decoder.message.type.AuthenticationType;
import io.reactiverse.pgclient.impl.codec.decoder.message.type.ErrorOrNoticeType;
import io.reactiverse.pgclient.impl.codec.decoder.message.type.MessageType;

import java.util.Deque;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageDecoder extends ChannelInboundHandlerAdapter {

  private final Deque<DecodeContext> decodeQueue;

  private ByteBuf in;

  public MessageDecoder(Deque<DecodeContext> decodeQueue) {
    this.decodeQueue = decodeQueue;
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
        composite = ctx.alloc().compositeBuffer();
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
            decodeReadyForQuery(ctx, in);
            break;
          }
          case MessageType.DATA_ROW: {
            decodeDataRow(in);
            break;
          }
          case MessageType.COMMAND_COMPLETE: {
            decodeCommandComplete(ctx, in);
            break;
          }
          case MessageType.BIND_COMPLETE: {
            decodeBindComplete(ctx);
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
        decodeRowDescription(ctx, in);
        break;
      }
      case MessageType.ERROR_RESPONSE: {
        decodeError(ctx, in);
        break;
      }
      case MessageType.NOTICE_RESPONSE: {
        decodeNotice(ctx, in);
        break;
      }
      case MessageType.AUTHENTICATION: {
        decodeAuthentication(ctx, in);
        break;
      }
      case MessageType.EMPTY_QUERY_RESPONSE: {
        decodeEmptyQueryResponse(ctx);
        break;
      }
      case MessageType.PARSE_COMPLETE: {
        decodeParseComplete(ctx);
        break;
      }
      case MessageType.CLOSE_COMPLETE: {
        decodeCloseComplete(ctx);
        break;
      }
      case MessageType.NO_DATA: {
        decodeNoData(ctx);
        break;
      }
      case MessageType.PORTAL_SUSPENDED: {
        decodePortalSuspended(ctx);
        break;
      }
      case MessageType.PARAMETER_DESCRIPTION: {
        decodeParameterDescription(ctx, in);
        break;
      }
      case MessageType.PARAMETER_STATUS: {
        decodeParameterStatus(ctx, in);
        break;
      }
      case MessageType.BACKEND_KEY_DATA: {
        decodeBackendKeyData(ctx, in);
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

  private void decodePortalSuspended(ChannelHandlerContext ctx_) {
    DecodeContext ctx = decodeQueue.peek();
    PgResult result = ctx.decoder.complete(0);
    ctx_.fireChannelRead(new PortalSuspended(result));
  }

  private void decodeCommandComplete(ChannelHandlerContext ctx_, ByteBuf in) {
    DecodeContext ctx = decodeQueue.peek();
    int updated = decodeCommandComplete(in);
    CommandComplete complete;
    if (ctx.decoder == null) {
      complete = new CommandComplete(new PgResultImpl(updated));
    } else {
      complete = new CommandComplete(ctx.decoder.complete(updated));
    }
    ctx_.fireChannelRead(complete);
  }

  private void decodeDataRow(ByteBuf in) {
    DecodeContext decodeCtx = decodeQueue.peek();
    int len = in.readUnsignedShort();
    decodeCtx.decoder.decodeRow(len, in);
  }

  private void decodeRowDescription(ChannelHandlerContext ctx, ByteBuf in) {
    ColumnDesc[] columns = decodeRowDescription(in);
    RowDescription rowDesc = new RowDescription(columns);
    ctx.fireChannelRead(rowDesc);
  }

  private void decodeReadyForQuery(ChannelHandlerContext ctx, ByteBuf in) {
    ctx.fireChannelRead(ReadyForQuery.decode(in.readByte()));
    decodeQueue.poll();
  }

  private void decodeError(ChannelHandlerContext ctx, ByteBuf in) {
    decodeErrorOrNotice(ctx, ErrorResponse.INSTANCE, in);
  }

  private void decodeNotice(ChannelHandlerContext ctx, ByteBuf in) {
    decodeErrorOrNotice(ctx, NoticeResponse.INSTANCE, in);
  }

  private void decodeErrorOrNotice(ChannelHandlerContext ctx, Response response, ByteBuf in) {

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
    ctx.fireChannelRead(response);
  }

  private void decodeAuthentication(ChannelHandlerContext ctx, ByteBuf in) {

    int type = in.readInt();
    switch (type) {
      case AuthenticationType.OK: {
        ctx.fireChannelRead(AuthenticationOk.INSTANCE);
      }
      break;
      case AuthenticationType.MD5_PASSWORD: {
        byte[] salt = new byte[4];
        in.readBytes(salt);
        ctx.fireChannelRead(new AuthenticationMD5Password(salt));
      }
      break;
      case AuthenticationType.CLEARTEXT_PASSWORD: {
        ctx.fireChannelRead(AuthenticationClearTextPassword.INSTANCE);
      }
      break;
      case AuthenticationType.KERBEROS_V5:
      case AuthenticationType.SCM_CREDENTIAL:
      case AuthenticationType.GSS:
      case AuthenticationType.GSS_CONTINUE:
      case AuthenticationType.SSPI:
      default:
        throw new UnsupportedOperationException("Authentication type is not supported in the client");
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

  private int decodeCommandComplete(ByteBuf in) {
    return processor.parse(in);
  }

  private ColumnDesc[]  decodeRowDescription(ByteBuf in) {
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
    return columns;
  }

  private void decodeParseComplete(ChannelHandlerContext ctx) {
    ctx.fireChannelRead(ParseComplete.INSTANCE);
  }

  private void decodeBindComplete(ChannelHandlerContext ctx) {
    ctx.fireChannelRead(BindComplete.INSTANCE);
  }

  private void decodeCloseComplete(ChannelHandlerContext ctx) {
    ctx.fireChannelRead(CloseComplete.INSTANCE);
  }

  private void decodeNoData(ChannelHandlerContext ctx) {
    ctx.fireChannelRead(NoData.INSTANCE);
  }

  private void decodeParameterDescription(ChannelHandlerContext ctx, ByteBuf in) {
    DataType[] paramDataTypes = new DataType[in.readUnsignedShort()];
    for (int c = 0; c < paramDataTypes.length; ++c) {
      paramDataTypes[c] = DataType.valueOf(in.readInt());
    }
    ctx.fireChannelRead(new ParameterDescription(paramDataTypes));
  }

  private void decodeParameterStatus(ChannelHandlerContext ctx, ByteBuf in) {
    ctx.fireChannelRead(new ParameterStatus(Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
  }

  private void decodeEmptyQueryResponse(ChannelHandlerContext ctx) {
    ctx.fireChannelRead(EmptyQueryResponse.INSTANCE);
  }

  private void decodeBackendKeyData(ChannelHandlerContext ctx, ByteBuf in) {
    ctx.fireChannelRead(new BackendKeyData(in.readInt(), in.readInt()));
  }

  private void decodeNotificationResponse(ChannelHandlerContext ctx, ByteBuf in) {
    ctx.fireChannelRead(new NotificationResponse(in.readInt(), Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
  }
}
