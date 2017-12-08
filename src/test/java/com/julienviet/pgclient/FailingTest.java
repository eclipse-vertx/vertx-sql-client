package com.julienviet.pgclient;

import org.junit.Test;

public class FailingTest {

  @Test
  public void testFoo() {
    throw new RuntimeException("Break the build purposely for testing CI");
  }
}
