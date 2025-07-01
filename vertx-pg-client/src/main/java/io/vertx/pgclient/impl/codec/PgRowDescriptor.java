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

import io.vertx.sqlclient.internal.RowDescriptorBase;

class PgRowDescriptor extends RowDescriptorBase {

  static PgRowDescriptor createBinary(PgColumnDesc[] columns) {
    // Fix to use binary when possible
    for (int i = 0; i < columns.length; i++) {
      PgColumnDesc columnDesc = columns[i];
      if (columnDesc.supportsBinary() && columnDesc.hasTextFormat()) {
        columns[i] = columnDesc.toBinaryDataFormat();
      }
    }
    return new PgRowDescriptor(columns);
  }

  static PgRowDescriptor create(PgColumnDesc[] columns) {
    return new PgRowDescriptor(columns);
  }

  final PgColumnDesc[] columns;

  private PgRowDescriptor(PgColumnDesc[] columns) {
    super(columns);
    this.columns = columns;
  }
}
