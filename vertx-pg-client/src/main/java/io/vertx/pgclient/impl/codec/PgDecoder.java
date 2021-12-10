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

package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.vertx.sqlclient.impl.Notification;
import io.vertx.pgclient.impl.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;

import java.util.ArrayDeque;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class PgDecoder extends ChannelInboundHandlerAdapter {

  private final ArrayDeque<PgCommandCodec<?, ?>> inflight;
  private ByteBufAllocator alloc;
  private ByteBuf in;

  PgDecoder(ArrayDeque<PgCommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    alloc = ctx.alloc();
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (in != null) {
      ByteBuf buff = this.in;
      this.in = null;
      buff.release();
    }
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
          case PgProtocolConstants.MESSAGE_TYPE_READY_FOR_QUERY: {
            decodeReadyForQuery(ctx, in);
            break;
          }
          case PgProtocolConstants.MESSAGE_TYPE_DATA_ROW: {
            decodeDataRow(in);
            break;
          }
          case PgProtocolConstants.MESSAGE_TYPE_COMMAND_COMPLETE: {
            decodeCommandComplete(in);
            break;
          }
          case PgProtocolConstants.MESSAGE_TYPE_BIND_COMPLETE: {
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
      case PgProtocolConstants.MESSAGE_TYPE_ROW_DESCRIPTION: {
        decodeRowDescription(in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_ERROR_RESPONSE: {
        decodeError(ctx, in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_NOTICE_RESPONSE: {
        decodeNotice(in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_AUTHENTICATION: {
        decodeAuthentication(in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_EMPTY_QUERY_RESPONSE: {
        decodeEmptyQueryResponse();
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_PARSE_COMPLETE: {
        decodeParseComplete();
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_CLOSE_COMPLETE: {
        decodeCloseComplete();
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_NO_DATA: {
        decodeNoData();
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_PORTAL_SUSPENDED: {
        decodePortalSuspended();
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_PARAMETER_DESCRIPTION: {
        decodeParameterDescription(in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_PARAMETER_STATUS: {
        decodeParameterStatus(in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_BACKEND_KEY_DATA: {
        decodeBackendKeyData(in);
        break;
      }
      case PgProtocolConstants.MESSAGE_TYPE_NOTIFICATION_RESPONSE: {
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
    PgCommandCodec<?, ?> codec = inflight.peek();
    QueryCommandBaseCodec<?, ?> cmd = (QueryCommandBaseCodec<?, ?>) codec;
    int len = in.readUnsignedShort();
    cmd.decoder.handleRow(len, in);
  }

  private void  decodeRowDescription(ByteBuf in) {
    PgColumnDesc[] columns = new PgColumnDesc[in.readUnsignedShort()];
    for (int c = 0; c < columns.length; ++c) {
      String fieldName = Util.readCStringUTF8(in);
      int tableOID = in.readInt();
      short columnAttributeNumber = in.readShort();
      int typeOID = in.readInt();
      short typeSize = in.readShort();
      int typeModifier = in.readInt();
      int textOrBinary = in.readUnsignedShort(); // Useless for now
      PgColumnDesc column = new PgColumnDesc(
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
    inflight.peek().handleRowDescription(columns);
  }

  private static final byte I = (byte) 'I', T = (byte) 'T';

  private void decodeReadyForQuery(ChannelHandlerContext ctx, ByteBuf in) {
    byte id = in.readByte();
    if (id == I) {
      // IDLE
    } else if (id == T) {
      // ACTIVE
    } else {
      // FAILED
      ctx.fireChannelRead(TxFailedEvent.INSTANCE);
    }
    inflight.peek().handleReadyForQuery();
  }

  private void decodeError(ChannelHandlerContext ctx, ByteBuf in) {
    ErrorResponse response = new ErrorResponse();
    decodeErrorOrNotice(response, in);
    switch (response.getCode()) {
      default:
        PgCommandCodec<?, ?> cmd = inflight.peek();
        cmd.handleErrorResponse(response);
        break;
        // Unsolicited errors
      case "57P01":
        // admin_shutdown
      case "25P03":
        // terminating connection due to idle-in-transaction timeout
        ctx.fireExceptionCaught(response.toException());
        break;
    }
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

        case PgProtocolConstants.ERROR_OR_NOTICE_SEVERITY:
          response.setSeverity(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_CODE:
          response.setCode(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_MESSAGE:
          response.setMessage(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_DETAIL:
          response.setDetail(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_HINT:
          response.setHint(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_INTERNAL_POSITION:
          response.setInternalPosition(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_INTERNAL_QUERY:
          response.setInternalQuery(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_POSITION:
          response.setPosition(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_WHERE:
          response.setWhere(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_FILE:
          response.setFile(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_LINE:
          response.setLine(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_ROUTINE:
          response.setRoutine(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_SCHEMA:
          response.setSchema(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_TABLE:
          response.setTable(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_COLUMN:
          response.setColumn(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_DATA_TYPE:
          response.setDataType(Util.readCStringUTF8(in));
          break;

        case PgProtocolConstants.ERROR_OR_NOTICE_CONSTRAINT:
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
      case PgProtocolConstants.AUTHENTICATION_TYPE_OK: {
        inflight.peek().handleAuthenticationOk();
      }
      break;
      case PgProtocolConstants.AUTHENTICATION_TYPE_MD5_PASSWORD: {
        byte[] salt = new byte[4];
        in.readBytes(salt);
        inflight.peek().handleAuthenticationMD5Password(salt);
      }
      break;
      case PgProtocolConstants.AUTHENTICATION_TYPE_CLEARTEXT_PASSWORD: {
        inflight.peek().handleAuthenticationClearTextPassword();
      }
      break;
      case PgProtocolConstants.AUTHENTICATION_TYPE_SASL: {
        inflight.peek().handleAuthenticationSasl(in);
      }
      break;
      case PgProtocolConstants.AUTHENTICATION_TYPE_SASL_CONTINUE: {
        inflight.peek().handleAuthenticationSaslContinue(in);
      }
      break;
      case PgProtocolConstants.AUTHENTICATION_TYPE_SASL_FINAL: {
        inflight.peek().handleAuthenticationSaslFinal(in);
      }
      break;
      case PgProtocolConstants.AUTHENTICATION_TYPE_KERBEROS_V5:
      case PgProtocolConstants.AUTHENTICATION_TYPE_SCM_CREDENTIAL:
      case PgProtocolConstants.AUTHENTICATION_TYPE_GSS:
      case PgProtocolConstants.AUTHENTICATION_TYPE_GSS_CONTINUE:
      case PgProtocolConstants.AUTHENTICATION_TYPE_SSPI:
      default:
        throw new UnsupportedOperationException("Authentication type " + type + " is not supported in the client");
    }
  }

  private CommandCompleteProcessor processor = new CommandCompleteProcessor();

  static class CommandCompleteProcessor implements ByteProcessor {
    private static final byte[] SELECT = new byte[]{'S', 'E', 'L', 'E', 'C', 'T'};
    private static final int MIN_READABLE_BYTES_OF_SELECT = 9;
    private static final byte SPACE = 32;
    private int rows;
    boolean afterSpace;
    int parse(ByteBuf in) {
      afterSpace = false;
      rows = 0;
      int readerIndex = in.readerIndex();
      int readableBytes = in.readableBytes();
      if (readableBytes >= MIN_READABLE_BYTES_OF_SELECT && in.getByte(readerIndex) == SELECT[0]) {
        boolean isSelect = true;
        for (int i = 1; i < SELECT.length; i++) {
          isSelect = SELECT[i] == in.getByte(readerIndex + i);
        }
        if (isSelect) {
          return rows;
        }
      }
      in.forEachByte(readerIndex, readableBytes - 1, this);
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
    inflight.peek().handleParameterDescription(new PgParamDesc(paramDataTypes));
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
    ctx.fireChannelRead(new Notification(in.readInt(), Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
  }
}
