package io.vertx.sqlclient.impl.tracing;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.Tuple;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Tracer for SQL client, wrapping the generic tracer.
 */
public class SqlTracer {

  enum RequestTags {

    // Generic
    PEER_ADDRESS("peer.address", q -> q.tracer.address),
    PEER_SERVICE("peer.service", q -> "todo"),
    SPAN_KIND("span.kind", q -> "client"),

    // DB
    // See https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/database.md

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
  private final String address;
  private final String host;
  private final int port;
  private final String user;
  private final String database;

  public SqlTracer(VertxTracer tracer, SqlConnectOptions options) {
    this.tracer = tracer;
    this.address = options.getHost() + ":" + options.getPort();
    this.host = options.getHost();
    this.port = options.getPort();
    this.user = options.getUser();
    this.database = options.getDatabase();
  }

  public Object sendRequest(ContextInternal context, String sql) {
    QueryRequest request = new QueryRequest(this, sql, Collections.emptyList());
    return tracer.sendRequest(context, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  public Object sendRequest(ContextInternal context, String sql, Tuple tuple) {
    QueryRequest request = new QueryRequest(this, sql, Collections.singletonList(tuple));
    return tracer.sendRequest(context, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  public Object sendRequest(ContextInternal context, String sql, List<Tuple> tuples) {
    QueryRequest request = new QueryRequest(this, sql, tuples);
    return tracer.sendRequest(context, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  public void receiveResponse(ContextInternal context, Object payload, Object result, Throwable failure) {
    tracer.receiveResponse(context, result, payload, failure, TagExtractor.empty());
  }
}
