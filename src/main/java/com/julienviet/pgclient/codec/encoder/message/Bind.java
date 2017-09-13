/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.julienviet.pgclient.codec.encoder.message;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * <p>
 * The message gives the name of the prepared statement, the name of portal,
 * and the values to use for any parameter values present in the prepared statement.
 * The supplied parameter set must match those needed by the prepared statement.
 *
 * <p>
 * The response is either {@link BindComplete} or {@link ErrorResponse}.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Bind implements Message {

  private String statement;
  private String portal;
  private byte[][] paramValues;
  private int[] paramFormats;

  public Bind setParamValues(byte[][] paramValues) {
    this.paramValues = paramValues;
    return this;
  }

  public Bind setParamFormats(int[] paramFormats) {
    this.paramFormats = paramFormats;
    return this;
  }

  public int[] getParamFormats() {
    return paramFormats;
  }

  public Bind setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Bind setPortal(String portal) {
    this.portal = portal;
    return this;
  }

  public String getStatement() {
    return statement;
  }

  public String getPortal() {
    return portal;
  }

  public byte[][] getParamValues() {
    return paramValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bind bind = (Bind) o;
    return Objects.equals(statement, bind.statement) &&
      Objects.equals(portal, bind.portal) &&
      Arrays.equals(paramValues, bind.paramValues) &&
      Arrays.equals(paramFormats, bind.paramFormats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statement, portal, paramValues, paramFormats);
  }


  @Override
  public String toString() {
    return "Bind{" +
      "statement='" + statement + '\'' +
      ", portal='" + portal + '\'' +
      ", paramValues=" + Arrays.toString(paramValues) +
      ", paramFormats=" + Arrays.toString(paramFormats) +
      '}';
  }
}
