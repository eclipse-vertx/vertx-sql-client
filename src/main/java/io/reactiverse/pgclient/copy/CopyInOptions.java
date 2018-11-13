package io.reactiverse.pgclient.copy;

import io.vertx.codegen.annotations.DataObject;

@DataObject(generateConverter = true)
public class CopyInOptions {

  public static CopyInOptions csv() {
    return new CopyInOptions(CopyTextFormat.CSV);
  }

  public static CopyInOptions text() {
    return new CopyInOptions(CopyTextFormat.TEXT);
  }

  private String delimiter;
  private String nullCharacter;
  private CopyTextFormat format;

  public CopyInOptions() {
    this(CopyTextFormat.TEXT);
  }

  private CopyInOptions(CopyTextFormat format) {
    this.format = format;
    setDelimiter(format.getDefaultDelimiter());
    setNullCharacter(format.getNullCharacter());
  }

  public CopyInOptions(CopyInOptions other) {
    this.delimiter = other.delimiter;
    this.nullCharacter = other.nullCharacter;
    this.format = other.format;
  }

  public CopyInOptions setDelimiter(String delimiter) {
    this.delimiter = delimiter;
    return this;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public CopyInOptions setNullCharacter(String nullChar) {
    this.nullCharacter = nullChar;
    return this;
  }

  public String getNullCharacter() {
    return nullCharacter;
  }

  public CopyTextFormat getFormat() {
    return format;
  }

  public CopyInOptions setFormat(CopyTextFormat format) {
    this.format = format;
    return this;
  }
}
