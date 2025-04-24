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

package io.vertx.sqlclient.internal.command;

import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.internal.TupleInternal;

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

  protected final String sql;
  protected final PrepareOptions options;
  public PreparedStatement ps;
  protected final boolean batch;
  private Object tuples;
  protected final int fetch;
  protected final String cursorId;
  protected final boolean suspended;
  private boolean prepared;

  private ExtendedQueryCommand(String sql,
                               PrepareOptions options,
                               PreparedStatement ps,
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
    this.ps = ps;
    this.batch = batch;
    this.tuples = tuples;
    this.fetch = fetch;
    this.cursorId = cursorId;
    this.suspended = suspended;
    this.prepared = ps != null;
  }

  public PrepareOptions options() {
    return options;
  }

  /**
   * Prepare and validate the tuple.
   *
   * @return {@code null} if the tuple preparation was successfull otherwise the validation error
   */
  public String prepare() {
    if (ps != null && !prepared) {
      prepared = true; // TODO : fix this
      try {
        if (batch) {
          tuples = ps.prepare((List<TupleInternal>) tuples);
          return null;
        } else {
          tuples = ps.prepare((TupleInternal) tuples);
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
  public List<TupleInternal> paramsList() {
    return batch ? (List<TupleInternal>) tuples : null;
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
      tuple = (TupleInternal) tuples;
    }
    return tuple.types();
  }

  /**
   * @return the parameters for query execution
   */
  public TupleInternal params() {
    return batch ? null : (TupleInternal) tuples;
  }

  public PreparedStatement preparedStatement() {
    return ps;
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
