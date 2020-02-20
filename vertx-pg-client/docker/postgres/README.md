## Postgres containers for testing

There will be 2 Postgres containers for testing, one with SSL enabled is for `TLSTest` and the other one with SSL disabled is for all other tests.

### Configure the containers

modify the content in the `docker-compose.yml`

If you want to test with unix domain socket on your Linux machine(Note Unix domain socket is not supported on OS X for now), you could configure the unix socket directory volume in the compose file.

### Run the containers

```
> docker-compose up --build -V
```

### Run tests

Test without enabling unix domain socket testing

```
> mvn test -Dconnection.uri=postgres://postgres:postgres@localhost:5432/postgres -Dtls.connection.uri=postgres://postgres:postgres@localhost:5433/postgres
```

Test with enabling unix domain socket testing

```
> mvn test -Dconnection.uri=postgres://postgres:postgres@localhost:5432/postgres -Dtls.connection.uri=postgres://postgres:postgres@localhost:5433/postgres -Dunix.socket.connection.uri=postgres://postgres:postgres@/var/run/postgresql:5432/postgres
```
