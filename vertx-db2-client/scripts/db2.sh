#!/bin/bash
echo "Starting DB2 database with:"
echo "    user=db2user"
echo "    pass=db2pass"
echo "  dbname=vertx_db"
echo "    port=50000"
echo ""
echo "### DB will be usable when you see the following message ###"
echo "    (*) Setup has completed."
echo ""
echo "Press Ctrl+C to exit when you are finished"
echo ""
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 \
  --name db2-vertx \
  -e DBNAME=vertx_db \
  -e DB2INSTANCE=db2user \
  -e DB2INST1_PASSWORD=db2pass \
  -e AUTOCONFIG=false \
  -e ARCHIVE_LOGS=false \
  -e LICENSE=accept \
  -p 50000:50000 \
  --privileged \
  ibmcom/db2:11.5.0.0a

