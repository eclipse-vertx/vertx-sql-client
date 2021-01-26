/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.typecodec;

import io.netty.util.collection.IntObjectHashMap;
import io.vertx.sqlclient.codec.DataType;
import io.vertx.sqlclient.codec.DataTypeCodec;
import io.vertx.sqlclient.codec.DataTypeCodecRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MySQLDataTypeCodecRegistry implements DataTypeCodecRegistry {

  private final IntObjectHashMap<DataTypeCodec<?, ?>> registries = new IntObjectHashMap<>();
  private final Map<Class<?>, DataTypeCodec<?, ?>> encodingTypeMapping = new HashMap<>();

  public static final MySQLDataTypeCodecRegistry INSTANCE = new MySQLDataTypeCodecRegistry();

  private MySQLDataTypeCodecRegistry() {
    registries.put(MySQLType.TINYINT.identifier(), MySQLDataTypeDefaultCodecs.TinyIntTypeCodec.INSTANCE);
    registries.put(MySQLType.UNSIGNED_TINYINT.identifier(), MySQLDataTypeDefaultCodecs.UnsignedTinyIntCodec.INSTANCE);

    encodingTypeMapping.put(Byte.class, MySQLDataTypeDefaultCodecs.TinyIntTypeCodec.INSTANCE);
  }

  @Override
  public void register(DataTypeCodec<?, ?> dataTypeCodec) {
    DataType<?, ?> dataType = dataTypeCodec.dataType();
    if (dataType == null) {
      throw new IllegalArgumentException(); // TODO errmsg
    }
    DataTypeCodec<?, ?> currentCodec = registries.get(dataType.identifier());
    if (currentCodec != null) {
      throw new IllegalArgumentException(); // TODO errmsg
    }

    Class<?> encodingJavaClass = dataType.encodingJavaClass();
    DataTypeCodec<?, ?> encodingCodec = encodingTypeMapping.get(encodingJavaClass);
    if (encodingCodec != null) {
      // TODO warn msg
      // this will override the default codec
      encodingTypeMapping.put(encodingJavaClass, encodingCodec);
    }

    registries.put(dataType.identifier(), dataTypeCodec);
  }

  @Override
  public void unregister(DataTypeCodec<?, ?> dataTypeCodec) {
    DataType<?, ?> dataType = dataTypeCodec.dataType();
    DataTypeCodec<?, ?> codec = registries.get(dataType.identifier());
    if (codec == null) {
      throw new IllegalArgumentException(); // TODO errmsg
    }

    Class<?> encodingJavaClass = dataType.encodingJavaClass();
    DataTypeCodec<?, ?> encodingCodec = encodingTypeMapping.get(encodingJavaClass);
    if (encodingCodec == dataTypeCodec) {
      // TODO warn msg
      // this will remove the default encoding codec
      encodingTypeMapping.remove(encodingJavaClass);
    }

    registries.remove(dataType.identifier());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <ET, DT> DataTypeCodec<ET, DT> lookup(DataType<ET, DT> dataType) {
    DataTypeCodec<?, ?> dataTypeCodec = registries.get(dataType.identifier());
    if (dataTypeCodec == null) {
      throw new RuntimeException(); // TODO errmsg
    }
    return (DataTypeCodec<ET, DT>) dataTypeCodec;
  }

  public IntObjectHashMap<DataTypeCodec<?, ?>> registries() {
    return registries;
  }

  @SuppressWarnings("unchecked")
  public <ET> DataTypeCodec<ET, ?> lookupForEncoding(Class<ET> clazz) {
    DataTypeCodec<ET, ?> dataTypeCodec = (DataTypeCodec<ET, ?>) encodingTypeMapping.get(clazz);
    if (dataTypeCodec == null) {
      throw new RuntimeException(); //TODO errmsg
    }
    return dataTypeCodec;
  }

  @Override
  public Collection<DataTypeCodec<?, ?>> listAll() {
    return registries.values();
  }
}
