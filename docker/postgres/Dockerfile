FROM healthcheck/postgres:alpine
ENV POSTGRES_DB postgres
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD postgres
COPY ./resources/create-postgres.sql /docker-entrypoint-initdb.d/create-postgres.sql
COPY ./resources/server.crt /server.crt
COPY ./resources/server.key /server.key
COPY ./ssl.sh /docker-entrypoint-initdb.d/ssl.sh
