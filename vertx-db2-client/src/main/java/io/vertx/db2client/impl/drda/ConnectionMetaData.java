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

import java.nio.charset.Charset;

import io.vertx.db2client.impl.DB2DatabaseMetadata;

public class ConnectionMetaData {
  
  public byte[] correlationToken;
  public String databaseName;
  private DB2DatabaseMetadata dbMetadata;
  public final SectionManager sectionManager = new SectionManager();
  
  private Charset currentCCSID = CCSIDConstants.EBCDIC;
  
  public Charset getCCSID() {
    return currentCCSID;
  }
  
  public boolean isZos() {
    return dbMetadata.isZOS();
  }
  
  public void setDbMetadata(DB2DatabaseMetadata metadata) {
    if (metadata.isZOS()) currentCCSID = CCSIDConstants.UTF8;
    dbMetadata = metadata;
  }
  
  public DB2DatabaseMetadata getDbMetadata() {
    return dbMetadata;
  }

}
