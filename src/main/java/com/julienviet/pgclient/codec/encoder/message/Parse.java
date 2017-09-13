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
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p>
 * The message contains a textual SQL query string.
 *
 * <p>
 * The response is either {@link ParseComplete} or {@link ErrorResponse}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Parse implements Message {

  private final String query;
  private String statement;
  private int[] paramDataTypes;

  public Parse(String query) {
    this.query = query;
  }

  public Parse setStatement(String statement) {
    this.statement = statement;
    return this;
  }

  public Parse setParamDataTypes(int[] paramDataTypes) {
    this.paramDataTypes = paramDataTypes;
    return this;
  }

  public String getStatement() {
    return statement;
  }

  public String getQuery() {
    return query;
  }

  public int[] getParamDataTypes() {
    return paramDataTypes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Parse parse = (Parse) o;
    return Objects.equals(query, parse.query) &&
      Objects.equals(statement, parse.statement) &&
      Arrays.equals(paramDataTypes, parse.paramDataTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, statement, paramDataTypes);
  }


  @Override
  public String toString() {
    return "Parse{" +
      "query='" + query + '\'' +
      ", statement='" + statement + '\'' +
      ", paramDataTypes=" + Arrays.toString(paramDataTypes) +
      '}';
  }
}
