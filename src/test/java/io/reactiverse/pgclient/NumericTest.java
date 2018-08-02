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
package io.reactiverse.pgclient;

import io.reactiverse.pgclient.data.Numeric;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public class NumericTest {

  private Random random = new Random();
  private BigDecimal bigDecimalValue = new BigDecimal(new BigInteger(100, random), 50);
  private BigInteger bigIntegerValue = new BigInteger(100, random);
  private long longValue = random.nextLong();
  private short shortValue = (short) random.nextInt();
  private int intValue = random.nextInt();
  private double doubleValue = random.nextDouble();
  private float floatValue = random.nextFloat();

  @Test
  public void testCreate() {
    assertEquals(shortValue, Numeric.create(shortValue).shortValue());
    assertEquals(intValue, Numeric.create(intValue).intValue());
    assertEquals(longValue, Numeric.create(longValue).longValue());
    assertEquals(floatValue, Numeric.create(floatValue).floatValue(), 0.01f);
    assertEquals(doubleValue, Numeric.create(doubleValue).doubleValue(), 0.01f);
    assertEquals(bigIntegerValue, Numeric.create(bigIntegerValue).bigIntegerValue());
    assertEquals(bigDecimalValue, Numeric.create(bigDecimalValue).bigDecimalValue());
  }

  @Test
  public void testParse() {
    assertEquals(Numeric.NaN, Numeric.parse("NaN"));
    assertEquals(Numeric.create(bigDecimalValue), Numeric.parse(bigDecimalValue.toString()));
    assertEquals(Numeric.create(bigIntegerValue), Numeric.parse(bigIntegerValue.toString()));
    assertEquals(Numeric.create(longValue), Numeric.parse("" + longValue));
    assertEquals(Numeric.create(shortValue), Numeric.parse("" + shortValue));
    assertEquals(Numeric.create(intValue), Numeric.parse("" + intValue));
    assertEquals(Numeric.create(floatValue), Numeric.parse("" + floatValue));
    assertEquals(Numeric.create(doubleValue), Numeric.parse("" + doubleValue));
  }

  @Test
  public void testMethods() {
    Number[] numbers = {
      Double.NaN,
      Double.NaN,
      Float.NaN,
      bigDecimalValue,
      bigIntegerValue,
      longValue,
      shortValue,
      intValue,
      doubleValue,
      floatValue,
    };
    Numeric[] test = {
      Numeric.NaN,
      Numeric.create(Double.NaN),
      Numeric.create(Float.NaN),
      Numeric.create(bigDecimalValue),
      Numeric.create(bigIntegerValue),
      Numeric.create(longValue),
      Numeric.create(shortValue),
      Numeric.create(intValue),
      Numeric.create(doubleValue),
      Numeric.create(floatValue),
    };
    for (int i = 0;i < numbers.length;i++) {
      assertEquals(Double.isNaN(numbers[i].doubleValue()), test[i].isNaN());
      assertEquals(numbers[i].byteValue(), test[i].byteValue());
      assertEquals(numbers[i].intValue(), test[i].intValue());
      assertEquals(numbers[i].shortValue(), test[i].shortValue());
      assertEquals(numbers[i].longValue(), test[i].longValue());
      assertEquals(numbers[i].floatValue(), test[i].floatValue(), 0.01f);
      assertEquals(numbers[i].doubleValue(), test[i].doubleValue(), 0.01d);
      assertEquals(Numeric.create(numbers[i]), test[i]);
      assertEquals(test[i], Numeric.create(numbers[i]));
      assertEquals(numbers[i].toString(), test[i].toString());
    }
  }

  @Test
  public void testFormatException() {
    Consumer<Runnable> checker = r -> {
      try {
        r.run();
        fail();
      } catch (NumberFormatException ignore) {
      }
    };
    for (Number number : Arrays.asList(
      Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY,
      Float.POSITIVE_INFINITY,
      Float.NEGATIVE_INFINITY)) {
      checker.accept(() -> {
        Numeric.create(number);
      });
    }
    checker.accept(() -> {
      Numeric.parse("foobar");
    });
  }

  @Test
  public void testNull() {
    Function<Number, Numeric> f1 = Numeric::create;
    Function<String, Numeric> f2 = Numeric::parse;
    for (Function<?, ?> c : Arrays.<Function<?, ?>>asList(f1, f2))
    try {
      c.apply(null);
      fail();
    } catch (NullPointerException ignore) {
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    int intValue = random.nextInt(1000);
    Numeric[] numerics = {
      Numeric.create(intValue),
      Numeric.create((short)intValue),
      Numeric.create((double)intValue),
      Numeric.create((long)intValue),
      Numeric.create((float)intValue),
      Numeric.create(new BigInteger("" + intValue)),
      Numeric.create(new BigDecimal("" + intValue)),
    };
    for (Numeric l : numerics) {
      for (Numeric r : numerics) {
        assertEquals(l, r);
        assertEquals(l.hashCode(), r.hashCode());
      }
    }
    assertEquals(Numeric.create(Double.NaN), Numeric.create(Float.NaN));
    for (Numeric l : numerics) {
      assertNotSame(Numeric.NaN, l);
      assertNotSame(l, Numeric.NaN);
      assertNotSame(Numeric.create(Float.NaN), l);
      assertNotSame(l, Numeric.create(Float.NaN));
      assertNotSame(Numeric.create(Double.NaN), l);
      assertNotSame(l, Numeric.create(Double.NaN));
    }
  }

  /**
   * Compute the lenght of the array in bytes.
   *
   * @param size
   * @return
   */
  private static int len(int size) {
    int d = (int) (((double)size * Math.log(10000) / Math.log(2)));
    return d;
  }

  @Test
  public void testFoo() {



  }
}
