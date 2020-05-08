/*
 * Copyright (C) 2020 IBM Corporation
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
 */
package io.vertx.db2client.impl.drda;

import java.sql.RowId;
import java.util.Arrays;
import java.util.Objects;

public class DB2RowId implements RowId {
  
  private final byte[] bytes;
  
  public DB2RowId(byte[] bytes) {
    Objects.requireNonNull(bytes);
    this.bytes = bytes;
  }

  @Override
  public byte[] getBytes() {
    return bytes;
  }
  
  @Override
  public String toString() {
    return Arrays.toString(bytes);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof DB2RowId))
      return false;
    DB2RowId other = (DB2RowId) obj;
    return Arrays.equals(bytes, other.bytes);
  }
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

}
