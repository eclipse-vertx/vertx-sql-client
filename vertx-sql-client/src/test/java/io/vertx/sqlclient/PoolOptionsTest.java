package io.vertx.sqlclient;

import static org.junit.Assert.*;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class PoolOptionsTest {
  @Test
  public void numberOfPoolOptions() {
    JsonObject json = new JsonObject();
    PoolOptionsConverter.toJson(new PoolOptions(), json);
    // if fields change equals() and hasCode() must change
    assertEquals(json.fieldNames().toString(), 3, json.fieldNames().size());
  }

  @Test
  public void test() {
    PoolOptions poolOptions = new PoolOptions();
    assertHashAndEquals(true, poolOptions, new PoolOptions());
    assertHashAndEquals(false, poolOptions, new PoolOptions().setMaxSize(11));
    assertHashAndEquals(false, poolOptions, new PoolOptions().setMaxWaitQueueSize(11));
    assertHashAndEquals(false, poolOptions, new PoolOptions().setConnectionReleaseDelay(11));
  }

  private void assertHashAndEquals(boolean equalExpected, PoolOptions poolOptions1, PoolOptions poolOptions2) {
    if (equalExpected) {
      assertTrue(poolOptions1.equals(poolOptions2));
      assertEquals(poolOptions2.hashCode(), poolOptions1.hashCode());
    } else {
      assertFalse(poolOptions1.equals(poolOptions2));
      assertNotEquals(poolOptions2.hashCode(), poolOptions1.hashCode());
    }
  }
}
