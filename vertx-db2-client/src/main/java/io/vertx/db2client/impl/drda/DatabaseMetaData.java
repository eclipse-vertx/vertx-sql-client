package io.vertx.db2client.impl.drda;

public class DatabaseMetaData {
  
  private boolean isZos;
  
  public String databaseName;
  
  public void setZos(boolean isZos) {
    this.isZos = isZos;
    if (isZos && CCSIDConstants.currentCCSID != CCSIDConstants.UTF8) {
      CCSIDConstants.currentCCSID = CCSIDConstants.UTF8;
      
      // DB2 on Z doesn't have small packages by default -- remove them
      SectionManager.INSTANCE.removeSmallPackages();
    }
  }
  
  public boolean isZos() {
    return isZos;
  }

}
