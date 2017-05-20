package com.julienviet.pgclient.codec.formatter;

import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class TimeFormatter {

  public static final java.time.format.DateTimeFormatter TIMETZ_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_TIME)
    .appendOffset("+HH:mm", "00:00")
    .toFormatter();
}
