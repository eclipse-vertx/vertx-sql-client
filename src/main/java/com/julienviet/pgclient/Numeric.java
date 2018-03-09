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
package com.julienviet.pgclient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The Postgres <i>NUMERIC</i> type.
 */
public final class Numeric extends Number {

  /**
   * Constant for the {@code NaN} value.
   */
  public static final Numeric NaN = new Numeric(null);

  private BigDecimal value;

  /**
   * Return a {@code Numeric} instance for the given {@code number}.
   *
   * @param number the number
   * @return the {@code Numeric} value
   */
  public static Numeric create(Number number) {
    if (number instanceof BigDecimal) {
      return new Numeric((BigDecimal) number);
    } else if (number instanceof BigInteger) {
      return new Numeric(new BigDecimal((BigInteger) number));
    } else if (number instanceof Integer || number instanceof Long) {
      return new Numeric(new BigDecimal(number.longValue()));
    } else if (number instanceof Float || number instanceof Double) {
      return new Numeric(new BigDecimal(number.doubleValue()));
    } else {
      return new Numeric(new BigDecimal(number.toString()));
    }
  }

  /**
   * Parse and return a {@code Numeric} instance for the given {@code s}.
   * <p/>
   * The string {@code "Nan"} will return the {@link #NaN} instance.
   *
   * @param s the string
   * @return the {@code Numeric} value
   */
  public static Numeric parse(String s) {
    if (s.equals("NaN")) {
      return NaN;
    } else {
      return new Numeric(new BigDecimal(s));
    }
  }

  private Numeric(BigDecimal value) {
    this.value = value;
  }

  @Override
  public int intValue() {
    return (value == null ? new Double(Double.NaN) : value).intValue();
  }

  @Override
  public long longValue() {
    return (value == null ? new Double(Double.NaN) : value).longValue();
  }

  @Override
  public float floatValue() {
    return value == null ? Float.NaN : value.floatValue();
  }

  @Override
  public double doubleValue() {
    return value == null ? Double.NaN : value.doubleValue();
  }

  /**
   * @return {@code true} when this number represents {@code NaN}
   */
  public boolean isNaN() {
    return value == null;
  }

  /**
   * @return  the numeric value represented by this object after conversion
   *          to type {@code BigDecimal}.
   */
  public BigDecimal bigDecimalValue() {
     return value;
  }

  /**
   * @return  the numeric value represented by this object after conversion
   *          to type {@code BigInteger}.
   */
  public BigInteger bigIntegerValue() {
    return value.toBigInteger();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Numeric && Objects.equals(((Numeric) obj).value, value);
  }

  @Override
  public int hashCode() {
    return value == null ? 0 : value.hashCode();
  }

  @Override
  public String toString() {
    return value == null ? "NaN" : value.toString();
  }
}
