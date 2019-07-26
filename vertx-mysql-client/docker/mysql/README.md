## MySQL containers for testing

### Configure the containers

modify the content in the `docker-compose.yml`

### Run the containers

```
> docker-compose up --build -V
```

### Run tests

```
> mvn test -Dconnection.uri=mysql://mysql:password@localhost:3306/testschema
```
