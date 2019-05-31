## Postgres containers for testing

There will be 2 Postgres containers for testing, one with SSL enabled is for `TLSTest` and the other one with SSL disabled is for all other tests.

### Configure the containers

modify the content in the `docker-compose.yml`

### Run the containers

```
> docker-compose up --build -V
```

### Run tests

```
> mvn test -Dconnection.uri=postgres://postgres:postgres@localhost:5432/postgres -Dtls.connection.uri=postgres://postgres:postgres@localhost:5433/postgres -Dunix.socket.directory=/var/run/postgresql -Dunix.socket.port=5432
```
