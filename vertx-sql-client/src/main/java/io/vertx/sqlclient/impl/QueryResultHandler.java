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

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.PropertyKind;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface QueryResultHandler<T> {

  QueryResultHandler<Void> NOOP_HANDLER = new QueryResultHandler<Void>() {
    @Override
    public <V> void addProperty(PropertyKind<V> property, V value) {
    }
    @Override
    public void handleResult(int updatedCount, int size, RowDesc desc, Void result, Throwable failure) {
    }
  };

  <V> void addProperty(PropertyKind<V> property, V value);

  void handleResult(int updatedCount, int size, RowDesc desc, T result, Throwable failure);

}
