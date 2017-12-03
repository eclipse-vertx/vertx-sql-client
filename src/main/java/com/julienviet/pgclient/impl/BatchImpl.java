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

import com.julienviet.pgclient.PgBatch;
import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BatchImpl implements PgBatch {

  private final PgPreparedStatementImpl ps;
  private final ParameterDescription paramDesc;
  private final ArrayList<List<Object>> values = new ArrayList<>();

  BatchImpl(PgPreparedStatementImpl ps, ParameterDescription paramDesc) {
    this.paramDesc = paramDesc;
    this.ps = ps;
  }

  @Override
  public PgBatch add(List<Object> args) {
    String msg = paramDesc.validate(args);
    if (msg != null) {
      throw new IllegalArgumentException(msg);
    }
    values.add(args);
    return this;
  }

  @Override
  public void execute(Handler<AsyncResult<List<PgResult>>> resultHandler) {
    ps.batch(values, (Handler)resultHandler);
  }
}
