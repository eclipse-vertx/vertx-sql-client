package io.vertx.sqlclient.templates;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientInternal;
import io.vertx.sqlclient.templates.impl.SqlTemplate;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TemplateBuilderTest {

  private abstract static class FakeClient implements SqlClientInternal {
    @Override
    public Query<RowSet<Row>> query(String sql) {
      throw new UnsupportedOperationException();
    }
    @Override
    public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
      throw new UnsupportedOperationException();
    }
    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
      throw new UnsupportedOperationException();
    }
    @Override
    public Future<Void> close() {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void testPostgresSql() {
    assertPgSql("${foo} ${bar}", "$1 $2");
    assertPgSql("${foo} ${foo}", "$1 $1");
    assertPgSql("${foo} ${bar} ${foo}", "$1 $2 $1");
    assertPgSql("${foo} ${bar} ${foo} ${bar}", "$1 $2 $1 $2");
  }

  @Test
  public void testPostgresTuple() {
    Map<String, Object> params = new HashMap<>();
    params.put("foo", "foo_value");
    params.put("bar", "bar_value");
    assertPgTuple("${foo} ${bar}", params, Tuple.of("foo_value", "bar_value"));
    assertPgTuple("${foo} ${foo}", params, Tuple.of("foo_value"));
    assertPgTuple("${foo} ${bar} ${foo}", params, Tuple.of("foo_value", "bar_value"));
    assertPgTuple("${foo} ${bar} ${foo} ${bar}", params, Tuple.of("foo_value", "bar_value"));
  }

  @Test
  public void testOtherSql() {
    assertOtherSql("${foo} ${bar}", "? ?");
    assertOtherSql("${foo} ${foo}", "? ?");
    assertOtherSql("${foo} ${bar} ${foo}", "? ? ?");
    assertOtherSql("${foo} ${bar} ${foo} ${bar}", "? ? ? ?");
  }

  @Test
  public void testOtherTuple() {
    Map<String, Object> params = new HashMap<>();
    params.put("foo", "foo_value");
    params.put("bar", "bar_value");
    assertOtherTuple("${foo} ${bar}", params, Tuple.of("foo_value", "bar_value"));
    assertOtherTuple("${foo} ${foo}", params, Tuple.of("foo_value", "foo_value"));
    assertOtherTuple("${foo} ${bar} ${foo}", params, Tuple.of("foo_value", "bar_value", "foo_value"));
    assertOtherTuple("${foo} ${bar} ${foo} ${bar}", params, Tuple.of("foo_value", "bar_value", "foo_value", "bar_value"));
  }

  @Test
  public void testSpecialCases() {
    assertOtherSql("\\${foo}", "${foo}");
    assertOtherSql("before\\${foo}after", "before${foo}after");
    assertOtherSql("${begin", "${begin");
  }

  private void assertPgSql(String template, String expectedSql) {
    Assert.assertEquals(pgTemplate(template).getSql(), expectedSql);
  }

  private void assertPgTuple(String template, Map<String, Object> params, Tuple expectedTuple) {
    SqlTemplate blah = pgTemplate(template);
    Tuple tuple = blah.mapTuple(params);
    assertTupleEquals(tuple, expectedTuple);
  }

  private SqlTemplate pgTemplate(String template) {
    return SqlTemplate.create(new FakeClient() {
      @Override
      public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
        queryBuilder.append('$').append(1 + index);
        return index;
      }
    }, template);
  }

  private void assertOtherSql(String template, String expectedSql) {
    Assert.assertEquals(otherTemplate(template).getSql(), expectedSql);
  }

  private void assertOtherTuple(String template, Map<String, Object> params, Tuple expectedTuple) {
    SqlTemplate blah = otherTemplate(template);
    Tuple tuple = blah.mapTuple(params);
    assertTupleEquals(tuple, expectedTuple);
  }

  private SqlTemplate otherTemplate(String template) {
    return SqlTemplate.create(new FakeClient() {
      @Override
      public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
        queryBuilder.append("?");
        return current;
      }
    }, template);
  }

  private static void assertTupleEquals(Tuple actual, Tuple expected) {
    assertEquals(actual.size(), expected.size());
    for (int idx = 0;idx < actual.size();idx++) {
      assertEquals(actual.getValue(idx), expected.getValue(idx));
    }
  }
}
