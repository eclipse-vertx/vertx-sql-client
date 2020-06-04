#!/bin/bash

echo "BEGIN SSH SETUP"
export PATH=/opt/ibm/db2/V11.5/bin/:$PATH

su - vertx -c 'db2 update dbm cfg using SSL_SVR_KEYDB /certs/server.kdb'
su - vertx -c 'db2 update dbm cfg using SSL_SVR_STASH /certs/server.sth'
su - vertx -c 'db2 update dbm cfg using SSL_SVR_LABEL mylabel'
su - vertx -c 'db2 update dbm cfg using ssl_svcename 50001'
su - vertx -c 'db2set -i vertx DB2COMM=SSL,TCPIP'
su - vertx -c 'db2stop'
su - vertx -c 'db2start'
  
echo "VERTX SSH SETUP DONE"