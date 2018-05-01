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

package io.reactiverse.pgclient.impl.codec;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ColumnDesc {

  private final String name;
  private final int relationId;
  private final DataType dataType;
  private final DataFormat dataFormat; // are we sure of taht ????
  private final short relationAttributeNo;
  private final short length;
  private final int typeModifier;

  public ColumnDesc(String name, int relationId, short relationAttributeNo, DataType dataType, short length, int typeModifier, DataFormat dataFormat) {
    this.name = name;
    this.dataType = dataType;
    this.dataFormat = dataFormat;
    this.length = length;
    this.relationId = relationId;
    this.relationAttributeNo = relationAttributeNo;
    this.typeModifier = typeModifier;
  }

  public String getName() {
    return name;
  }

  public DataType getDataType() {
    return dataType;
  }

  public DataFormat getDataFormat() {
    return dataFormat;
  }

  public int getRelationId() {
    return relationId;
  }

  public short getRelationAttributeNo() {
    return relationAttributeNo;
  }

  public short getLength() {
    return length;
  }

  public int getTypeModifier() {
    return typeModifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ColumnDesc column = (ColumnDesc) o;
    return relationId == column.relationId &&
      relationAttributeNo == column.relationAttributeNo &&
      length == column.length &&
      typeModifier == column.typeModifier &&
      Objects.equals(name, column.name) &&
      dataType == column.dataType &&
      dataFormat == column.dataFormat;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dataType, dataFormat, relationId, relationAttributeNo, length, typeModifier);
  }


  @Override
  public String toString() {
    return "Column{" +
      "name='" + name + '\'' +
      ", dataType=" + dataType +
      ", dataFormat=" + dataFormat +
      ", relationId=" + relationId +
      ", relationAttributeNo=" + relationAttributeNo +
      ", length=" + length +
      ", typeModifier=" + typeModifier +
      '}';
  }
}
