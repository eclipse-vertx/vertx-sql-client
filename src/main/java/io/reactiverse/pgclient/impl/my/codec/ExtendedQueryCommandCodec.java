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
package io.reactiverse.pgclient.impl.my.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataFormat;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataTypeCodec;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.impl.command.ExtendedQueryCommand;

public class ExtendedQueryCommandCodec<R> extends QueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  // TODO handle re-bound situations?
  // Flag if parameters must be re-bound
  private final byte sendType = 1;

  public ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd, DataFormat.BINARY);
  }

  @Override
  void encodePayload(MyEncoder encoder) {
    super.encodePayload(encoder);
    MyPreparedStatement ps = (MyPreparedStatement) cmd.preparedStatement();
    writeExecuteMessage(encoder, ps.statementId, ps.paramDescs, sendType, cmd.params());
  }

  private void writeExecuteMessage(MyEncoder encoder, long statementId, ColumnDefinition[] paramsColumnDefinitions, byte sendType, Tuple params) {
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();

    payload.writeByte(CommandType.COM_STMT_EXECUTE);
    payload.writeIntLE((int) statementId);
    // CURSOR_TYPE_NO_CURSOR
    payload.writeByte(0x00);
    // iteration count, always 1
    payload.writeIntLE(1);

    int numOfParams = paramsColumnDefinitions.length;
    int bitmapLength = (numOfParams + 7) / 8;
    byte[] nullBitmap = new byte[bitmapLength];

    int pos = payload.writerIndex();

    if (numOfParams > 0) {
      // write a dummy bitmap first
      payload.writeBytes(nullBitmap);
      payload.writeByte(sendType);
      if (sendType == 1) {
        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          if (value != null) {
            payload.writeByte(paramsColumnDefinitions[i].getType().id);
          } else {
            payload.writeByte(ColumnDefinition.ColumnType.MYSQL_TYPE_NULL);
          }
          // TODO handle parameter flag (unsigned or signed)
          payload.writeByte(0);
        }

        for (int i = 0; i < numOfParams; i++) {
          Object value = params.getValue(i);
          //FIXME make sure we have correctly handled null value here
          if (value != null) {
            DataTypeCodec.encodeBinary(paramsColumnDefinitions[i].getType(), value, payload);
          } else {
            nullBitmap[i / 8] |= (1 << (i & 7));
          }
        }

      }

      // padding null-bitmap content
      payload.setBytes(pos, nullBitmap);
    }

    encoder.writePacketAndFlush(sequenceId++, payload);
  }
}
