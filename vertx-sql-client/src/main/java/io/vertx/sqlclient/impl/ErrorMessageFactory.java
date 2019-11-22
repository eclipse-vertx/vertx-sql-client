package io.vertx.sqlclient.impl;

/**
 * A factory for building error messages.
 */
public class ErrorMessageFactory {
  public static String buildWhenArgumentsLengthNotMatched(int expectedNumber, int actualNumber) {
    return String.format("The number of parameters to execute should be consistent with the expected number of parameters = [%d] but the actual number is [%d].", expectedNumber, actualNumber);
  }

  public static String buildWhenArgumentsTypeNotMatched(Class expectedClass, int pos, Object value) {
    return String.format("Parameter at position[%d] with class = [%s] and value = [%s] can not be coerced to the expected class = [%s] for encoding.", pos, value.getClass().getName(), String.valueOf(value), expectedClass.getName());
  }
}
