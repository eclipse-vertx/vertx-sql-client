package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayColumnWriter extends ClickhouseColumnWriter {
  private final ClickhouseNativeDatabaseMetadata md;
  private final ClickhouseNativeColumnDescriptor elementTypeDescr;
  private final ClickhouseColumn elementTypeColumn;

  public ArrayColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeColumnDescriptor elementTypeDescr, ClickhouseNativeDatabaseMetadata md, int columnIndex) {
    super(data, descriptor.copyAsNestedArray(), columnIndex);
    this.md = md;
    this.elementTypeDescr = elementTypeDescr;
    this.elementTypeColumn = ClickhouseColumns.columnForSpec(elementTypeDescr, md);
  }

  @Override
  protected void serializeDataInternal(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    writeSizes(sink, false, fromRow, toRow);
    writeNullsInfo(sink, 0, columnDescriptor, data, fromRow, toRow, columnIndex);
    writeElementData(sink, 0, columnDescriptor, data, fromRow, toRow, columnIndex);
  }

  private void writeElementData(ClickhouseStreamDataSink sink, int localDepth, ClickhouseNativeColumnDescriptor descr, List<Tuple> localData, int fromRow, int toRow, int colIndex) {
    if (localDepth != 0 && descr.isArray() && elementTypeDescr.isNullable() && localData == null) {
      localData = Collections.emptyList();
    }
    ClickhouseNativeColumnDescriptor localNested = descr.getNestedDescr();
    if (localNested.isArray()) {
      localData = flattenArrays(localData, fromRow, toRow, colIndex);
      colIndex = 0;
      fromRow = 0;
      toRow = localData.size();
    }
    ClickhouseColumn localNestedColumn = ClickhouseColumns.columnForSpec(localNested, md);
    ClickhouseColumnWriter localWriter = localNestedColumn.writer(localData, colIndex);
    if (localWriter.getClass() == ArrayColumnWriter.class) {
      ArrayColumnWriter localArrayWriter = (ArrayColumnWriter)localWriter;
      localArrayWriter.writeElementData(sink, localDepth + 1, localNested, localData, fromRow, toRow, colIndex);
    } else {
      localWriter.serializeDataInternal(sink, fromRow, toRow);
    }
  }

  private void writeNullsInfo(ClickhouseStreamDataSink sink, int localDepth, ClickhouseNativeColumnDescriptor descr, List<Tuple> localData, int fromRow, int toRow, int colIndex) {
    if (localDepth != 0 && descr.isArray() && elementTypeDescr.isNullable() && localData == null) {
      localData = Collections.emptyList();
    }
    ClickhouseNativeColumnDescriptor localNested = descr.getNestedDescr();
    if (localNested.isArray()) {
      List<Tuple> flattened = flattenArrays(localData, fromRow, toRow, colIndex);
      writeNullsInfo(sink, localDepth + 1, localNested, flattened, 0, flattened.size(), 0);
    } else {
      if (localNested.isNullable()) {
        elementTypeColumn.writer(localData, colIndex).serializeNullsMap(sink, fromRow, toRow);
      }
    }
  }

  private static List<Tuple> flattenArrays(List<Tuple> data, int fromRow, int toRow, int colIndex) {
    List<Tuple> result = new ArrayList<>();
    for (int i = fromRow; i < toRow; ++i) {
      Tuple row = data.get(i);
      Object element = row.getValue(colIndex);
      Class<?> cls = element.getClass();
      if (cls.isArray() && cls != byte[].class) {
        Object[] arr = (Object[])element;
        List<Tuple> tuples = Arrays.stream(arr).map(Tuple::of).collect(Collectors.toList());
        result.addAll(tuples);
      } else {
        result.add(Tuple.of(element));
      }
    }
    return result;
  }

  private void writeSizes(ClickhouseStreamDataSink sink, boolean writeTotalSize, int fromRow, int toRow) {
    int nRows = toRow - fromRow;
    ClickhouseNativeColumnDescriptor column = columnDescriptor;
    List<Integer> sizes = new ArrayList<>();
    if (writeTotalSize) {
      sizes.add(nRows);
    }

    List<?> values = data;
    int localColumnIndex = columnIndex;
    ClickhouseNativeColumnDescriptor nestedColumn;
    while ((nestedColumn = column.getNestedDescr()).isArray()) {
      int offset = 0;
      List<Object> newValue = new ArrayList<>();
      for (int i = fromRow; i < toRow; ++i) {
        Object valObj = values.get(i);
        Object[] val = (Object[]) maybeUnwrapTuple(valObj, localColumnIndex);
        offset += val.length;
        sizes.add(offset);
        List<Object> newTuples = Arrays.asList(val);
        newValue.addAll(newTuples);
      }
      values = newValue;
      column = nestedColumn;
      localColumnIndex = 0;
      fromRow = 0;
      toRow = newValue.size();
    }
    sink.ensureWritable(sizes.size() * Long.BYTES);
    for (Integer size : sizes) {
      sink.writeLongLE(size);
    }
  }

  protected void serializeStatePrefix(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    elementTypeColumn.writer(data, 0).serializeStatePrefix(sink, fromRow, toRow);
  }

  private static Object maybeUnwrapTuple(Object val, int columnIndex) {
    return val instanceof Tuple ? ((Tuple)val).getValue(columnIndex) : val;
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
  }

  @Override
  protected void serializeNullsMap(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
  }

  @Override
  protected void ensureCapacity(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
  }
}
