package com.julienviet.pgclient;

import java.util.ArrayList;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Result extends ArrayList<Row> {

  private int updatedRows;


  public int getUpdatedRows() {
    return updatedRows;
  }

  public Result setUpdatedRows(int updatedRows) {
    this.updatedRows = updatedRows;
    return this;
  }
}
