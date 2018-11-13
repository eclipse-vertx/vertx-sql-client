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
   * Create a CopyTuple of one element.
   *
   * @param elt1 the first value
   * @return the tuple
   */
  static CopyTuple of(CopyData elt1) {
    CopyTuple tuple = new CopyTupleImpl(1);
    tuple.addValue(elt1);
    return tuple;
  }

  /**
   * Create a CopyTuple of two elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @return the tuple
   */
  static CopyTuple of(CopyData elt1, CopyData elt2) {
    CopyTuple tuple = new CopyTupleImpl(2);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    return tuple;
  }

  /**
   * Create a CopyTuple of three elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @return the tuple
   */
  static CopyTuple of(CopyData elt1, CopyData elt2, CopyData elt3) {
    CopyTuple tuple = new CopyTupleImpl(3);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    return tuple;
  }

  /**
   * Create a CopyTuple of four elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @param elt4 the fourth value
   * @return the tuple
   */
  static CopyTuple of(CopyData elt1, CopyData elt2, CopyData elt3, CopyData elt4) {
    CopyTuple tuple = new CopyTupleImpl(4);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    tuple.addValue(elt4);
    return tuple;
  }

  /**
   * Create a CopyTuple of five elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @param elt4 the fourth value
   * @param elt5 the fifth value
   * @return the tuple
   */
  static CopyTuple of(CopyData elt1, CopyData elt2, CopyData elt3, CopyData elt4, CopyData elt5) {
    CopyTuple tuple = new CopyTupleImpl(5);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    tuple.addValue(elt4);
    tuple.addValue(elt5);
    return tuple;
  }

  /**
   * Create a CopyTuple of six elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @param elt4 the fourth value
   * @param elt5 the fifth value
   * @param elt6 the sixth value
   * @return the tuple
   */
  static CopyTuple of(CopyData elt1, CopyData elt2, CopyData elt3, CopyData elt4, CopyData elt5, CopyData elt6) {
    CopyTuple tuple = new CopyTupleImpl(5);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    tuple.addValue(elt4);
    tuple.addValue(elt5);
    tuple.addValue(elt6);
    return tuple;
  }

  /**
   * Create a CopyTuple of an arbitrary number of elements.
   *
   * @param elt1 the first element
   * @param elts the remaining elements
   * @return the tuple
   */
  @GenIgnore
  static CopyTuple of(CopyData elt1, CopyData... elts) {
    CopyTuple tuple = new CopyTupleImpl(1 + elts.length);
    tuple.addValue(elt1);
    for (Object elt: elts) {
      tuple.addValue(elt);
    }
    return tuple;
  }

  /**
   * Gets the {@link DataType} at {@code pos}.
   * @param pos the position
   * @return the {@link DataType} or {@link DataType#UNKNOWN}
   */
  DataType getDataType(int pos);

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
