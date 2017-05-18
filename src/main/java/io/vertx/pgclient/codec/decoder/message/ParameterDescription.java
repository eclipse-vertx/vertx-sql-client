package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ParameterDescription implements Message {

  // OIDs
  private final int[] paramDataTypes;

  public ParameterDescription(int[] paramDataTypes) {
    this.paramDataTypes = paramDataTypes;
  }

  public int[] getParamDataTypes() {
    return paramDataTypes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ParameterDescription that = (ParameterDescription) o;
    return Arrays.equals(paramDataTypes, that.paramDataTypes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(paramDataTypes);
  }


  @Override
  public String toString() {
    return "ParameterDescription{" +
      "paramDataTypes=" + Arrays.toString(paramDataTypes) +
      '}';
  }
}
