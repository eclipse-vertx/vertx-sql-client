package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;

import static com.julienviet.pgclient.codec.DataType.*;
import static com.julienviet.pgclient.codec.decoder.message.type.CommandCompleteType.*;
import static com.julienviet.pgclient.codec.formatter.DateTimeFormatter.*;
import static com.julienviet.pgclient.codec.formatter.TimeFormatter.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class QueryCommandBase extends CommandBase {

  private RowDescription rowDesc;
  private ResultSet resultSet;
  private List<String> columnNames;
  private List<JsonArray> results;

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
      resultSet = new ResultSet();
      results = new ArrayList<>();
      return false;
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      Column[] columns = rowDesc.getColumns();
      columnNames = new ArrayList<>(columns.length);
      JsonArray crow = new JsonArray();
      for (int i = 0; i < columns.length; i++) {
        Column columnDesc = columns[i];
        columnNames.add(columnDesc.getName());
        DataFormat dataFormat = columnDesc.getDataFormat();
        DataType dataType = columnDesc.getDataType();
        byte[] data = dataRow.getValue(i);
        switch (dataFormat) {
          case TEXT: {
            handleText(dataType, data, crow);
          }
          break;
          case BINARY: {
            handleBinary(dataType, data, crow);
          }
          break;
        }
      }
      // vertx common
      results.add(crow);
      resultSet.setColumnNames(columnNames);
      resultSet.setResults(results);
      return false;
    } else if (msg.getClass() == CommandComplete.class) {
      CommandComplete complete = (CommandComplete) msg;
      rowDesc = null;
      if(complete.getCommand().equals(SELECT)) {
        ResultSet r = resultSet;
        resultSet = null;
        if (r == null) {
          r = new ResultSet();
        }
        handleResult(r);
      }
      return false;
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      fail(new RuntimeException(error.getMessage()));
      return false;
    } else {
      return super.handleMessage(msg);
    }
  }

  private void handleBinary(DataType dataType, byte[] d, JsonArray crow) {

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
      case DATE:
        row.add(LocalDate.parse(value));
        break;
      case TIME:
        row.add(LocalTime.parse(value));
        break;
      case TIMETZ:
        row.add(OffsetTime.parse(value, TIMETZ_FORMAT));
        break;
      case TIMESTAMP:
        row.add(LocalDateTime.parse(value, TIMESTAMP_FORMAT));
        break;
      case TIMESTAMPTZ:
        row.add(OffsetDateTime.parse(value, TIMESTAMPTZ_FORMAT));
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
      default:
        row.add(value);
        break;
    }
  }

  abstract void handleResult(ResultSet resultSet);

  abstract void fail(Throwable cause);
}