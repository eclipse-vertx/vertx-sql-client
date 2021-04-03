package io.vertx.clickhousenativeclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.*;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumnReader;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumnWriter;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumns;
import io.vertx.sqlclient.Tuple;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class ArraySerDesTest {
  private final ClickhouseColumn col;
  private final List<Tuple> data;

  public ArraySerDesTest(String nm, ClickhouseColumn col, List<Tuple> data) {
    this.col = col;
    this.data = data;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Iterable<Object[]> dataForTest() {
    ClickhouseNativeColumnDescriptor descr = ClickhouseColumns.columnDescriptorForSpec("Array(Array(Array(LowCardinality(Nullable(String)))))", "name");
    ClickhouseNativeDatabaseMetadata md = new ClickhouseNativeDatabaseMetadata("a", "b",
      0, 0,0, 0, "dname", ZoneId.systemDefault(), ZoneId.systemDefault(), "client",
      Collections.emptyMap(), StandardCharsets.UTF_8, null, null, null);
    ClickhouseColumn col = ClickhouseColumns.columnForSpec(descr, md);
    List<Tuple> data = Arrays.asList(Tuple.of(new String[][][]{ {{}, {"1"}, {"2", "3"}} }),
      Tuple.of(new String[][][]{ {{}, {"1"}, {"2", "3"}} }),
      Tuple.of(new String[][][]{ {{}}, {{}} } ),
      Tuple.of(new String[][][]{ {{}}, {{}} } ),
      Tuple.of( new String[][][]{ {{"str1_1", "str1_2", null}, {null}}, {{}} } ),
      Tuple.of( new String[][][]{ {{"str1_1", "str1_2", null}, {null}}, {{"str1_3", "str1_4", null}, {null}}} )
    );

    return Arrays.asList(new Object[][]{
      {data.size() + " rows", col, data},
      {"0 rows", col, Collections.emptyList()}
    });
  }

  @Test
  public void doSerDes() {
    ClickhouseColumnWriter writer = col.writer(data, 0);
    ByteBuf buf = Unpooled.buffer();
    ClickhouseStreamDataSink sink = new RawClickhouseStreamDataSink(buf);
    writer.serializeColumn(sink, 0, data.size());
    sink.finish();

    ClickhouseColumnReader reader = col.reader(data.size());
    ClickhouseStreamDataSource ds = new RawClickhouseStreamDataSource();
    ds.moreData(buf, UnpooledByteBufAllocator.DEFAULT);
    reader.readColumn(ds);
    for (int rowIdx = 0; rowIdx < data.size(); ++rowIdx) {
      Object actual = reader.getElement(rowIdx, String.class);
      Object expected = data.get(rowIdx).getValue(0);
      Assert.assertArrayEquals((Object[])expected, (Object[])actual);
    }
  }
}
