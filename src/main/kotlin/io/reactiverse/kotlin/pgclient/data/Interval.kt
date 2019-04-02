package io.reactiverse.kotlin.pgclient.data

import io.reactiverse.pgclient.data.Interval

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.data.Interval] objects.
 *
 * Postgres Interval is date and time based
 * such as 120 years 3 months 332 days 20 hours 20 minutes 20.999999 seconds
 *
 * @param days 
 * @param hours 
 * @param microseconds 
 * @param minutes 
 * @param months 
 * @param seconds 
 * @param years 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.data.Interval original] using Vert.x codegen.
 */
fun Interval(
  days: Int? = null,
  hours: Int? = null,
  microseconds: Int? = null,
  minutes: Int? = null,
  months: Int? = null,
  seconds: Int? = null,
  years: Int? = null): Interval = io.reactiverse.pgclient.data.Interval().apply {

  if (days != null) {
    this.setDays(days)
  }
  if (hours != null) {
    this.setHours(hours)
  }
  if (microseconds != null) {
    this.setMicroseconds(microseconds)
  }
  if (minutes != null) {
    this.setMinutes(minutes)
  }
  if (months != null) {
    this.setMonths(months)
  }
  if (seconds != null) {
    this.setSeconds(seconds)
  }
  if (years != null) {
    this.setYears(years)
  }
}

