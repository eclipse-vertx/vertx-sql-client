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
import java.math.BigInteger;

/**
 * The PostgreSQL <a href="https://www.postgresql.org/docs/9.1/datatype-money.html">MONEY</> type.
 *
 * This has the {@link #getIntegerPart() integer part} and {@link #getDecimalPart() decimal part} of the value without loss of information.
 *
 * {@link #bigDecimalValue()} returns the value without loss of information
 * {@link #doubleValue()} ()} returns the value possible loss of information
 */
public class Money {

  private long integerPart;
  private int decimalPart;

  public Money(long integerPart, int decimalPart) {
    setIntegerPart(integerPart);
    setDecimalPart(decimalPart);
  }

  public Money(Number value) {
    if (value instanceof Double || value instanceof Float) {
      value = BigDecimal.valueOf((double) value);
    }
    if (value instanceof BigDecimal) {
      BigInteger bd = ((BigDecimal) value).multiply(new BigDecimal(100)).toBigInteger();
      setIntegerPart(bd.divide(BigInteger.valueOf(100)).longValueExact());
      setDecimalPart(bd.remainder(BigInteger.valueOf(100)).abs().intValueExact());
    } else {
      setIntegerPart(value.longValue());
    }
  }

  public Money() {
  }

  public long getIntegerPart() {
    return integerPart;
  }

  public int getDecimalPart() {
    return decimalPart;
  }

  /**
   * Set the integer part of the monetary value.
   *
   * <p> This value must belong to the range {@code ]Long.MAX_VALUE / 100, Long.MIN_VALUE / 100[}
   *
   * @param part the integer part of the value
   * @return this object
   */
  public Money setIntegerPart(long part) {
    if (part > Long.MAX_VALUE / 100 || part < Long.MIN_VALUE / 100) {
      throw new IllegalArgumentException();
    }
    integerPart = part;
    return this;
  }

  /**
   * Set the decimal part of the monetary value.
   *
   * <p> This value must belong to the range {@code [0, 100]}
   *
   * @param part decimal part
   * @return this object
   */
  public Money setDecimalPart(int part) {
    if (part > 99 || part < 0) {
      throw new IllegalArgumentException();
    }
    decimalPart = part;
    return this;
  }

  /**
   * @return the monetary amount as a big decimal without loss of information
   */
  public BigDecimal bigDecimalValue() {
    BigDecimal value = new BigDecimal(integerPart).multiply(BigDecimal.valueOf(100));
    if (integerPart >= 0) {
      value = value.add(BigDecimal.valueOf(decimalPart));
    } else {
      value = value.subtract(BigDecimal.valueOf(decimalPart));
    }
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
    Money that = (Money) o;
    return decimalPart == that.decimalPart && integerPart == that.integerPart;
  }

  @Override
  public int hashCode() {
    return ((Long)integerPart).hashCode() ^ ((Integer)decimalPart).hashCode();
  }

  @Override
  public String toString() {
    return "Money(" + integerPart + "." + decimalPart + ")";
  }
}
