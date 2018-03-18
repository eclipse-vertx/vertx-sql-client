package com.julienviet.pgclient;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

class ColumnChecker {

  private static List<SerializableBiFunction<Tuple, Integer, ?>> tupleMethods = new ArrayList<>();
  private static List<SerializableBiFunction<Row, String, ?>> rowMethods = new ArrayList<>();

  static {
    tupleMethods.add(Tuple::getValue);
    rowMethods.add(Row::getValue);
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
    return new ColumnChecker(null, index, name);
  }

  private final Set<Method> blackList = new HashSet<>();
  private final List<Expect<?, ?>> expects = new ArrayList<>();
  private final ColumnChecker previous;
  private final int index;
  private final String name;

  private ColumnChecker(ColumnChecker previous, int index, String name) {
    this.previous = previous;
    this.index = index;
    this.name = name;
  }

  <R> ColumnChecker returns(SerializableBiFunction<Tuple, Integer, R> byIndexGetter,
                            SerializableBiFunction<Row, String, R> byNameGetter,
                            R val) {
    blackList.add(byIndexGetter.method());
    blackList.add(byNameGetter.method());
    expects.add(new Expect<>(byIndexGetter, index, val));
    expects.add(new Expect<>(byNameGetter, name, val));
    return this;
  }

  ColumnChecker andCheckThatColumn(int index, String name) {
    return new ColumnChecker(this, index, name);
  }

  void forRow(Row row) {
    for (SerializableBiFunction<Tuple, Integer, ?> m : tupleMethods) {
      if (!blackList.contains(m.method())) {
        Object v = m.apply(row, index);
        assertNull(v);
      }
    }
    for (SerializableBiFunction<Row, String, ?> m : rowMethods) {
      if (!blackList.contains(m.method())) {
        Object v = m.apply(row, name);
        assertNull(v);
      }
    }
    for (Expect<?, ?> e : expects) {
      e.check(row);
    }
    if (previous != null) {
      previous.forRow(row);
    }
  }

  private static class Expect<T, R> {

    final SerializableBiFunction<? super Row, T, R> bifunc;
    final T key;
    final R expected;

    Expect(SerializableBiFunction<? super Row, T, R> bifunc, T key, R expected) {
      this.bifunc = bifunc;
      this.key = key;
      this.expected = expected;
    }

    void check(Row o) {
      Object actual = bifunc.apply(o, key);
      assertEquals("Expected that " + bifunc.method() + " return " + expected + " instead of " + actual, actual, expected);
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
