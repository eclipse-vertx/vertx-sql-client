package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.data.Json;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen
public interface CopyTuple extends Tuple {

  /**
   * @return a new empty tuple
   */
  static CopyTuple tuple() {
    return new CopyTupleImpl(10);
  }

  /**
   * Create a tuple of one element.
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
   * Create a tuple of two elements.
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
   * Create a tuple of three elements.
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
   * Create a tuple of four elements.
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
   * Create a tuple of five elements.
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
   * Create a tuple of six elements.
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
   * Create a tuple of an arbitrary number of elements.
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

  DataType getDateType(int index);

  CopyTuple addVarChar(String value);

  CopyTuple addVarCharArray(String[] value);

  CopyTuple addJsonb(Json value);

  CopyTuple addJsonbArray(Json[] value);
}
