package io.reactiverse.pgclient;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

class ColumnChecker {

  private static List<SerializableBiFunction<Tuple, Integer, ?>> tupleMethods = new ArrayList<>();
  private static List<SerializableBiFunction<Row, String, ?>> rowMethods = new ArrayList<>();

  static {
    tupleMethods.add(Tuple::getValue);
    rowMethods.add(Row::getValue);
    tupleMethods.add(Tuple::getShort);
    rowMethods.add(Row::getShort);
    tupleMethods.add(Tuple::getInteger);
    rowMethods.add(Row::getInteger);
    tupleMethods.add(Tuple::getLong);
    rowMethods.add(Row::getLong);
    tupleMethods.add(Tuple::getFloat);
    rowMethods.add(Row::getFloat);
    tupleMethods.add(Tuple::getDouble);
    rowMethods.add(Row::getDouble);
    tupleMethods.add(Tuple::getBigDecimal);
    rowMethods.add(Row::getBigDecimal);
    tupleMethods.add(Tuple::getNumeric);
    rowMethods.add(Row::getNumeric);
    tupleMethods.add(Tuple::getString);
    rowMethods.add(Row::getString);
    tupleMethods.add(Tuple::getCharacter);
    rowMethods.add(Row::getCharacter);
    tupleMethods.add(Tuple::getBoolean);
    rowMethods.add(Row::getBoolean);
    tupleMethods.add(Tuple::getJsonObject);
    rowMethods.add(Row::getJsonObject);
    tupleMethods.add(Tuple::getJsonArray);
    rowMethods.add(Row::getJsonArray);
    tupleMethods.add(Tuple::getBuffer);
    rowMethods.add(Row::getBuffer);
    tupleMethods.add(Tuple::getBuffer);
    rowMethods.add(Row::getBuffer);
    tupleMethods.add(Tuple::getTemporal);
    rowMethods.add(Row::getTemporal);
    tupleMethods.add(Tuple::getLocalDate);
    rowMethods.add(Row::getLocalDate);
    tupleMethods.add(Tuple::getLocalTime);
    rowMethods.add(Row::getLocalTime);
    tupleMethods.add(Tuple::getOffsetTime);
    rowMethods.add(Row::getOffsetTime);
    tupleMethods.add(Tuple::getLocalDateTime);
    rowMethods.add(Row::getLocalDateTime);
    tupleMethods.add(Tuple::getOffsetDateTime);
    rowMethods.add(Row::getOffsetDateTime);
    tupleMethods.add(Tuple::getUUID);
    rowMethods.add(Row::getUUID);
  }

  static ColumnChecker checkColumn(int index, String name) {
    return new ColumnChecker(index, name);
  }

  private final List<Method> blackList = new ArrayList<>();
  private final List<Consumer<? super Row>> expects = new ArrayList<>();
  private final int index;
  private final String name;

  private ColumnChecker(int index, String name) {
    this.index = index;
    this.name = name;
  }

  <R> ColumnChecker returns(SerializableBiFunction<Tuple, Integer, R> byIndexGetter,
                            SerializableBiFunction<Row, String, R> byNameGetter,
                            R expected) {
    Method byIndexMeth = byIndexGetter.method();
    blackList.add(byIndexMeth);
    Method byNameMeth = byNameGetter.method();
    blackList.add(byNameMeth);
    expects.add(row -> {
      Object actual = byIndexGetter.apply(row, index);
      assertEquals("Expected that " + byIndexMeth + " returns " + expected + " instead of " + actual, actual, expected);
      actual = byNameGetter.apply(row, name);
      assertEquals("Expected that " + byNameMeth + " returns " + expected + " instead of " + actual, actual, expected);
    });
    return this;
  }

  ColumnChecker returns(SerializableBiFunction<Tuple, Integer, Double> byIndexGetter,
                            SerializableBiFunction<Row, String, Double> byNameGetter,
                        double expected, double delta) {
    blackList.add(byIndexGetter.method());
    blackList.add(byNameGetter.method());
    expects.add(row -> {
      Object actual = byIndexGetter.apply(row, index);
      assertEquals("Expected that " + byIndexGetter.method() + " returns " + expected + " instead of " + actual, (double)actual, expected, delta);
      actual = byNameGetter.apply(row, name);
      assertEquals("Expected that " + byNameGetter.method() + " returns " + expected + " instead of " + actual, (double)actual, expected, delta);
    });
    return this;
  }

  ColumnChecker returns(SerializableBiFunction<Tuple, Integer, Float> byIndexGetter,
                        SerializableBiFunction<Row, String, Float> byNameGetter,
                        float expected, float delta) {
    blackList.add(byIndexGetter.method());
    blackList.add(byNameGetter.method());
    expects.add(row -> {
      Object actual = byIndexGetter.apply(row, index);
      assertEquals("Expected that " + byIndexGetter.method() + " returns " + expected + " instead of " + actual, (float)actual, expected, delta);
      actual = byNameGetter.apply(row, name);
      assertEquals("Expected that " + byNameGetter.method() + " returns " + expected + " instead of " + actual, (float)actual, expected, delta);
    });
    return this;
  }

  <R> ColumnChecker fails(SerializableBiFunction<Tuple, Integer, R> byIndexGetter,
                            SerializableBiFunction<Row, String, R> byNameGetter) {
    blackList.add(byIndexGetter.method());
    blackList.add(byNameGetter.method());
    expects.add(row -> {
      try {
        byIndexGetter.apply(row, index);
        fail("Expected that " + byIndexGetter.method() + " would throw an exception");
      } catch (Exception ignore) {
      }
      try {
        byNameGetter.apply(row, name);
        fail("Expected that " + byNameGetter.method() + " would throw an exception");
      } catch (Exception ignore) {
      }
    });
    return this;
  }

  void forRow(Row row) {
    for (SerializableBiFunction<Tuple, Integer, ?> m : tupleMethods) {
      if (!blackList.contains(m.method())) {
        Object v = m.apply(row, index);
        try {
          assertNull("Was expecting null for " + m.method() + " instead of " + v, v);
        } catch (Throwable e) {
          e.printStackTrace();
          throw e;
        }
      }
    }
    for (SerializableBiFunction<Row, String, ?> m : rowMethods) {
      if (!blackList.contains(m.method())) {
        Object v = m.apply(row, name);
        assertNull(v);
      }
    }
    for (Consumer<? super Row> e : expects) {
      e.accept(row);
    }
  }

  interface MethodReferenceReflection {

    //inspired by: http://benjiweber.co.uk/blog/2015/08/17/lambda-parameter-names-with-reflection/

    default SerializedLambda serialized() {
      try {
        Method replaceMethod = getClass().getDeclaredMethod("writeReplace");
        replaceMethod.setAccessible(true);
        return (SerializedLambda) replaceMethod.invoke(this);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    default Class getContainingClass() {
      try {
        String className = serialized().getImplClass().replaceAll("/", ".");
        return Class.forName(className);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    default Method method() {
      SerializedLambda lambda = serialized();
      Class containingClass = getContainingClass();
      return Arrays.stream(containingClass.getDeclaredMethods())
        .filter(method -> Objects.equals(method.getName(), lambda.getImplMethodName()))
        .findFirst()
        .orElseThrow(MethodReferenceReflection.UnableToGuessMethodException::new);
    }

    class UnableToGuessMethodException extends RuntimeException {}
  }

  interface SerializableBiFunction<O, T, R> extends BiFunction<O, T, R>, Serializable, MethodReferenceReflection {}
}
