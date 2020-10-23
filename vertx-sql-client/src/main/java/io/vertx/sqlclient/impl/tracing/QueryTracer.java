package io.vertx.sqlclient.impl.tracing;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.Tuple;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Tracer for queries, wrapping the generic tracer.
 */
public class QueryTracer {

  enum RequestTags {

    // Generic
    PEER_ADDRESS("peer.address", q -> q.tracer.address),
    SPAN_KIND("span.kind", q -> "client"),

    // DB
    // See https://github.com/opentracing/specification/blob/master/semantic_conventions.md

    DB_USER("db.user", q -> q.tracer.user),
    DB_INSTANCE("db.instance", q -> q.tracer.database),
    DB_STATEMENT("db.statement", QueryRequest::sql),
    DB_TYPE("db.type", q -> "sql");

    final String name;
    final Function<QueryRequest, String> fn;

    RequestTags(String name, Function<QueryRequest, String> fn) {
      this.name = name;
      this.fn = fn;
    }
  }

  private static final TagExtractor<QueryRequest> REQUEST_TAG_EXTRACTOR = new TagExtractor<QueryRequest>() {

    private final RequestTags[] TAGS = RequestTags.values();

    @Override
    public int len(QueryRequest obj) {
      return TAGS.length;
    }
    @Override
    public String name(QueryRequest obj, int index) {
      return TAGS[index].name;
    }
    @Override
    public String value(QueryRequest obj, int index) {
      return TAGS[index].fn.apply(obj);
    }
  };

  private final VertxTracer tracer;
  private final TracingPolicy tracingPolicy;
  private final String address;
  private final String user;
  private final String database;

  public QueryTracer(VertxTracer tracer, TracingPolicy tracingPolicy, String address, String user, String database) {
    this.tracer = tracer;
    this.tracingPolicy = tracingPolicy;
    this.address = address;
    this.user = user;
    this.database = database;
  }

  public QueryTracer(VertxTracer tracer, SqlConnectOptions options) {
    this(tracer, options.getTracingPolicy(), options.getHost() + ":" + options.getPort(), options.getUser(), options.getDatabase());
  }

  public Object sendRequest(ContextInternal context, String sql) {
    QueryRequest request = new QueryRequest(this, sql, Collections.emptyList());
    return tracer.sendRequest(context, tracingPolicy, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  public Object sendRequest(ContextInternal context, String sql, Tuple tuple) {
    QueryRequest request = new QueryRequest(this, sql, Collections.singletonList(tuple));
    return tracer.sendRequest(context, tracingPolicy, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  public Object sendRequest(ContextInternal context, String sql, List<Tuple> tuples) {
    QueryRequest request = new QueryRequest(this, sql, tuples);
    return tracer.sendRequest(context, tracingPolicy, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  public void receiveResponse(ContextInternal context, Object payload, Object result, Throwable failure) {
    tracer.receiveResponse(context, result, payload, failure, TagExtractor.empty());
  }
}
