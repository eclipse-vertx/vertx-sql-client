package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Result;
import com.julienviet.pgclient.Row;
import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import com.julienviet.pgclient.codec.formatter.DateTimeFormatter;
import com.julienviet.pgclient.codec.formatter.TimeFormatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class QueryCommandBase extends CommandBase {

  private RowDescription rowDesc;
  private Result result;

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      result = new Result();
      return false;
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      Column[] columns = rowDesc.getColumns();
      Row row = new Row();
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
      result.add(row);
      return false;
    } else if (msg.getClass() == CommandComplete.class) {
      CommandComplete complete = (CommandComplete) msg;
      Result r = result;
      result = null;
      rowDesc = null;
      if (r == null) {
        r = new Result();
      }
      r.setUpdatedRows(complete.getRowsAffected());
      handleResult(r);
      return false;
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      fail(new RuntimeException(error.getMessage()));
      return false;
    } else {
      return super.handleMessage(msg);
    }
  }

  private void handleText(DataType type, byte[] data, Row row) {
    if(data == null) {
      row.add(null);
      return;
    }
    if(type == DataType.CHAR) {
      row.add((char) data[0]);
      return;
    }
    if(type == DataType.BOOL) {
      if(data[0] == 't') {
        row.add(true);
      } else {
        row.add(false);
      }
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
        row.add(new BigDecimal(value));
        break;
      case BPCHAR:
      case VARCHAR:
      case NAME:
      case TEXT:
        row.add(value);
        break;
      case UUID:
        row.add(java.util.UUID.fromString(value));
        break;
      case DATE:
        row.add(LocalDate.parse(value));
        break;
      case TIME:
        row.add(LocalTime.parse(value));
        break;
      case TIMETZ:
        row.add(OffsetTime.parse(value, TimeFormatter.TIMETZ_FORMAT));
        break;
      case TIMESTAMP:
        row.add(LocalDateTime.parse(value, DateTimeFormatter.TIMESTAMP_FORMAT));
        break;
      case TIMESTAMPTZ:
        row.add(OffsetDateTime.parse(value, DateTimeFormatter.TIMESTAMPTZ_FORMAT));
        break;
      case JSON:
      case JSONB:
        if(value.charAt(0)== '{') {
          row.add(new JsonObject(value));
        } else {
          row.add(new JsonArray(value));
        }
        break;
      default:
        System.out.println("unsupported " + type);
        break;
    }
  }

  private void handleBinary(DataType type, byte[] data, Row row) {

  }

  abstract void handleResult(Result result);

  abstract void fail(Throwable cause);
}
