/*
 * Copyright 2019 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
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
fun intervalOf(
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
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("intervalOf(days, hours, microseconds, minutes, months, seconds, years)")
)
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

