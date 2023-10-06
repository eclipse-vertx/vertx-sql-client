/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.pgclient.data;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * The PostgreSQL <a href="https://www.postgresql.org/docs/9.1/datatype-money.html">MONEY</> type.
 * <p>
 * {@link #bigDecimalValue()} returns the value without loss of information.
 * {@link #doubleValue()} returns the value possible loss of information.
 */
public class Money {

  private BigDecimal value;

  /**
   * @deprecated as of 4.4.6, use {@link #Money(Number)} instead
   */
  @Deprecated
  public Money(long integerPart, int decimalPart) {
    this(new BigDecimal(integerPart + "." + new DecimalFormat("00").format(decimalPart)));
  }

  public Money(Number value) {
    this.value = (value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(String.valueOf(value))).stripTrailingZeros();
    if (this.value.toBigInteger().abs().longValue() > Long.MAX_VALUE / 100) {
      throw new IllegalArgumentException("Value is too big: " + value);
    }
    if (this.value.scale() > 2) {
      throw new IllegalArgumentException("Value has more than two decimal digits: " + value);
    }
  }

  public Money() {
    value = BigDecimal.ZERO;
  }

  /**
   * @deprecated as of 4.4.6, use {@link #bigDecimalValue()} instead
   */
  @Deprecated
  public long getIntegerPart() {
    return value.toBigInteger().longValue();
  }

  /**
   * @deprecated as of 4.4.6, use {@link #bigDecimalValue()} instead
   */
  @Deprecated
  public int getDecimalPart() {
    return value.remainder(BigDecimal.ONE).movePointRight(value.scale()).abs().intValue();
  }

  /**
   * @deprecated as of 4.4.6, create another instance instead
   */
  @Deprecated
  public Money setIntegerPart(long part) {
    value = new Money(part, value.remainder(BigDecimal.ONE).abs().intValue()).bigDecimalValue();
    return this;
  }

  /**
   * @deprecated as of 4.4.6, create another instance instead
   */
  @Deprecated
  public Money setDecimalPart(int part) {
    value = new Money(value.longValue(), part).bigDecimalValue();
    return this;
  }

  /**
   * @return the monetary amount as a big decimal without loss of information
   */
  public BigDecimal bigDecimalValue() {
    return value;
  }

  /**
   * @return the monetary amount as a double with possible loss of information
   */
  public double doubleValue() {
    return bigDecimalValue().doubleValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return Objects.equals(value, money.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "Money(" + new DecimalFormat("#0.##").format(value) + ")";
  }
}
