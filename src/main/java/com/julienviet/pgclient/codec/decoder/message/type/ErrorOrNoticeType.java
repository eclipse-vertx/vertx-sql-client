package com.julienviet.pgclient.codec.decoder.message.type;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ErrorOrNoticeType {
  public static final byte SEVERITY = 'S';
  public static final byte CODE = 'C';
  public static final byte MESSAGE = 'M';
  public static final byte DETAIL = 'D';
  public static final byte HINT = 'H';
  public static final byte POSITION = 'P';
  public static final byte INTERNAL_POSITION = 'p';
  public static final byte INTERNAL_QUERY = 'q';
  public static final byte WHERE = 'W';
  public static final byte FILE = 'F';
  public static final byte LINE = 'L';
  public static final byte ROUTINE = 'R';
  public static final byte SCHEMA = 's';
  public static final byte TABLE = 't';
  public static final byte COLUMN = 'c';
  public static final byte DATA_TYPE = 'd';
  public static final byte CONSTRAINT = 'n';
}
