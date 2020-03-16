package io.vertx.sqlclient;

import java.util.stream.Collector;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface Query<R> {
	
	Query<R> withOptions(QueryOptions options);
	
	<T> Query<SqlResult<T>> asCollector(Collector<?, ?, T> collector);
	
	Future<R> execute();
	
	void execute(Handler<AsyncResult<R>> handler);
	
}
