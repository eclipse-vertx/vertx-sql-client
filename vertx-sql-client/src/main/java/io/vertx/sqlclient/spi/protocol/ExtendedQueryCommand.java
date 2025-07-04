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

package io.vertx.sqlclient.spi.protocol;

import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.internal.TupleBase;

import java.util.List;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExtendedQueryCommand<R> extends QueryCommandBase<R> {

  public static <R> ExtendedQueryCommand<R> createQuery(
    String sql,
    PrepareOptions options,
    PreparedStatement ps,
    Tuple tuple,
    boolean autoCommit,
    Collector<Row, ?, R> collector,
    QueryResultHandler<R> resultHandler) {
    return new ExtendedQueryCommand<>(sql, options, ps, false, tuple, 0, null, false, autoCommit, collector, resultHandler);
  }

  public static <R> ExtendedQueryCommand<R> createQuery(
    String sql,
    PrepareOptions options,
    PreparedStatement ps,
    Tuple tuple,
    int fetch,
    String cursorId,
    boolean suspended,
    boolean autoCommit,
    Collector<Row, ?, R> collector,
    QueryResultHandler<R> resultHandler) {
    return new ExtendedQueryCommand<>(sql, options, ps, false, tuple, fetch, cursorId, suspended, autoCommit, collector, resultHandler);
  }

  public static <R> ExtendedQueryCommand<R> createBatch(
    String sql,
    PrepareOptions options,
    PreparedStatement ps,
    List<Tuple> tuples,
    boolean autoCommit,
    Collector<Row, ?, R> collector,
    QueryResultHandler<R> resultHandler) {
    return new ExtendedQueryCommand<>(sql, options, ps, true, tuples, 0, null, false, autoCommit, collector, resultHandler);
  }

  private final String sql;
  private final PrepareOptions options;
  private final PreparedStatement preparedStatement;
  private final boolean batch;
  private final int fetch;
  private final String cursorId;
  private final boolean suspended;
  private Object tuples;
  private boolean prepared;

  private ExtendedQueryCommand(String sql,
                               PrepareOptions options,
                               PreparedStatement preparedStatement,
                               boolean batch,
                               Object tuples,
                               int fetch,
                               String cursorId,
                               boolean suspended,
                               boolean autoCommit,
                               Collector<Row, ?, R> collector,
                               QueryResultHandler<R> resultHandler) {
    super(autoCommit, collector, resultHandler);
    this.sql = sql;
    this.options = options;
    this.preparedStatement = preparedStatement;
    this.batch = batch;
    this.tuples = tuples;
    this.fetch = fetch;
    this.cursorId = cursorId;
    this.suspended = suspended;
    this.prepared = preparedStatement != null;
  }

  public PrepareOptions options() {
    return options;
  }

  /**
   * Prepare and validate the tuple.
   *
   * @return {@code null} if the tuple preparation was successfull otherwise the validation error
   */
  public String prepare(PreparedStatement ps) {
    if (ps != null && !prepared) {
      prepared = true; // TODO : fix this
      try {
        if (batch) {
          tuples = ps.prepare((List<TupleBase>) tuples);
          return null;
        } else {
          tuples = ps.prepare((TupleBase) tuples);
        }
      } catch (Exception e) {
        return e.getMessage();
      }
    }
    return null;
  }

  public boolean isBatch() {
    return batch;
  }

  /**
   * @return the list of parameters for batch execution
   */
  public List<TupleBase> paramsList() {
    return batch ? (List<TupleBase>) tuples : null;
  }

  /**
   * @return the list of parameter types built from the tuple
   */
  public List<Class<?>> parameterTypes() {
    Tuple tuple;
    if (batch) {
      List<Tuple> list = (List<Tuple>) tuples;
      if (list.isEmpty()) {
        return null;
      }
      tuple = list.get(0);
    } else {
      tuple = (TupleBase) tuples;
    }
    return tuple.types();
  }

  /**
   * @return the parameters for query execution
   */
  public TupleBase params() {
    return batch ? null : (TupleBase) tuples;
  }

  public PreparedStatement preparedStatement() {
    return preparedStatement;
  }

  public int fetch() {
    return fetch;
  }

  public String cursorId() {
    return cursorId;
  }

  public boolean isSuspended() {
    return suspended;
  }

  @Override
  public String sql() {
    return sql;
  }

}
