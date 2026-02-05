package io.vertx.sqlclient.impl.tracing;

import io.vertx.core.internal.ContextInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.tracing.SpanKind;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.QueryResultBuilder;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.protocol.ExtendedQueryCommand;
import io.vertx.sqlclient.spi.protocol.QueryCommandBase;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Tracer for queries, wrapping the generic tracer.
 */
public class QueryReporter {

  enum RequestTags {

    // Generic
    PEER_ADDRESS("network.peer.address", q -> q.tracer.address),
    SPAN_KIND("span.kind", q -> "client"),

    // DB
    // See https://opentelemetry.io/docs/specs/semconv/database/

    DB_USER("db.user", q -> q.tracer.user),
    DB_NAMESPACE("db.namespace", q -> q.tracer.database),
    DB_QUERY_TEXT("db.query.text", QueryRequest::sql),
    DB_SYSTEM("db.system", q -> q.tracer.system),
    ;

    final String name;
    final Function<QueryRequest, String> fn;

    RequestTags(String name, Function<QueryRequest, String> fn) {
      this.name = name;
      this.fn = fn;
    }
  }

  private static final TagExtractor<QueryRequest> REQUEST_TAG_EXTRACTOR = new TagExtractor<>() {

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

  private final QueryCommandBase<?> cmd;
  private final VertxTracer tracer;
  private final ClientMetrics metrics;
  private final ContextInternal context;
  private final TracingPolicy tracingPolicy;
  private final String address;
  private final String user;
  private final String system;
  private final String database;
  private Object payload;
  private Object metric;

  public QueryReporter(VertxTracer tracer, ClientMetrics metrics, ContextInternal context, QueryCommandBase<?> queryCmd, Connection conn) {
    this.tracer = tracer;
    this.metrics = metrics;
    this.context = context;
    this.tracingPolicy = conn.tracingPolicy();
    this.address = conn.server().hostAddress() + ":" + conn.server().port();
    this.user = conn.user();
    this.system = conn.system();
    this.database = conn.database();
    this.cmd = queryCmd;
  }

  private Object sendRequest(ContextInternal context, String sql) {
    QueryRequest request = new QueryRequest(this, sql, Collections.emptyList());
    return tracer.sendRequest(context, SpanKind.RPC, tracingPolicy, request, "Query", (k, v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  private Object sendRequest(ContextInternal context, String sql, Tuple tuple) {
    QueryRequest request = new QueryRequest(this, sql, Collections.singletonList(tuple));
    return tracer.sendRequest(context, SpanKind.RPC, tracingPolicy, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  private Object sendRequest(ContextInternal context, String sql, List<Tuple> tuples) {
    QueryRequest request = new QueryRequest(this, sql, tuples);
    return tracer.sendRequest(context, SpanKind.RPC, tracingPolicy, request, "Query", (k,v) -> {}, REQUEST_TAG_EXTRACTOR);
  }

  private void receiveResponse(ContextInternal context, Object payload, Object result, Throwable failure) {
    tracer.receiveResponse(context, result, payload, failure, TagExtractor.empty());
  }

  public void before() {
    if (tracer != null) {
      String sql = cmd.sql();
      if (cmd instanceof SimpleQueryCommand) {
        payload = sendRequest(context, sql);
      } else {
        ExtendedQueryCommand<?> extendedQueryCmd = (ExtendedQueryCommand<?>) cmd;
        if (extendedQueryCmd.params() != null) {
          payload = sendRequest(context, sql, ((ExtendedQueryCommand<?>) cmd).params());
        } else {
          payload = sendRequest(context, sql, ((ExtendedQueryCommand) cmd).paramsList());
        }
      }
    }
    if (metrics != null) {
      String sql = cmd.sql();
      metric = metrics.init();
      metrics.requestBegin(metric, sql, sql);
      metrics.requestEnd(metric);
    }
  }

  public void after(Object res, Throwable err) {
    if (tracer != null) {
      QueryResultBuilder<?, ?, ?> qbr = (QueryResultBuilder) cmd.resultHandler();
      receiveResponse(context, payload, err == null ? qbr.first : null, err);
    }
    if (metrics != null) {
      if (err == null) {
        metrics.responseBegin(metric, null);
        metrics.responseEnd(metric);
      } else {
        metrics.requestReset(metric);
      }
    }
  }
}
