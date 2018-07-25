package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Postgres Interval is date and time based
 * such as 120 years 3 months 332 days 20 hours 20 minutes 20.999999 seconds
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

@DataObject(generateConverter = true)
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

  public Interval(JsonObject json) {
    IntervalConverter.fromJson(json, this);
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
  public String toString() {
    return "Interval( " + years + " years " + months + " months " + days + " days " + hours + " hours " +
      minutes + " minutes " + seconds + (microseconds == 0 ? "" : "." + Math.abs(microseconds)) + " seconds )";
  }

}
