/*
 * Copyright (C) 2022 Long Dinh
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

package io.vertx.pgclient.impl;

import io.vertx.pgclient.impl.codec.DataType;
import io.vertx.sqlclient.impl.RowDesc;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class RowImplTest {
  @Test
  public void testGetNullEnum() {
    RowImpl row = new RowImpl(new RowDesc(Collections.singletonList("enum")));
    row.addValue(null);
    assertNull(row.get(DataType.class, 0));

    row.addValue(LocalDate.now());
    assertThrows(ClassCastException.class, () -> row.get(DataType.class, 1));
  }
}
