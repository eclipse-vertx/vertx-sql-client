package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.vertx.ext.sql.TransactionIsolation;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class TxQueryCommandBase extends CommandBase {

  private String value;

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == RowDescription.class) {
      return false;
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      value = new String(dataRow.getValue(0), UTF_8);
      return false;
    } else if (msg.getClass() == CommandComplete.class) {
      switch (value) {
        case "read committed": {
          handleResult(TransactionIsolation.READ_COMMITTED);
        }
        break;
        case "read uncommitted": {
          handleResult(TransactionIsolation.READ_UNCOMMITTED);
        }
        break;
        case "repeatable read": {
          handleResult(TransactionIsolation.REPEATABLE_READ);
        }
        break;
        case "serializable": {
          handleResult(TransactionIsolation.SERIALIZABLE);
        }
        break;
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

  abstract void handleResult(TransactionIsolation isolation);

  abstract void fail(Throwable cause);

}
