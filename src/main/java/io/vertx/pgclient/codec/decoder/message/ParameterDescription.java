package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ParameterDescription implements Message {

  // OIDs
  private final int[] parameterDataTypes;

  public ParameterDescription(int[] parameterDataTypes) {
    this.parameterDataTypes = parameterDataTypes;
  }

  public int[] getParameterDataTypes() {
    return parameterDataTypes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ParameterDescription that = (ParameterDescription) o;
    return Arrays.equals(parameterDataTypes, that.parameterDataTypes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(parameterDataTypes);
  }


  @Override
  public String toString() {
    return "ParameterDescription{" +
      "parameterDataTypes=" + Arrays.toString(parameterDataTypes) +
      '}';
  }
}
