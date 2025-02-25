package io.vertx.tests.db2client.junit;

import java.util.Arrays;

import io.vertx.ext.unit.TestContext;

public class TestUtil {

  public static void assertContains(TestContext ctx, String fullString, String... lookFor) {
    if (lookFor == null || lookFor.length == 0)
      throw new IllegalArgumentException("Must look for at least 1 token");
    ctx.assertNotNull(fullString, "Expected to find '" + lookFor + "' in string, but was null");
    for (String s : lookFor)
      if (fullString.contains(s))
        return; // found
    if (lookFor.length == 1)
      ctx.fail("Expected to find '" + lookFor + "' in string, but was: " + fullString);
    else
      ctx.fail("Expected to find one of " + Arrays.toString(lookFor) + " in string, but was: " + fullString);
  }

}
