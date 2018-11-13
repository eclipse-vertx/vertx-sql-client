package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.codec.DataType;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents data to be copied during a binary COPY FROM operation.
 */
@VertxGen
public interface CopyData {

  /**
   * A new CopyData object. The {@link DataType} will be inferred.
   * @param value the value of this data.
   * @return the data
   */
  static CopyData create(Object value) {
    return new CopyDataImpl(value);
  }

  /**
   * A new CopyData object.
   * @param value the value of this data.
   * @param type the {@link DataType}
   * @return the data
   */
  static CopyData create(Object value, DataType type) {
    return new CopyDataImpl(value, type);
  }

  /**
   * Gets the value of this copy data.
   * @return the value or {@code null}
   */
  Object getValue();

  /**
   * Gets the {@link DataType} of this copy data.
   * @return the {@link DataType} or {@link DataType#UNKNOWN}
   */
  DataType getDataType();
}
