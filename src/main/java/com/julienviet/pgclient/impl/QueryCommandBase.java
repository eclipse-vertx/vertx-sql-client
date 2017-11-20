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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgException;
import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static com.julienviet.pgclient.codec.DataType.*;
import static com.julienviet.pgclient.codec.formatter.DateTimeFormatter.*;
import static com.julienviet.pgclient.codec.formatter.TimeFormatter.*;
import static java.nio.charset.StandardCharsets.*;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class QueryCommandBase extends CommandBase {

  protected final QueryResultHandler handler;
  private RowDescription rowDesc;

  public QueryCommandBase(QueryResultHandler handler) {
    this.handler = handler;
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      super.handleMessage(msg);
      handler.end();
    } else if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      Column[] columns = rowDesc.getColumns();
      List<String> columnNames = new ArrayList<>(columns.length);
      for (Column columnDesc : columns) {
        columnNames.add(columnDesc.getName());
      }
      handler.beginResult(columnNames);
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      JsonArray row = new JsonArray();
      Column[] columns = rowDesc.getColumns();
      for (int i = 0; i < columns.length; i++) {
        Column columnDesc = columns[i];
        DataFormat dataFormat = columnDesc.getDataFormat();
        DataType dataType = columnDesc.getDataType();
        byte[] data = dataRow.getValue(i);
        switch (dataFormat) {
          case TEXT: {
            handleText(dataType, data, row);
          }
          break;
          case BINARY: {
            handleBinary(dataType, data, row);
          }
          break;
        }
      }
      handler.handleRow(row);
    } else if (msg.getClass() == CommandComplete.class) {
      rowDesc = null;
      handler.endResult(false);
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      fail(new PgException(error));
    } else {
      super.handleMessage(msg);
    }
  }

  private void handleBinary(DataType dataType, byte[] d, JsonArray row) {
  }

  private void handleText(DataType type, byte[] data, JsonArray row) {
    if(data == null) {
      row.addNull();
      return;
    }
    if(type == CHAR) {
      row.add((char) data[0]);
      return;
    }
    if(type == BOOL) {
      if(data[0] == 't') {
        row.add(true);
      } else {
        row.add(false);
      }
      return;
    }
    if(type == BYTEA) {
      row.add(parseHexBinary(new String(data, 2, data.length - 2, UTF_8)));
      return;
    }
    String value = new String(data, UTF_8);
    switch (type) {
      case INT2:
        row.add(Short.parseShort(value));
        break;
      case INT4:
        row.add(Integer.parseInt(value));
        break;
      case INT8:
        row.add(Long.parseLong(value));
        break;
      case FLOAT4:
        row.add(Float.parseFloat(value));
        break;
      case FLOAT8:
        row.add(Double.parseDouble(value));
        break;
      case NUMERIC:
        BigDecimal big = new BigDecimal(value);
        if (big.scale() == 0) {
          row.add(big.toBigInteger());
        } else {
          // we might loose precision here
          row.add(big.doubleValue());
        }
        break;
      case TIMETZ:
        row.add(OffsetTime.parse(value, TIMETZ_FORMAT).toString());
        break;
      case TIMESTAMP:
        row.add(LocalDateTime.parse(value, TIMESTAMP_FORMAT).toInstant(ZoneOffset.UTC));
        break;
      case TIMESTAMPTZ:
        row.add(OffsetDateTime.parse(value, TIMESTAMPTZ_FORMAT).toInstant());
        break;
      case JSON:
      case JSONB:
        if(value.charAt(0)== '{') {
          row.add(new JsonObject(value));
        } else {
          row.add(new JsonArray(value));
        }
        break;
      case BPCHAR:
      case VARCHAR:
      case NAME:
      case TEXT:
      case UUID:
      case DATE:
      case TIME:
      default:
        row.add(value);
        break;
    }
  }

  @Override
  void fail(Throwable cause) {
    handler.fail(cause);
  }
}
