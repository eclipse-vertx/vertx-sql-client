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

import org.junit.Assert;
import org.junit.Test;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class TupleTest {

  @Test
  public void testOf() {
    Tuple tuple = Tuple.of(
      ZonedDateTime.now(),
      Instant.now(),
      ZoneId.of("UTC"),
      new Date(),
      Calendar.getInstance(),
      ZoneOffset.UTC,
      Time.valueOf(LocalTime.MAX),
      Timestamp.from(Instant.EPOCH)
    );
    Assert.assertEquals(8, tuple.size());
    Assert.assertNull(tuple.getValue(0));
    Assert.assertNull(tuple.getValue(1));
    Assert.assertNull(tuple.getValue(2));
    Assert.assertNull(tuple.getValue(3));
    Assert.assertNull(tuple.getValue(4));
    Assert.assertNull(tuple.getValue(5));
    Assert.assertNull(tuple.getValue(6));
    Assert.assertNull(tuple.getValue(7));
  }
}
