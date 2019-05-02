/*
 * Copyright (C) 2018 Julien Viet
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
package io.reactiverse.pgclient.data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The Postgres <i>NUMERIC</i> type.
 */
public final class Numeric extends Number {

  /**
   * Constant for the {@code NaN} value.
   */
  public static final Numeric NaN = new Numeric(Double.NaN);

  private final Number value;

  /**
   * Return a {@code Numeric} instance for the given {@code number}.
   * <p/>
   * Null values or infinite {@code Double} or {@code Float} are rejected.
   *
   * @param number the number
   * @return the {@code Numeric} value
   * @throws NumberFormatException when the number is infinite
   */
  public static Numeric create(Number number) {
    if (number == null) {
      throw new NullPointerException();
    }
    if (number instanceof Double && ((Double)number).isInfinite() || number instanceof Float && ((Float)number).isInfinite()) {
      throw new NumberFormatException("Infinite numbers are not valid numerics");
    }
    return new Numeric(number);
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
    switch (s) {
      case "NaN":
        return NaN;
      default:
        return new Numeric(new BigDecimal(s));
    }
  }

  private Numeric(Number value) {
    this.value = value;
  }

  @Override
  public short shortValue() {
    return value.shortValue();
  }

  @Override
  public int intValue() {
    return value.intValue();
  }

  @Override
  public long longValue() {
    return value.longValue();
  }

  @Override
  public float floatValue() {
    return value.floatValue();
  }

  @Override
  public double doubleValue() {
    return value.doubleValue();
  }

  /**
   * @return {@code true} when this number represents {@code NaN}
   */
  public boolean isNaN() {
    return value instanceof Double && ((Double)value).isNaN() || value instanceof Float && ((Float)value).isNaN();
  }

  /**
   * @return  the numeric value represented by this object after conversion
   *          to type {@code BigDecimal}. It can be {@code null} when this instance
   *          represents the {@code NaN} value.
   */
  public BigDecimal bigDecimalValue() {
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    } else if (value instanceof BigInteger) {
      return new BigDecimal((BigInteger)value);
    } else if (isNaN()) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }

  /**
   * @return  the numeric value represented by this object after conversion
   *          to type {@code BigInteger}. It can be {@code null} when this instance
   *          represents the {@code NaN} value.
   */
  public BigInteger bigIntegerValue() {
    if (value instanceof BigInteger) {
      return (BigInteger) value;
    } else if (value instanceof BigDecimal) {
      return ((BigDecimal)value).toBigInteger();
    } else if (isNaN()) {
      return null;
    } else {
      return new BigInteger(Long.toString(value.longValue()));
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Numeric) {
      Numeric that = (Numeric) obj;
      if (value.getClass() == that.value.getClass()) {
        return value.equals(that.value);
      } else {
        BigDecimal l = bigDecimalValue();
        BigDecimal r = that.bigDecimalValue();
        if (l == null) {
          return r == null;
        } else if (r == null) {
          return false;
        }
        return l.compareTo(r) == 0;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return intValue();
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
