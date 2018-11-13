package io.reactiverse.kotlin.pgclient.copy

import io.reactiverse.pgclient.copy.CopyFromOptions
import io.reactiverse.pgclient.copy.CopyFormat

/**
 * A function providing a DSL for building [io.reactiverse.pgclient.copy.CopyFromOptions] objects.
 *
 * The options for configuring a text or CSV COPY FROM command
 *
 *
 * @param delimiter  Set the delimiter for copying from a file. Must be one character.
 * @param format  Set the format of this COPY FROM operation.
 * @param nullCharacter  Set the null character for copying from a file. Must be one character.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.copy.CopyFromOptions original] using Vert.x codegen.
 */
fun CopyFromOptions(
  delimiter: String? = null,
  format: CopyFormat? = null,
  nullCharacter: String? = null): CopyFromOptions = io.reactiverse.pgclient.copy.CopyFromOptions().apply {

  if (delimiter != null) {
    this.setDelimiter(delimiter)
  }
  if (format != null) {
    this.setFormat(format)
  }
  if (nullCharacter != null) {
    this.setNullCharacter(nullCharacter)
  }
}

