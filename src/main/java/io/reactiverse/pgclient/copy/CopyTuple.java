package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.data.Json;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

/**
 * A tuple for use with binary data for the COPY FROM command.
 */
@VertxGen
public interface CopyTuple extends Tuple {

  /**
   * @return a new empty CopyTuple
   */
  static CopyTuple tuple() {
    return new CopyTupleImpl(10);
  }

  /**
   * Gets the {@link DataType} at {@code pos}.
   * @param pos the position
   * @return the {@link DataType} or {@link DataType#UNKNOWN}
   */
  DataType getDataType(int pos);

  /**
   * Add an object value at the end of the tuple, setting its Postgres type.
   *
   * @param value the value
   * @param type the type of Postgres datatype.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CopyTuple addValue(Object value, DataType type);

  @Fluent
  @Override
  CopyTuple addValue(Object value);

  /**
   * Add a jsonb value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CopyTuple addVarChar(String value);

  /**
   * Add an array of {@code String} VarChar values at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  CopyTuple addVarCharArray(String[] value);

  /**
   * Add an array of {@code Json} Jsonb values at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  CopyTuple addJsonbArray(Json[] value);

  /**
   * Add a jsonb value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CopyTuple addJsonb(Json value);

  /**
   * Get a json value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Json getJsonb(int pos) {
    return getJson(pos);
  }

  /**
   * Get a String VarChar value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default String getVarChar(int pos) {
    return getString(pos);
  }

  /**
   * Get an array of {@link Json} Jsonb value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore
  default Json[] getJsonbArray(int pos) {
    return getJsonArray(pos);
  }

  /**
   * Get an array of {@link String} VarChar value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore
  default String[] getVarCharArray(int pos) {
    return getStringArray(pos);
  }
}
