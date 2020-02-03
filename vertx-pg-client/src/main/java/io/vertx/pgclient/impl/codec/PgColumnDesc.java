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

import io.vertx.pgclient.impl.datatype.DataType;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class PgColumnDesc {

  final String name;
  final int relationId;
  final DataType dataType;
  final DataFormat dataFormat; // are we sure that ????
  final short relationAttributeNo;
  final short length;
  final int typeModifier;

  PgColumnDesc(String name, int relationId, short relationAttributeNo, DataType dataType, short length, int typeModifier, DataFormat dataFormat) {
    this.name = name;
    this.dataType = dataType;
    this.dataFormat = dataFormat;
    this.length = length;
    this.relationId = relationId;
    this.relationAttributeNo = relationAttributeNo;
    this.typeModifier = typeModifier;
  }
}
