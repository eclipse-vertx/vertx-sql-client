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

class PgRowDesc extends RowDesc {

  static PgRowDesc createBinary(PgColumnDesc[] columns) {
    // Fix to use binary when possible
    for (int i = 0; i < columns.length; i++) {
      PgColumnDesc columnDesc = columns[i];
      if (columnDesc.supportsBinary() && columnDesc.hasTextFormat()) {
        columns[i] = columnDesc.toBinaryDataFormat();
      }
    }
    return new PgRowDesc(columns);
  }

  static PgRowDesc create(PgColumnDesc[] columns) {
    return new PgRowDesc(columns);
  }

  final PgColumnDesc[] columns;

  private PgRowDesc(PgColumnDesc[] columns) {
    super(columns);
    this.columns = columns;
  }
}
