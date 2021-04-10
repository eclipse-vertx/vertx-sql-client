/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class Decimal128Column extends ClickhouseColumn {
  public static final Numeric[] EMPTY_ARRAY = new Numeric[0];

  public static final int ELEMENT_SIZE = 16;
  public static final int MAX_PRECISION = 38;
  public static final MathContext MATH_CONTEXT = new MathContext(MAX_PRECISION, RoundingMode.HALF_EVEN);
  private final Numeric zeroValue;

  public Decimal128Column(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
    zeroValue = Numeric.create(new BigDecimal(BigInteger.ZERO, descriptor.getPrecision(), MATH_CONTEXT));
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new GenericDecimalColumnReader(nRows, descriptor, MATH_CONTEXT);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new GenericDecimalColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    return zeroValue;
  }

  @Override
  public Object[] emptyArray() {
    return EMPTY_ARRAY;
  }
}
