/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.benchmarks;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Utils {

  public static String[] generateStrings(int size, boolean shuffle, boolean gc) throws InterruptedException {
    String[] strings = new String[size];
    for (int c = 0; c < size; c++) {
      strings[c] = "Value" + c;
    }
    if (shuffle) {
      Collections.shuffle(Arrays.asList(strings), new Random(0xBAD_BEE));
    }
    if (gc) {
      for (int c = 0; c < 5; c++) {
        System.gc();
        TimeUnit.SECONDS.sleep(1);
      }
    }
    return strings;
  }

  private Utils() {
    // Utility
  }
}
