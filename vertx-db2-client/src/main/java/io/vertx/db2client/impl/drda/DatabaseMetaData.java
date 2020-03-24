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

public class DatabaseMetaData {
  
  private boolean isZos;
  
  public String databaseName;
  
  private Charset currentCCSID = CCSIDConstants.EBCDIC;
  
  public void setZos(boolean isZos) {
    this.isZos = isZos;
    if (isZos && currentCCSID != CCSIDConstants.UTF8) {
      currentCCSID = CCSIDConstants.UTF8;
      
      // DB2 on Z doesn't have small packages by default -- remove them
      SectionManager.INSTANCE.removeSmallPackages();
    }
  }
  
  public boolean isZos() {
    return isZos;
  }
  
  public Charset getCCSID() {
    return currentCCSID;
  }

}
