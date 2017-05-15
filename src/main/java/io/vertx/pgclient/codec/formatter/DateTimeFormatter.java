package io.vertx.pgclient.codec.formatter;

import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.*;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DateTimeFormatter {

  public static final java.time.format.DateTimeFormatter TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(ISO_LOCAL_TIME)
    .toFormatter();

  public static final java.time.format.DateTimeFormatter TIMESTAMPTZ_FORMAT = new DateTimeFormatterBuilder()
    .append(TIMESTAMP_FORMAT)
    .appendOffset("+HH:mm", "00:00")
    .toFormatter();
}
