package io.vertx.pgclient.data;

import java.time.Duration;

/**
 * Postgres Interval is date and time based
 * such as 120 years 3 months 332 days 20 hours 20 minutes 20.999999 seconds
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Interval {

  private int years, months, days, hours, minutes, seconds, microseconds;

  public Interval() {
    this(0, 0, 0, 0, 0, 0, 0);
  }

  public Interval(int years, int months, int days, int hours, int minutes, int seconds, int microseconds) {
    this.years = years;
    this.months = months;
    this.days = days;
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
    this.microseconds = microseconds;
  }

  public Interval(int years, int months, int days, int hours, int minutes, int seconds) {
    this(years, months, days, hours, minutes, seconds, 0);
  }

  public Interval(int years, int months, int days, int hours, int minutes) {
    this(years, months, days, hours, minutes, 0);
  }

  public Interval(int years, int months, int days, int hours) {
    this(years, months, days, hours, 0);
  }

  public Interval(int years, int months, int days) {
    this(years, months, days, 0);
  }

  public Interval(int years, int months) {
    this(years, months, 0);
  }

  public Interval(int years) {
    this(years, 0);
  }

  public static Interval of() {
    return new Interval();
  }

  public static Interval of(int years, int months, int days, int hours, int minutes, int seconds, int microseconds) {
    return new Interval(years, months, days, hours, minutes, seconds, microseconds);
  }

  public static Interval of(int years, int months, int days, int hours, int minutes, int seconds) {
    return new Interval(years, months, days, hours, minutes, seconds);
  }

  public static Interval of(int years, int months, int days, int hours, int minutes) {
    return new Interval(years, months, days, hours, minutes);
  }

  public static Interval of(int years, int months, int days, int hours) {
    return new Interval(years, months, days, hours);
  }

  public static Interval of(int years, int months, int days) {
    return new Interval(years, months, days);
  }

  public static Interval of(int years, int months) {
    return new Interval(years, months);
  }

  public static Interval of(int years) {
    return new Interval(years);
  }

  /**
   * Creates an instance from the given {@link Duration}.
   * <p>
   * The conversion algorithm assumes a year lasts 12 months and a month lasts 30 days, as <a href="https://github.com/postgres/postgres/blob/5bbdfa8a18dc56d3e64aa723a68e02e897cb5ec3/src/include/datatype/timestamp.h#L116">Postgres does</a> and ISO 8601 suggests.
   *
   * @param duration the value to convert
   * @return a new instance of {@link Interval}
   */
  public static Interval of(Duration duration) {
    long totalSeconds = duration.getSeconds();

    int years = (int) (totalSeconds / 31104000);
    long remainder = totalSeconds % 31104000;

    int months = (int) (remainder / 2592000);
    remainder = totalSeconds % 2592000;

    int days = (int) (remainder / 86400);
    remainder = remainder % 86400;

    int hours = (int) (remainder / 3600);
    remainder = remainder % 3600;

    int minutes = (int) (remainder / 60);
    remainder = remainder % 60;

    return new Interval(years, months, days, hours, minutes, (int) remainder, duration.getNano() / 1000);
  }

  public Interval years(int years)  {
    this.years = years;
    return this;
  }

  public Interval months(int months)  {
    this.months = months;
    return this;
  }

  public Interval days(int days)  {
    this.days = days;
    return this;
  }

  public Interval hours(int hours)  {
    this.hours = hours;
    return this;
  }

  public Interval minutes(int minutes)  {
    this.minutes = minutes;
    return this;
  }

  public Interval seconds(int seconds)  {
    this.seconds = seconds;
    return this;
  }

  public Interval microseconds(int microseconds)  {
    this.microseconds = microseconds;
    return this;
  }

  public int getYears() {
    return years;
  }

  public void setYears(int years) {
    this.years = years;
  }

  public int getMonths() {
    return months;
  }

  public void setMonths(int months) {
    this.months = months;
  }

  public int getDays() {
    return days;
  }

  public void setDays(int days) {
    this.days = days;
  }

  public int getHours() {
    return hours;
  }

  public void setHours(int hours) {
    this.hours = hours;
  }

  public int getMinutes() {
    return minutes;
  }

  public void setMinutes(int minutes) {
    this.minutes = minutes;
  }

  public int getSeconds() {
    return seconds;
  }

  public void setSeconds(int seconds) {
    this.seconds = seconds;
  }

  public int getMicroseconds() {
    return microseconds;
  }

  public void setMicroseconds(int microseconds) {
    this.microseconds = microseconds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Interval interval = (Interval) o;
    return years == interval.years &&
      months == interval.months &&
      days == interval.days &&
      hours == interval.hours &&
      minutes == interval.minutes &&
      seconds == interval.seconds &&
      microseconds == interval.microseconds;
  }

  @Override
  public int hashCode() {
    int result = years;
    result = 31 * result + months;
    result = 31 * result + days;
    result = 31 * result + hours;
    result = 31 * result + minutes;
    result = 31 * result + seconds;
    result = 31 * result + microseconds;
    return result;
  }

  @Override
  public String toString() {
    return "Interval( "
           + years + " years "
           + months + " months "
           + days + " days "
           + hours + " hours "
           + minutes + " minutes "
           + seconds + " seconds "
           + microseconds + " microseconds )";
  }

  /**
   * Convert this interval to an instance of {@link Duration}.
   * <p>
   * The conversion algorithm assumes a year lasts 12 months and a month lasts 30 days, as <a href="https://github.com/postgres/postgres/blob/5bbdfa8a18dc56d3e64aa723a68e02e897cb5ec3/src/include/datatype/timestamp.h#L116">Postgres does</a> and ISO 8601 suggests.
   *
   * @return an instance of {@link Duration} representing the same amount of time as this interval
   */
  public Duration toDuration() {
    return Duration.ofSeconds(((((years * 12L + months) * 30L + days) * 24L + hours) * 60 + minutes) * 60 + seconds)
      .plusNanos(microseconds * 1000L);
  }
}
