# MySQL for reactive-pg-client

An experiment to support MySQL protocol using current reactive-pg-client API.

## Project

This is only a POC project, and we will see how MySQL works with reactive-pg-client.

Supports:

* plain handshake
* MySQL Native41 authentication
* TEXT Protocol(QUERY)
* PING COMMAND(dropped temporarily for PG API)
* Rich data type support
* Collector API (Reuse from postgres client implementation)

TODO:

- [x] Connection (Initial plain handshake)
- [ ] SSL handshake
- [ ] Authentication(caching_sha2_password, auth switch, etc...)
- [ ] Text Protocol(Local INFILE Request)
- [ ] Text Protocol(All Utility Commands)
- [x] Binary protocol (Prepared Statement part)
- [ ] Cursor support
- [ ] Full Datatype codec support
- [ ] Compression
- [ ] Stored Procedures
- [ ] Replication Protocol(Binlog, row, semi-sync)
- [ ] Some protocol details(not clarified well in official documentation) and confusing issues to be solved.
- [ ] See if we can support MariaDB because MariaDB protocol is similar to MySQL protocol
- [ ] Maybe X protocol(Protobuf serialization + TCP) can also be implemented, but it's totally different from MySQL C/S Protocol

Need check:

- [ ] do we only support UTF-8 charset? (utf8mb4 utf8mb3)
https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-charsets.html

## Supported Data Type

* INT1(j.l.Byte)
* INT2(j.l.Short)
* INT3(j.l.Integer)
* INT4(j.l.Integer)
* INT8(j.l.Long)
* FLOAT(j.l.Float)
* DOUBLE(j.l.Double)
* NUMERIC,DECIMAL(i.r.p.data.Numeric)
* BOOLEAN(j.l.Byte)=INT1(j.l.Byte) TODO what java type should be mapped?
* CHAR(j.l.String)=BINARY(j.l.String) TODO what java type should be mapped?
* VARCHAR(j.l.String)=VARBINARY(j.l.String) TODO what java type should be mapped? 
* TINYBLOB,BLOB,MEDIUMBLOB,LONGBLOB(i.v.c.b.Buffer)=TINYTEXT,TEXT,MEDIUMTEXT,LONGTEXT(i.v.c.b.Buffer) TODO what java type should be mapped? 
* DATE(j.t.LocalDate)
* TIME(j.t.LocalTime)
* DATETIME(j.t.LocalDateTime)

TODO:
- [ ] Numeric: BIT
- [ ] String: BINARY, VARBINARY, TINYBLOB, TINYTEXT, BLOB, TEXT, MEDIUMBLOB, MEDIUMTEXT, LONGBLOB, LONGTEXT, ENUM, SET
- [ ] Data and Time: TIMESTAMP, YEAR
- [ ] JSON
- [ ] Spatial data types

## Development

Implementation in is package `io.reactiverse.mysqlclient` of `src/main/java`.

Tests are in the package `io.reactiverse.mysqlclient` of `src/test/java` and can be regarded as examples for how to use this client. 

## Resources

Official Documentation:

* https://dev.mysql.com/doc/dev/mysql-server/latest/PAGE_PROTOCOL.html

* https://dev.mysql.com/doc/internals/en/client-server-protocol.html *outdated but something is still useful*

* https://mariadb.com/kb/en/library/clientserver-protocol/ *MariaDB protocol is similar and documentation is well organized*





