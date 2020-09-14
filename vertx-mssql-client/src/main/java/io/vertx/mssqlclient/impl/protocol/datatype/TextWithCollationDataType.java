/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol.datatype;

import java.sql.JDBCType;

// BIGCHARTYPE, BIGVARCHRTYPE, TEXTTYPE, NTEXTTYPE, NCHARTYPE, or NVARCHARTYPE
public class TextWithCollationDataType extends MSSQLDataType {
  private final String collation;

  public TextWithCollationDataType(int id, Class<?> mappedJavaType, String collation) {
    super(id, mappedJavaType, JDBCType.VARCHAR);
    this.collation = collation;
  }

  public String collation() {
    return collation;
  }
}
