package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.clickhouse.clickhousenative.impl.codec.RawClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LowCardinalityColumnWriter extends ClickhouseColumnWriter {
  //Need to read additional keys.
  //Additional keys are stored before indexes as value N and N keys
  //after them.
  public static final int HAS_ADDITIONAL_KEYS_BIT = 1 << 9;
  //  # Need to update dictionary.
  //  # It means that previous granule has different dictionary.
  public static final int NEED_UPDATE_DICTIONARY = 1 << 10;
  public static final int SERIALIZATION_TYPE = HAS_ADDITIONAL_KEYS_BIT | NEED_UPDATE_DICTIONARY;

  private final ClickhouseNativeDatabaseMetadata md;
  private Map<Object, Integer> dictionaryIndex;
  private List<Integer> keys;
  private int nullAddon;

  public LowCardinalityColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, ClickhouseNativeDatabaseMetadata md, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
    this.md = md;
  }

  @Override
  protected void serializeStatePrefix(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    sink.writeLongLE(1);
  }

  @Override
  protected void serializeDataInternal(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    dictionaryIndex = new LinkedHashMap<>();
    keys = new ArrayList<>();
    ClickhouseNativeColumnDescriptor dictionaryWriterDescr = columnDescriptor.copyWithModifiers(false, false);
    ClickhouseColumn dictionaryColumn = ClickhouseColumns.columnForSpec(dictionaryWriterDescr, md);
    nullAddon = columnDescriptor.isNullable() ? 1 : 0;
    super.serializeDataInternal(sink, fromRow, toRow);
    int dictionarySize = dictionaryIndex.size() + nullAddon;
    //empty array
    if (dictionarySize == 0) {
      return;
    }
    int intType = (int) (log2(dictionarySize) / 8);
    ClickhouseColumn valuesColumn = LowCardinalityColumnReader.uintColumn(columnDescriptor.name(), intType);

    int serializationType = SERIALIZATION_TYPE | intType;
    sink.writeLongLE(serializationType);
    sink.writeLongLE(dictionarySize);

    Collection<Object> nullVal = columnDescriptor.isNullable() ? Collections.singleton(dictionaryColumn.nullValue()) : Collections.emptyList();
    ArrayList<Tuple> dictionaryTuples = Stream.concat(nullVal.stream(), dictionaryIndex.keySet().stream())
      .map(LowCardinalityColumnWriter::maybeUnwrapArrayWrapper)
      .map(Tuple::of)
      .collect(Collectors.toCollection(ArrayList::new));

    ClickhouseColumnWriter dictionaryWriter = dictionaryColumn.writer(dictionaryTuples, 0);
    dictionaryWriter.serializeData(sink, 0, dictionaryTuples.size());
    sink.writeLongLE(data.size());
    ClickhouseColumnWriter valuesColumnWriter = valuesColumn.writer(keys.stream().map(Tuple::of).collect(Collectors.toCollection(ArrayList::new)), 0);
    valuesColumnWriter.serializeData(sink, 0, data.size());
  }

  private static Object maybeUnwrapArrayWrapper(Object from) {
    if (from.getClass() == ArrayWrapper.class) {
      return ((ArrayWrapper) from).array;
    }
    return from;
  }

  private Object maybeWrapArray(Object val) {
    if (val.getClass() == byte[].class) {
      val = new ArrayWrapper((byte[]) val);
    } else if (val.getClass() == String.class) {
      val = new ArrayWrapper(((String) val).getBytes(md.getStringCharset()));
    }
    return val;
  }

  private double log2(int x) {
    return (Math.log(x) / Math.log(2));
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    val = maybeWrapArray(val);
    Integer index = dictionaryIndex.computeIfAbsent(val, dictionaryMissVal -> dictionaryIndex.size() + nullAddon);
    keys.add(index);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    keys.add(0);
  }

  @Override
  protected void serializeNullsMap(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
  }

  private static class ArrayWrapper {
    private final byte[] array;
    private final int hash;

    ArrayWrapper(byte[] array) {
      this.array = array;
      this.hash = Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ArrayWrapper that = (ArrayWrapper) o;
      return hash == that.hash && Arrays.equals(array, that.array);
    }

    @Override
    public int hashCode() {
      return hash;
    }
  }

  public static void main(String[] args) {
    ClickhouseNativeColumnDescriptor descr = ClickhouseColumns.columnDescriptorForSpec("LowCardinality(Nullable(String))", "name");
    List<Tuple> data = Arrays.asList(Tuple.of("str1"), Tuple.of("str2"), Tuple.of("str1"), Tuple.of(null));

    ClickhouseNativeDatabaseMetadata md = new ClickhouseNativeDatabaseMetadata("a", "b",
      0, 0,0, 0, "dname", ZoneId.systemDefault(), "client",
      Collections.emptyMap(), StandardCharsets.UTF_8, null, null, null);
    LowCardinalityColumnWriter writer = new LowCardinalityColumnWriter(data, descr, md, 0);

    ByteBuf buffer = Unpooled.buffer(100);
    RawClickhouseStreamDataSink sink = new RawClickhouseStreamDataSink(buffer);
    writer.serializeData(sink, 0, data.size());
    System.err.println(writer.dictionaryIndex);
    System.err.println(writer.keys);
    System.err.println(buffer.readableBytes());
    System.err.print(ByteBufUtil.hexDump(buffer));
  }
}
