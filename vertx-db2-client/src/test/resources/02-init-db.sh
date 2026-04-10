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

set -e

echo "BEGIN DB2 DATABASE INITIALIZATION"

su - ${DB2INSTANCE} -c "db2 force applications all; sleep 5; echo '--- Connecting ---'; db2 connect to ${DBNAME}; echo '--- Running SQL ---'; db2 -tvf /tmp/init.sql; echo '--- Finished SQL ---'"

echo "DB2 DATABASE INITIALIZATION COMPLETE"
