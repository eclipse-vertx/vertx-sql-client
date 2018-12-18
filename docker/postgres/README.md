## Postgres docker file for testing

### Build the container

```
> docker build -t test/postgres .
```

### Run the container

```
> docker run --rm --name test-postgres -v /var/run/postgresql:/var/run/postgresql -p 5432:5432 test/postgres
```

### Run tests

```
> mvn test -Dconnection.uri=postgres://postgres:postgres@localhost/postgres -Dunix.socket.directory=/var/run/postgresql
```
