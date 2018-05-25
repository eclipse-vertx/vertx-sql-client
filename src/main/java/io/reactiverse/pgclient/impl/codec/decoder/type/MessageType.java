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

import io.reactiverse.pgclient.impl.codec.decoder.MessageDecoder;

/**
 *
 * Backend message types for {@link MessageDecoder}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageType {
  public static final byte BACKEND_KEY_DATA = 'K';
  public static final byte AUTHENTICATION = 'R';
  public static final byte ERROR_RESPONSE = 'E';
  public static final byte NOTICE_RESPONSE = 'N';
  public static final byte NOTIFICATION_RESPONSE = 'A';
  public static final byte COMMAND_COMPLETE = 'C';
  public static final byte PARAMETER_STATUS = 'S';
  public static final byte READY_FOR_QUERY = 'Z';
  public static final byte PARAMETER_DESCRIPTION = 't';
  public static final byte ROW_DESCRIPTION = 'T';
  public static final byte DATA_ROW = 'D';
  public static final byte PORTAL_SUSPENDED = 's';
  public static final byte NO_DATA = 'n';
  public static final byte EMPTY_QUERY_RESPONSE = 'I';
  public static final byte PARSE_COMPLETE = '1';
  public static final byte BIND_COMPLETE = '2';
  public static final byte CLOSE_COMPLETE = '3';
  public static final byte FUNCTION_RESULT = 'V';
  public static final byte SSL_YES = 'S';
  public static final byte SSL_NO = 'N';
}
