#!/bin/bash

#
# Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
# which is available at https://www.apache.org/licenses/LICENSE-2.0.
#
# SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
#

echo "BEGIN SSH SETUP"

su - ${DB2INSTANCE} -c "db2 update dbm cfg using DIAGLEVEL 2"
su - ${DB2INSTANCE} -c "db2 update dbm cfg using SSL_SVR_KEYDB /certs/server.kdb"
su - ${DB2INSTANCE} -c "db2 update dbm cfg using SSL_SVR_STASH /certs/server.sth"
su - ${DB2INSTANCE} -c "db2 update dbm cfg using SSL_SVR_LABEL mylabel"
su - ${DB2INSTANCE} -c "db2 update dbm cfg using ssl_svcename 50001"
su - ${DB2INSTANCE} -c "db2set -i vertx DB2COMM=SSL,TCPIP"
su - ${DB2INSTANCE} -c "db2stop"
su - ${DB2INSTANCE} -c "db2start"

echo "VERTX SSH SETUP DONE"
