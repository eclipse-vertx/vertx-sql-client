package io.reactiverse.pgclient.copy;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * The options for configuring a text or CSV COPY FROM command
 *
 */
@DataObject(generateConverter = true)
public class CopyFromOptions {

  /**
   * Creates options for copying from a CSV.
   * @return the options
   */
  public static CopyFromOptions csv() {
    return new CopyFromOptions(CopyFormat.CSV);
  }

  /**
   * Creates options for copying from a text file.
   * @return the options
   */
  public static CopyFromOptions text() {
    return new CopyFromOptions(CopyFormat.TEXT);
  }

  /**
   * Creates options for copying binary data.
   * @return the options
   */
  public static CopyFromOptions binary() {
    return new CopyFromOptions(CopyFormat.BINARY);
  }

  private String delimiter;
  private String nullCharacter;
  private CopyFormat format;

  public CopyFromOptions() {
    this(CopyFormat.TEXT);
  }

  private CopyFromOptions(CopyFormat format) {
    this.format = format;
    setDelimiter(format.getDefaultDelimiter());
    setNullCharacter(format.getNullCharacter());
  }

  public CopyFromOptions(CopyFromOptions other) {
    this.delimiter = other.delimiter;
    this.nullCharacter = other.nullCharacter;
    this.format = other.format;
  }

  public CopyFromOptions(JsonObject object) {
    this.delimiter = object.getString("delimiter");
    this.nullCharacter = object.getString("nullCharacter");
    this.format = CopyFormat.valueOf(object.getString("format").toUpperCase());
  }

  /**
   * Set the delimiter for copying from a file. Must be one character.
   *
   * @param delimiter  the delimiter to be used.
   * @return a reference to this, so the API can be used fluently
   */
  public CopyFromOptions setDelimiter(String delimiter) {
    this.delimiter = delimiter;
    return this;
  }

  /**
   * @return  the delimiter
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * Set the null character for copying from a file. Must be one character.
   *
   * @param nullChar  the null character to be used.
   * @return a reference to this, so the API can be used fluently
   */
  public CopyFromOptions setNullCharacter(String nullChar) {
    this.nullCharacter = nullChar;
    return this;
  }

  /**
   * @return  the null character
   */
  public String getNullCharacter() {
    return nullCharacter;
  }

  /**
   * @return  the format
   */
  public CopyFormat getFormat() {
    return format;
  }

  /**
   * Set the format of this COPY FROM operation.
   *
   * @param format  the format of this operation.
   * @return a reference to this, so the API can be used fluently
   */
  public CopyFromOptions setFormat(CopyFormat format) {
    this.format = format;
    return this;
  }

  /**
   * Converts these options to JSON.
   * @return the {@link JsonObject}
   */
  public JsonObject toJson() {
   return new JsonObject()
     .put("delimiter", getDelimiter())
     .put("nullCharacter", getNullCharacter())
     .put("format", getFormat().name());
  }
}
