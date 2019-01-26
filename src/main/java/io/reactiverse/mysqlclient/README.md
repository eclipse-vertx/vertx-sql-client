# MySQL for reactive-pg-client

An experiment to support MySQL protocol using current reactive-pg-client API.

## Project

This is only a POC project, and we will see how MySQL works with reactive-pg-client.

Supports:

* plain handshake
* MySQL Native41 authentication
* TEXT Protocol(QUERY)
* PING COMMAND
* INT2,INT3,INT4,INT8,FLOAT,DOUBLE,VARCHAR Data type
* Collector API (Reuse from postgres client implementation)

TODO:

- [x] Connection (Initial plain handshake)
- [ ] SSL handshake
- [ ] Authentication(caching_sha2_password, auth switch, etc...)
- [ ] Text Protocol(Local INFILE Request)
- [ ] All Utility Commands
- [ ] Binary protocol (Prepared Statement)
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

## Development

Implementation in is package `io.reactiverse.mysqlclient` of `src/main/java`.

Tests are in the package `io.reactiverse.mysqlclient` of `src/test/java` and can be regarded as examples for how to use this client. 

## Resources

Official Documentation:

* https://dev.mysql.com/doc/dev/mysql-server/latest/PAGE_PROTOCOL.html

* https://dev.mysql.com/doc/internals/en/client-server-protocol.html *outdated but something is still useful*

* https://mariadb.com/kb/en/library/clientserver-protocol/ *MariaDB protocol is similar and documentation is well organized*





