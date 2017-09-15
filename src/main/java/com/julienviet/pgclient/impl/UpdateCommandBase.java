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
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.UpdateResult;

import static com.julienviet.pgclient.codec.util.Util.handleRow;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class UpdateCommandBase extends CommandBase {

  private UpdateResult updateResult = new UpdateResult();
  private RowDescription rowDesc;
  protected Handler<Void> doneHandler;

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == CommandComplete.class) {
      rowDesc = null;
      CommandComplete complete = (CommandComplete) msg;
      updateResult.setUpdated(complete.getRowsAffected());
      handleResult(updateResult);
    } else if (msg.getClass() == RowDescription.class) {
      rowDesc = (RowDescription) msg;
    } else if (msg.getClass() == DataRow.class) {
      DataRow dataRow = (DataRow) msg;
      JsonArray keys = new JsonArray();
      Column[] columns = rowDesc.getColumns();
      handleRow(columns, dataRow, keys);
      updateResult.setKeys(keys);
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      fail(new PgException(error));
    } else {
      super.handleMessage(msg);
    }
  }

  abstract void handleResult(UpdateResult updateResult);

  abstract void fail(Throwable cause);

}
