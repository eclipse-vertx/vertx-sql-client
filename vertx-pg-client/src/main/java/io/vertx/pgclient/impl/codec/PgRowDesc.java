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
package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.impl.RowDesc;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PgRowDesc extends RowDesc {

  static PgRowDesc createBinary(PgColumnDesc[] columns) {
    // Fix to use binary when possible
    return new PgRowDesc(Arrays.stream(columns)
      .map(c -> new PgColumnDesc(
        c.name,
        c.relationId,
        c.relationAttributeNo,
        c.dataType,
        c.length,
        c.typeModifier,
        c.dataType.supportsBinary ? DataFormat.BINARY : DataFormat.TEXT))
      .toArray(PgColumnDesc[]::new));
  }

  static PgRowDesc create(PgColumnDesc[] columns) {
    return new PgRowDesc(columns);
  }

  final PgColumnDesc[] columns;

  private PgRowDesc(PgColumnDesc[] columns) {
    super(Collections.unmodifiableList(Stream.of(columns)
      .map(d -> d.name)
      .collect(Collectors.toList())), Collections.unmodifiableList(Arrays.asList(columns)));
    this.columns = columns;
  }
}
