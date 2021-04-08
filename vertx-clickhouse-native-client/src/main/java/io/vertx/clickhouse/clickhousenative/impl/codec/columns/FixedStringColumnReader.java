package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FixedStringColumnReader extends ClickhouseColumnReader {
  private final Charset charset;
  private final boolean removeTrailingZerosInStrings;
  private final boolean enableStringCache;
  private final StringCache cache;

  private List<byte[]> elements;

  protected FixedStringColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor, boolean enableStringCache, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, columnDescriptor);
    this.elements = new ArrayList<>(nRows);
    this.charset = md.getStringCharset();
    this.removeTrailingZerosInStrings = md.isRemoveTrailingZerosInFixedStrings();
    this.enableStringCache = enableStringCache;
    this.cache = enableStringCache ? new StringCache(nRows) : null;
  }

  @Override
  protected Object[] readItems(ClickhouseStreamDataSource in) {
    int elementSize = columnDescriptor.getElementSize();
    while (elements.size() < nRows) {
      if (in.readableBytes() < elementSize) {
        return null;
      }
      byte[] stringBytes;
      if (nullsMap == null || !nullsMap.get(elements.size())) {
        stringBytes = new byte[elementSize];
        in.readBytes(stringBytes);
      } else {
        in.skipBytes(elementSize);
        stringBytes = null;
      }
      elements.add(stringBytes);
    }
    Object[] ret = elements.toArray(new byte[elements.size()][]);
    elements = null;
    return ret;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Object tmp = getObjectsArrayElement(rowIdx);
    if ((desired == String.class || desired == Object.class) && tmp != null) {
      return enableStringCache ? cache.get(rowIdx, () -> buildStringFromElement((byte[]) tmp)) : buildStringFromElement((byte[]) tmp);
    }
    return tmp;
  }

  private String buildStringFromElement(byte[] tmp) {
    int lastNonZeroIdx = removeTrailingZerosInStrings ? ColumnUtils.getLastNonZeroPos(tmp) : tmp.length - 1;
    return new String(tmp, 0, lastNonZeroIdx + 1, charset);
  }
}
