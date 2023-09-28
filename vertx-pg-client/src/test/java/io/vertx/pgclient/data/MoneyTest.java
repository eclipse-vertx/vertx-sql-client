package io.vertx.pgclient.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MoneyTest {

  @Parameters
  public static Object[][] data() {
    return new Object[][]{{"-1234.56", -1234L, 56}, {"-0.12", 0L, 12}, {"0.12", 0L, 12}, {"1234.56", 1234L, 56}};
  }

  @Parameter
  public String strValue;
  @Parameter(1)
  public long integralPart;
  @Parameter(2)
  public int fractionalPart;
  private BigDecimal value;
  private Money money;

  @Before
  public void setUp() {
    value = new BigDecimal(strValue);
    money = new Money(value);
  }

  @Test
  public void testBigDecimalValue() {
    assertEquals(value, money.bigDecimalValue());
  }

  @Test
  public void testIntegerPart() {
    assertEquals(integralPart, money.getIntegerPart());
  }

  @Test
  public void testDecimalPart() {
    assertEquals(fractionalPart, money.getDecimalPart());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void setParts() {
    assertEquals(new Money(3.24D), money.setIntegerPart(3).setDecimalPart(24));
    assertEquals(new Money(-173.01D), money.setIntegerPart(-173).setDecimalPart(1));
  }
}
