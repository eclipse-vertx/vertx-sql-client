/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.pgclient.impl.codec.decoder.type;

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
