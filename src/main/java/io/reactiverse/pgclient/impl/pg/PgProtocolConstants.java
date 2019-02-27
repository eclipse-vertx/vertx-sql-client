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
package io.reactiverse.pgclient.impl.pg;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class PgProtocolConstants {

  public static final int AUTHENTICATION_TYPE_OK = 0;
  public static final int AUTHENTICATION_TYPE_KERBEROS_V5 = 2;
  public static final int AUTHENTICATION_TYPE_CLEARTEXT_PASSWORD = 3;
  public static final int AUTHENTICATION_TYPE_MD5_PASSWORD = 5;
  public static final int AUTHENTICATION_TYPE_SCM_CREDENTIAL = 6;
  public static final int AUTHENTICATION_TYPE_GSS = 7;
  public static final int AUTHENTICATION_TYPE_GSS_CONTINUE = 8;
  public static final int AUTHENTICATION_TYPE_SSPI = 9;

  public static final byte ERROR_OR_NOTICE_SEVERITY = 'S';
  public static final byte ERROR_OR_NOTICE_CODE = 'C';
  public static final byte ERROR_OR_NOTICE_MESSAGE = 'M';
  public static final byte ERROR_OR_NOTICE_DETAIL = 'D';
  public static final byte ERROR_OR_NOTICE_HINT = 'H';
  public static final byte ERROR_OR_NOTICE_POSITION = 'P';
  public static final byte ERROR_OR_NOTICE_INTERNAL_POSITION = 'p';
  public static final byte ERROR_OR_NOTICE_INTERNAL_QUERY = 'q';
  public static final byte ERROR_OR_NOTICE_WHERE = 'W';
  public static final byte ERROR_OR_NOTICE_FILE = 'F';
  public static final byte ERROR_OR_NOTICE_LINE = 'L';
  public static final byte ERROR_OR_NOTICE_ROUTINE = 'R';
  public static final byte ERROR_OR_NOTICE_SCHEMA = 's';
  public static final byte ERROR_OR_NOTICE_TABLE = 't';
  public static final byte ERROR_OR_NOTICE_COLUMN = 'c';
  public static final byte ERROR_OR_NOTICE_DATA_TYPE = 'd';
  public static final byte ERROR_OR_NOTICE_CONSTRAINT = 'n';

  public static final byte MESSAGE_TYPE_BACKEND_KEY_DATA = 'K';
  public static final byte MESSAGE_TYPE_AUTHENTICATION = 'R';
  public static final byte MESSAGE_TYPE_ERROR_RESPONSE = 'E';
  public static final byte MESSAGE_TYPE_NOTICE_RESPONSE = 'N';
  public static final byte MESSAGE_TYPE_NOTIFICATION_RESPONSE = 'A';
  public static final byte MESSAGE_TYPE_COMMAND_COMPLETE = 'C';
  public static final byte MESSAGE_TYPE_PARAMETER_STATUS = 'S';
  public static final byte MESSAGE_TYPE_READY_FOR_QUERY = 'Z';
  public static final byte MESSAGE_TYPE_PARAMETER_DESCRIPTION = 't';
  public static final byte MESSAGE_TYPE_ROW_DESCRIPTION = 'T';
  public static final byte MESSAGE_TYPE_DATA_ROW = 'D';
  public static final byte MESSAGE_TYPE_PORTAL_SUSPENDED = 's';
  public static final byte MESSAGE_TYPE_NO_DATA = 'n';
  public static final byte MESSAGE_TYPE_EMPTY_QUERY_RESPONSE = 'I';
  public static final byte MESSAGE_TYPE_PARSE_COMPLETE = '1';
  public static final byte MESSAGE_TYPE_BIND_COMPLETE = '2';
  public static final byte MESSAGE_TYPE_CLOSE_COMPLETE = '3';
  public static final byte MESSAGE_TYPE_FUNCTION_RESULT = 'V';
  public static final byte MESSAGE_TYPE_SSL_YES = 'S';
  public static final byte MESSAGE_TYPE_SSL_NO = 'N';
}
