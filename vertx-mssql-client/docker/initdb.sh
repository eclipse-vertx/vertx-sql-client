#!/bin/sh

set -e

SERVER="$1"
PASSWORD="$2"
SQL_FILE="$3"

until /opt/mssql-tools18/bin/sqlcmd -l 1 -S $SERVER -U SA -P $PASSWORD -C -No -q "SELECT 1"; do
  >&2 echo "MSSQL is unavailable - sleeping"
  sleep 1
done

>&2 echo "MSSQL is up - executing command"
/opt/mssql-tools18/bin/sqlcmd -S $SERVER -U SA -P $PASSWORD -i $SQL_FILE -C -No
