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

package io.vertx.sqlclient.internal;

import io.vertx.sqlclient.desc.RowDescriptor;

import java.util.ArrayList;
import java.util.List;

public interface PreparedStatement {

  RowDescriptor rowDesc();

  String sql();

  default List<TupleBase> prepare(List<TupleBase> batch) throws Exception {
    List<TupleBase> actual = batch;
    int size = actual.size();
    for (int idx = 0;idx < size;idx++) {
      TupleBase values = batch.get(idx);
      TupleBase prepared = prepare(values);
      if (prepared != values) {
        if (batch == actual) {
          actual = new ArrayList<>(actual);
        }
        actual.set(idx, prepared);
      }
    }
    return actual;
  }

  default TupleBase prepare(TupleBase values) throws Exception {
    return values;
  }
}
