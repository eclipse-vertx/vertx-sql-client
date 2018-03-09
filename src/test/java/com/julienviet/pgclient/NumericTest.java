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

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class NumericTest {

  private Random random = new Random();

  @Test
  public void testCreate() {
    int intValue = random.nextInt();
    long longValue = random.nextLong();
    float floatValue = random.nextFloat();
    double doubleValue = random.nextDouble();
    BigInteger bigIntegerValue = new BigInteger(100, random);
    BigDecimal bigDecimalValue = new BigDecimal(new BigInteger(100, random), 50);
    assertEquals(intValue, Numeric.create(intValue).intValue());
    assertEquals(longValue, Numeric.create(longValue).longValue());
    assertEquals(floatValue, Numeric.create(floatValue).floatValue(), 0.01f);
    assertEquals(doubleValue, Numeric.create(doubleValue).doubleValue(), 0.01f);
    assertEquals(bigIntegerValue, Numeric.create(bigIntegerValue).bigIntegerValue());
    assertEquals(bigDecimalValue, Numeric.create(bigDecimalValue).bigDecimalValue());
  }

  @Test
  public void testParse() {
    BigDecimal bigDecimalValue = new BigDecimal(new BigInteger(100, random), 50);
    assertEquals(Numeric.NaN, Numeric.parse("NaN"));
    assertEquals(Numeric.create(bigDecimalValue), Numeric.parse(bigDecimalValue.toString()));
  }
}
