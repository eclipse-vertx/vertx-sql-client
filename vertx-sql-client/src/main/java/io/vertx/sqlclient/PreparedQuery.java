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

package io.vertx.sqlclient;

import io.vertx.core.Future;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collector;

/**
 * A prepared statement, the statement is pre-compiled and
 * it's more efficient to execute the statement for multiple times.
 * In addition, this kind of statement provides protection against SQL injection attacks.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PreparedQuery<R> extends Query<R> {
	
	@Override
	public PreparedQuery<R> withOptions(QueryOptions options);
	
	@Override
	public <T> PreparedQuery<SqlResult<T>> asCollector(Collector<?, ?, T> collector);
	
	public PreparedQuery<Cursor> asCursor();
	
	public PreparedQuery<RowStream<Row>> asStream(int fetchSize);
	
	public Future<R> execute(Tuple args);
	@Override
	public default Future<R> execute() {
		return execute(ArrayTuple.EMPTY);
	}

	public void execute(Tuple args, Handler<AsyncResult<R>> handler);
	@Override
	public default void execute(Handler<AsyncResult<R>> handler) {
		execute(ArrayTuple.EMPTY, handler);
	}


  /**
   * Execute a batch.
   *
   * @param argsList the list of tuple for the batch
   * @return the createBatch
   */
  void executeBatch(List<Tuple> argsList, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #batch(List, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<R> executeBatch(List<Tuple> argsList);

  /**
   * Close the prepared query and release its resources.
   */
  Future<Void> close();

  /**
   * Like {@link #close()} but notifies the {@code completionHandler} when it's closed.
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

}
