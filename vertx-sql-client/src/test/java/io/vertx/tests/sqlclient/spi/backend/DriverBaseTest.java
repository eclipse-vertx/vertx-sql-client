package io.vertx.tests.sqlclient.spi.backend;

import io.vertx.core.Completable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowBase;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.internal.QueryResultHandler;
import io.vertx.sqlclient.internal.RowDescriptorBase;
import io.vertx.sqlclient.spi.connection.ConnectionContext;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.DriverBase;
import org.junit.Test;

import java.sql.JDBCType;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

import static org.junit.Assert.*;

public class DriverBaseTest {

  private static class VarcharColumnDescriptor implements ColumnDescriptor {

    private final String name;

    public VarcharColumnDescriptor(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return name;
    }
    @Override
    public boolean isArray() {
      return false;
    }
    @Override
    public String typeName() {
      return "VARCHAR";
    }
    @Override
    public JDBCType jdbcType() {
      return JDBCType.VARCHAR;
    }
  }

  @Test
  public void testSimple() {

    DriverBase<SqlConnectOptions> driver = new DriverBase<SqlConnectOptions>("generic") {
      @Override
      public ConnectionFactory<SqlConnectOptions> createConnectionFactory(Vertx vertx, NetClientOptions transportOptions) {
        return new ConnectionFactory<>() {
          @Override
          public Future<Connection> connect(Context context, SqlConnectOptions options) {
            return Future.succeededFuture(new Connection() {
              @Override
              public TracingPolicy tracingPolicy() {
                return null;
              }
              @Override
              public SocketAddress server() {
                return null;
              }
              @Override
              public String database() {
                return "";
              }
              @Override
              public String user() {
                return "";
              }
              @Override
              public ClientMetrics metrics() {
                return null;
              }
              @Override
              public void init(ConnectionContext context) {
              }
              @Override
              public boolean isSsl() {
                return false;
              }
              @Override
              public boolean isValid() {
                return true;
              }
              @Override
              public int pipeliningLimit() {
                return 1;
              }
              @Override
              public DatabaseMetadata databaseMetadata() {
                throw new UnsupportedOperationException();
              }
              @Override
              public void close(ConnectionContext holder, Completable<Void> promise) {
                promise.succeed();
              }
              @Override
              public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
                if (cmd instanceof SimpleQueryCommand) {
                  SimpleQueryCommand simpleQueryCmd = (SimpleQueryCommand) cmd;
                  scheduleQueryCommand(simpleQueryCmd, (Completable) handler);
                } else {
                  handler.fail(new UnsupportedOperationException());
                }
              }
              private <T, A> void scheduleQueryCommand(SimpleQueryCommand<T> simpleQuery, Completable<Boolean> handler) {
                QueryResultHandler<T> qrh = simpleQuery.resultHandler();
                Collector<Row, A, T> collector = (Collector<Row, A, T>) simpleQuery.collector();
                A container = collector.supplier().get();
                BiConsumer<A, Row> accumulator = collector.accumulator();
                RowDescriptorBase rowDescriptor = new RowDescriptorBase(new ColumnDescriptor[]{new VarcharColumnDescriptor("value")});
                Row row = new RowBase(rowDescriptor);
                row.addValue("Hello " + simpleQuery.sql());
                accumulator.accept(container, row);
                T result = collector.finisher().apply(container);
                qrh.handleResult(0, 1, rowDescriptor
                  , result, null);
                handler.succeed(true);
              }
            });
          }
          @Override
          public void close(Completable<Void> completable) {
            completable.succeed();
          }
        };
      }
      @Override
      public SqlConnectOptions parseConnectionUri(String uri) {
        throw new UnsupportedOperationException();
      }
      @Override
      public boolean acceptsOptions(SqlConnectOptions connectOptions) {
        return true;
      }
      @Override
      public SqlConnectOptions downcast(SqlConnectOptions connectOptions) {
        return connectOptions;
      }
    };

    Vertx vertx = Vertx.vertx();

    try {
      Pool pool = driver.createPool(vertx, () -> Future.succeededFuture(new SqlConnectOptions()), new PoolOptions(), new NetClientOptions(), null);

      Future<RowSet<Row>> res = pool.withConnection(conn -> conn.query("Julien").execute());

      RowSet<Row> rowset = res.await();
      RowIterator<Row> iterator = rowset.iterator();
      assertTrue(iterator.hasNext());
      Row row = iterator.next();
      assertEquals(1, row.size());
      assertEquals("Hello Julien", row.getString(0));
      assertFalse(iterator.hasNext());
    } finally {
      vertx.close().await();
    }
  }
}
